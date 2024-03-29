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
package org.parosproxy.paros.extension.scanner;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.extension.AbstractDialog;

/**
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ProgressDialog extends AbstractDialog {

	private static final long serialVersionUID = -6578939977401710598L;

	private JPanel jPanel = null;
	private JScrollPane paneScroll = null;
	private JButton btnStopAllHost = null;
	private JPanel paneProgress = null;
	private ExtensionScanner pluginScanner = null;

	/**
	 * @throws HeadlessException
	 */
	public ProgressDialog() throws HeadlessException {
		super();
		initialize();
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @throws HeadlessException
	 */
	public ProgressDialog(Frame arg0, boolean arg1) throws HeadlessException {
		super(arg0, arg1);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Scanning");
		this.setName("ProgressDialog");
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setContentPane(getJPanel());
		this.setSize(440, 120);
		this.setResizable(false);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				getBtnStopAllHost().doClick();
			}
		});

	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();

			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints6.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 1;
			gridBagConstraints7.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints7.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.weightx = 1.0D;
			jPanel.add(getPaneScroll(), gridBagConstraints6);
			jPanel.add(getBtnStopAllHost(), gridBagConstraints7);
		}
		return jPanel;
	}

	/**
	 * This method initializes paneScroll
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getPaneScroll() {
		if (paneScroll == null) {
			paneScroll = new JScrollPane();
			paneScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			paneScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			paneScroll.setViewportView(getPaneProgress());
		}
		return paneScroll;
	}

	/**
	 * This method initializes btnStopAllHost
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnStopAllHost() {
		if (btnStopAllHost == null) {
			btnStopAllHost = new JButton();
			btnStopAllHost.setText("Stop all hosts");
			btnStopAllHost.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (pluginScanner != null) {
						btnStopAllHost.setEnabled(false);
						pluginScanner.getScanner().stop();
					}
				}
			});

		}
		return btnStopAllHost;
	}

	/**
	 * This method initializes paneProgress
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPaneProgress() {
		if (paneProgress == null) {
			GridLayout gridLayout8 = new GridLayout();
			gridLayout8.setColumns(1);
			gridLayout8.setRows(1);
			paneProgress = new JPanel();
			paneProgress.setLayout(gridLayout8);
			paneProgress.setSize(440, 120);
		}
		return paneProgress;
	}

	void addHostProgress(final String hostAndPort, final HostProcess hostThread) {
		if (EventQueue.isDispatchThread()) {
			addHostProgressNonEvent(hostAndPort, hostThread);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					addHostProgressNonEvent(hostAndPort, hostThread);
				}
			});
		} catch (Exception e) {
		}
	}

	void removeHostProgress(final String hostAndPort) {
		if (EventQueue.isDispatchThread()) {
			removeHostProgressNonEvent(hostAndPort);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					removeHostProgressNonEvent(hostAndPort);
				}
			});
		} catch (Exception e) {
		}

	}

	void updateHostProgress(final String hostAndPort, final String msg,
			final int percentage) {
		if (EventQueue.isDispatchThread()) {
			updateHostProgressNonEvent(hostAndPort, msg, percentage);
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					updateHostProgressNonEvent(hostAndPort, msg, percentage);
				}
			});
		} catch (Exception e) {
		}

	}

	private void addHostProgressNonEvent(String hostAndPort, HostProcess hostThread) {
		HostProgressMeter meter = new HostProgressMeter();
		meter.setHostProcess(hostThread);
		meter.setName(hostAndPort);
		meter.getTxtHost().setText(hostAndPort);
		synchronized (getPaneProgress()) {
			getPaneProgress().add(meter);
			getPaneProgress().validate();
		}
	}

	private void removeHostProgressNonEvent(String hostAndPort) {
		HostProgressMeter meter = getMeter(hostAndPort);
		if (meter == null) {
			return;
		}
		synchronized (getPaneProgress()) {
			getPaneProgress().remove(meter);
			getPaneProgress().validate();

		}
	}

	private void updateHostProgressNonEvent(String hostAndPort,
			String testName, int percentage) {
		HostProgressMeter meter = getMeter(hostAndPort);
		if (meter == null) {
			return;
		}
		meter.setProgress(testName, percentage);

	}

	private HostProgressMeter getMeter(String hostAndPort) {
		synchronized (getPaneProgress()) {
			for (int i = 0; i < getPaneProgress().getComponentCount(); i++) {
				Component c = (Component) getPaneProgress().getComponent(i);
				if (c.getName().equals(hostAndPort)) {
					return (HostProgressMeter) c;
				}
			}
		}
		return null;
	}

	/**
	 * @param pluginScanner
	 *            The pluginScanner to set.
	 */
	public void setPluginScanner(ExtensionScanner pluginScanner) {
		this.pluginScanner = pluginScanner;
	}
}
