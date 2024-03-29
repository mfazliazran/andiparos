/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.parosproxy.paros.Constant;

public class HttpSender {

	private static ProtocolSocketFactory sslFactory = null;
	private static Protocol protocol = null;

	static {

		try {
			protocol = Protocol.getProtocol("https");
			sslFactory = protocol.getSocketFactory();
		} catch (Exception e) {
		}
		
		// avoid init again if already initialized
		if (sslFactory == null || !(sslFactory instanceof SSLConnector)) {
			Protocol.registerProtocol("https", new Protocol("https",
					(ProtocolSocketFactory) new SSLConnector(), 443));
		}
	}

	private static HttpMethodHelper helper = new HttpMethodHelper();
	
	private static String userAgent = "";
	
	private HttpClient client = null;
	private HttpClient clientViaProxy = null;
	private ConnectionParam param = null;
	private MultiThreadedHttpConnectionManager httpConnManager = null;
	private MultiThreadedHttpConnectionManager httpConnManagerProxy = null;
	private boolean followRedirect = false;
	private boolean allowState = false;

	public HttpSender(ConnectionParam connectionParam, boolean allowState) {
		this.param = connectionParam;
		this.allowState = allowState;

		client = createHttpClient();
		clientViaProxy = createHttpClientViaProxy();

		if (this.allowState) {
			checkState();
		}
		addAuth(client);
		addAuth(clientViaProxy);
	}

	public static SSLConnector getSSLConnector() {
		return (SSLConnector) protocol.getSocketFactory();
	}

	private void checkState() {
		if (param.isHttpStateEnabled()) {
			client.setState(param.getHttpState());
			clientViaProxy.setState(param.getHttpState());
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
			clientViaProxy.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		} else {
			client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
			clientViaProxy.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
		}
	}

	private HttpClient createHttpClient() {

		httpConnManager = new MultiThreadedHttpConnectionManager();
		setCommonManagerParams(httpConnManager);
		return new HttpClient(httpConnManager);
	}

	public HttpClient createHttpClientViaProxy() {

		if (param.getProxyChainName().equals("")) {
			return createHttpClient();
		}

		httpConnManagerProxy = new MultiThreadedHttpConnectionManager();
		setCommonManagerParams(httpConnManagerProxy);
		HttpClient clientProxy = new HttpClient(httpConnManagerProxy);
		clientProxy.getHostConfiguration().setProxy(param.getProxyChainName(), param.getProxyChainPort());

		if (!param.getProxyChainUserName().equals("")) {

			// NTCredentials credentials = new NTCredentials(
			// param.getProxyChainUserName(), param.getProxyChainPassword(),
			// param.getProxyChainName(), param.getProxyChainName());
			NTCredentials credentials = new NTCredentials(
					param.getProxyChainUserName(),
					param.getProxyChainPassword(), "",
					param.getProxyChainRealm().equals("")
						? "" : param.getProxyChainRealm());

			// Below is the original code, but user reported that above code
			// works.
			// UsernamePasswordCredentials credentials = new
			// UsernamePasswordCredentials(
			// param.getProxyChainUserName(), param.getProxyChainPassword());
			AuthScope authScope = new AuthScope(param.getProxyChainName(),
					param.getProxyChainPort(),
					param.getProxyChainRealm().equals("")
						? AuthScope.ANY_REALM : param.getProxyChainRealm());

			clientProxy.getState().setProxyCredentials(authScope, credentials);
		}

		return clientProxy;
	}

	public int executeMethod(HttpMethod method)
	throws HttpException, IOException
	{
		String hostName = method.getURI().getHost();
		method.setDoAuthentication(true);

		if (param.isUseProxy(hostName)) {
			return clientViaProxy.executeMethod(method);
		} else {
			return client.executeMethod(method);
		}
	}

	public void shutdown() {
		if (httpConnManager != null) {
			httpConnManager.shutdown();
		}
		if (httpConnManagerProxy != null) {
			httpConnManagerProxy.shutdown();
		}
	}

	private void addAuth(HttpClient client) {
		List<HostAuthentication> list = param.getListAuth();
		for (int i = 0; i < list.size(); i++) {
			HostAuthentication auth = (HostAuthentication) list.get(i);
			AuthScope authScope = null;
			NTCredentials credentials = null;
			try {
				authScope = new AuthScope(auth.getHostName(), auth.getPort(),
					(auth.getRealm() == null || auth.getRealm().equals("")) ? AuthScope.ANY_REALM : auth.getRealm());
				credentials = new NTCredentials(auth.getUserName(), auth.getPassword(),
					InetAddress.getLocalHost().getCanonicalHostName(), auth.getHostName());
				client.getState().setCredentials(authScope, credentials);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void sendAndReceive(HttpMessage msg)
	throws HttpException, IOException {
		sendAndReceive(msg, followRedirect);
	}

	/**
	 * Do not use this unless sure what is doing. This method works but proxy
	 * may skip the pipe without properly handle the filter.
	 * 
	 * @param msg
	 * @param pipe
	 * @param buf
	 * @throws HttpException
	 * @throws IOException
	 */
	public void sendAndReceive(HttpMessage msg, HttpOutputStream pipe, byte[] buf)
	throws HttpException, IOException
	{
		sendAndReceive(msg, followRedirect, pipe, buf);
	}

	/**
	 * Send and receive a HttpMessage.
	 * 
	 * @param msg
	 * @param isFollowRedirect
	 * @throws HttpException
	 * @throws IOException
	 */
	public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect)
	throws HttpException, IOException {

		msg.setTimeSentMillis(System.currentTimeMillis());

		try {
			if (!isFollowRedirect
				|| !(msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)
				|| msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.PUT))) {
				
				send(msg, isFollowRedirect);
				return;
			} else {
				send(msg, false);
			}

			HttpMessage temp = msg.cloneAll();
			// POST/PUT method cannot be redirected by library. Need to follow
			// by code

			// loop 1 time only because httpclient can handle redirect itself
			// after first GET.
			for (int i = 0; i < 1 && (HttpStatusCode.isRedirection(temp.getResponseHeader().getStatusCode())
				&& temp.getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED); i++) {

				String location = temp.getResponseHeader().getHeader(HttpHeader.LOCATION);
				URI baseUri = temp.getRequestHeader().getURI();
				URI newLocation = new URI(baseUri, location, false);
				temp.getRequestHeader().setURI(newLocation);
				temp.getRequestHeader().setMethod(HttpRequestHeader.GET);
				temp.getRequestHeader().setContentLength(0);
				send(temp, true);
			}

			msg.setResponseHeader(temp.getResponseHeader());
			msg.setResponseBody(temp.getResponseBody());

		} finally {
			msg.setTimeElapsedMillis((int) (System.currentTimeMillis() - msg.getTimeSentMillis()));
		}
	}

	private void send(HttpMessage msg, boolean isFollowRedirect)
	throws HttpException, IOException {
		HttpMethod method = null;
		HttpResponseHeader resHeader = null;

		try {
			method = runMethod(msg, isFollowRedirect);
			// successfully executed;
			resHeader = HttpMethodHelper.getHttpResponseHeader(method);
			resHeader.setHeader(HttpHeader.TRANSFER_ENCODING, null);
			msg.setResponseHeader(resHeader);
			msg.getResponseBody().setCharset(resHeader.getCharset());
			// process response for each listener
			msg.getResponseBody().setLength(0);
			msg.getResponseBody().append(method.getResponseBody());
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}

	}

	private HttpMethod runMethod(HttpMessage msg, boolean isFollowRedirect)
	throws HttpException, IOException {
		HttpMethod method = null;
		// no more retry
		modifyUserAgent(msg);
		method = helper.createRequestMethod(msg.getRequestHeader(), msg.getRequestBody());
		method.setFollowRedirects(isFollowRedirect);
		this.executeMethod(method);
		if (allowState) {
			if (param.isHttpStateEnabled()) {
				HttpMethodHelper.updateHttpRequestHeaderSent(msg.getRequestHeader(), method);
			}
		}
		return method;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}

	private void modifyUserAgent(HttpMessage msg) {
		
		try {
			userAgent = getUserAgent();
			
			// no modification to user agent if empty
			if (userAgent.equals("") || msg.getRequestHeader().isEmpty()) {
				return;
			}
			
			String currentUserAgent = msg.getRequestHeader().getHeader(HttpHeader.USER_AGENT);
			if (currentUserAgent == null) currentUserAgent = "";
			
			if (currentUserAgent.indexOf(userAgent) >= 0) {
				// user agent already in place, exit
				return;
			}

			msg.getRequestHeader().setHeader(HttpHeader.USER_AGENT, userAgent);

		} catch (Exception e) {
		}
	}

	/**
	 * @return Returns the userAgent.
	 */
	public static String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent
	 *            The userAgent to set.
	 */
	public static void setUserAgent(String userAgent) {;
		HttpSender.userAgent = userAgent;
	}

	private void setCommonManagerParams(MultiThreadedHttpConnectionManager mgr) {
		mgr.getParams().setSoTimeout(40000);
		mgr.getParams().setStaleCheckingEnabled(true);
		mgr.getParams().setDefaultMaxConnectionsPerHost(
				(Constant.MAX_HOST_CONNECTION > 5) ? 10 : 5 * Constant.MAX_HOST_CONNECTION);
	}

	/**
	 * Send and receive a HttpMessage.
	 * 
	 * @param msg
	 * @param isFollowRedirect
	 * @throws HttpException
	 * @throws IOException
	 */
	public void sendAndReceive(HttpMessage msg, boolean isFollowRedirect, HttpOutputStream pipe, byte[] buf)
	throws HttpException, IOException
	{

		msg.setTimeSentMillis(System.currentTimeMillis());

		try {
			if (!isFollowRedirect
					|| !(msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.POST)
					|| msg.getRequestHeader().getMethod().equalsIgnoreCase(HttpRequestHeader.PUT))) {
				send(msg, isFollowRedirect, pipe, buf);
				return;
			} else {
				send(msg, false, pipe, buf);
			}

			HttpMessage temp = msg.cloneAll();
			// POST/PUT method cannot be redirected by library. Need to follow
			// by code

			// loop 1 time only because httpclient can handle redirect itself
			// after first GET.
			for (int i = 0; i < 1
				&& (HttpStatusCode.isRedirection(temp.getResponseHeader().getStatusCode())
				&& temp.getResponseHeader().getStatusCode() != HttpStatusCode.NOT_MODIFIED); i++) {

				String location = temp.getResponseHeader().getHeader(HttpHeader.LOCATION);
				URI baseUri = temp.getRequestHeader().getURI();
				URI newLocation = new URI(baseUri, location, false);
				temp.getRequestHeader().setURI(newLocation);
				temp.getRequestHeader().setMethod(HttpRequestHeader.GET);
				temp.getRequestHeader().setContentLength(0);
				send(temp, true, pipe, buf);
			}

			msg.setResponseHeader(temp.getResponseHeader());
			msg.setResponseBody(temp.getResponseBody());

		} finally {
			msg.setTimeElapsedMillis((int) (System.currentTimeMillis() - msg.getTimeSentMillis()));
		}
	}

	/**
	 * Do not use this unless sure what is doing. This method works but proxy
	 * may skip the pipe without properly handle the filter.
	 * 
	 * @param msg
	 * @param isFollowRedirect
	 * @param pipe
	 * @param buf
	 * @throws HttpException
	 * @throws IOException
	 */
	private void send(HttpMessage msg, boolean isFollowRedirect, HttpOutputStream pipe, byte[] buf)
	throws HttpException, IOException {
		
		HttpMethod method = null;
		HttpResponseHeader resHeader = null;

		try {
			method = runMethod(msg, isFollowRedirect);
			// successfully executed;
			resHeader = HttpMethodHelper.getHttpResponseHeader(method);
			resHeader.setHeader(HttpHeader.TRANSFER_ENCODING, null);
			msg.setResponseHeader(resHeader);
			msg.getResponseBody().setCharset(resHeader.getCharset());
			msg.getResponseBody().setLength(0);
			// process response for each listener
			pipe.write(msg.getResponseHeader());
			pipe.flush();

			if (msg.getResponseHeader().getContentLength() >= 0
				&& msg.getResponseHeader().getContentLength() < 20480) {
				
				// save time expanding buffer in HttpBody
				if (msg.getResponseHeader().getContentLength() > 0) {
					msg.getResponseBody().setBody(method.getResponseBody());
					pipe.write(msg.getResponseBody());
					pipe.flush();
				}
			} else {
				InputStream in = method.getResponseBodyAsStream();

				int len = 0;
				while (in != null && (len = in.read(buf)) > 0) {
					pipe.write(buf, 0, len);
					pipe.flush();

					msg.getResponseBody().append(buf, len);
				}
			}
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}
}
