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
package org.parosproxy.paros.core.scanner.plugin;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppParamPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;

public class TestCrossSiteScript extends AbstractAppParamPlugin {

	private static final String[] XSS = {
			"<SCRIPT>alert(\"" + Constant.getEyeCatcher() + "\");</SCRIPT>", // normal XSS
			"<SCRIPT>alert(" + Constant.getEyeCatcher() + ");</SCRIPT>", // XSS without double quotes
			"test@<SCRipt>alert(" + Constant.getEyeCatcher()	+ ")</scrIPT>.example.com" // XSS to fulfill email check
	};

	public int getId() {		return 40000; }
	public int getCategory() {	return Category.HTML_INJECTION; }
	public String getName() {	return "Cross site scripting"; }
	
	public String[] getDependency() {
		return null;
	}

	public String getDescription() {
		String msg = "Cross-site scripting or HTML injection is possible. "
			+ "Malicious script may be injected into the browser which appeared to "
			+ "be genuine content from the original site. These scripts can be "
			+ "used to execute arbitrary code or steal customer sensitive "
			+ "information such as user password or cookies.\r\n"
			+ "Very often this is in the form of a hyperlink with the injected "
			+ "script embeded in the query strings. However, XSS is possible via "
			+ "FORM POST data, cookies, user data sent from another user or shared "
			+ "data retrieved from database.\r\n"
			+ "Currently this check does not verify XSS from cookie or database. "
			+ "They should be checked manually if the application retrieve database "
			+ "records from another user's input.";
		return msg;
	}

	public String getSolution() {
		String msg = "Do not trust client side input even if there is client side "
			+ "validation. Sanitize potentially danger characters in the server "
			+ "side. Very often filtering the <, >, \" characters prevented injected "
			+ "script to be executed in most cases. However, sometimes other danger "
			+ "meta-characters such as ' , (, ), /, &, ; etc are also needed.\r\n"
			+ "In addition (or if these characters are needed), HTML encode meta-"
			+ "characters in the response. For example, encode < as &lt;\r\n";
		return msg;
	}

	public String getReference() {
		String msg = "<ul><li>The OWASP guide at http://www.owasp.org/documentation/guide</li>"
			+ "<li>http://www.technicalinfo.net/papers/CSS.html</li>"
			+ "<li>http://www.cgisecurity.org/articles/xss-faq.shtml</li>"
			+ "<li>http://www.cert.org/tech_tips/malicious_code_FAQ.html</li>"
			+ "<li>http://sandsprite.com/Sleuth/papers/RealWorld_XSS_1.html</li></ul>";
		return msg;
	}

	public void init() { }

	public void scan(HttpMessage msg, String param, String value) {

		String result = null;
		int pos = 0;
		
		for (int i = 0; i < XSS.length; i++) {
			msg = getNewMsg();

			setParameter(msg, param, XSS[i]);

			try {
				sendAndReceive(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// no need to have 200 response
			// if (getResponseHeader().getStatusCode() != HttpStatusCode.OK) {
			// return;
			// }

			result = msg.getResponseBody().toString();
			pos = result.indexOf(XSS[i]);
			if (pos == -1) {
				continue;
			}

			if (i == 1) {
				// special handling for XSS without double quote
				if (result.charAt(pos - 1) != '"' && result.charAt(pos + XSS[i].length()) != '"') {
					// check if adjacent character is double quote. If so, maybe OK
					bingo(Alert.RISK_MEDIUM, Alert.SUSPICIOUS, null, param + "=" + XSS[i], null, msg);
					return;
				}
			} else {
				bingo(Alert.RISK_MEDIUM, Alert.WARNING, null, param + "=" + XSS[i], null, msg);
				return;
			}
		}
	}
}
