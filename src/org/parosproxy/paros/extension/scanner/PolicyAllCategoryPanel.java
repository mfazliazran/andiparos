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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.view.AbstractParamPanel;

/**
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class PolicyAllCategoryPanel extends AbstractParamPanel {

	private static final long serialVersionUID = -8853878314057946195L;

	private JTable tableTest = null;
	private JScrollPane jScrollPane = null;
	private AllCategoryTableModel allCategoryTableModel = null;

	/**
     * 
     */
	public PolicyAllCategoryPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

		this.setLayout(new GridBagLayout());
		this.setSize(375, 204);
		this.setName("categoryPanel");
		gridBagConstraints11.weightx = 1.0;
		gridBagConstraints11.weighty = 1.0;
		gridBagConstraints11.fill = GridBagConstraints.BOTH;
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.gridy = 1;
		gridBagConstraints11.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints11.gridwidth = 2;
		gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.insets = new Insets(2, 2, 2, 2);
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.insets = new Insets(2, 2, 2, 2);
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
		this.add(getBtnEnableAll(), gridBagConstraints1);
		this.add(getBtnDisableAll(), gridBagConstraints2);
		this.add(getJScrollPane(), gridBagConstraints11);
	}

	private static final int width[] = { 300, 50 };
	private JButton btnEnableAll = null;
	private JButton btnDisableAll = null;

	/**
	 * This method initializes tableTest
	 * 
	 * @return JTable
	 */
	private JTable getTableTest() {
		if (tableTest == null) {
			tableTest = new JTable();
			tableTest.setModel(getAllCategoryTableModel());
			tableTest.setRowHeight(18);
			tableTest.setIntercellSpacing(new Dimension(1, 1));
			for (int i = 0; i < 2; i++) {
				TableColumn column = tableTest.getColumnModel().getColumn(i);
				column.setPreferredWidth(width[i]);
			}
		}
		return tableTest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.proofsecure.paros.view.AbstractParamPanel#initParam(java.lang.Object)
	 */
	public void initParam(Object obj) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.proofsecure.paros.view.AbstractParamPanel#validateParam(java.lang
	 * .Object)
	 */
	public void validateParam(Object obj) throws Exception {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.proofsecure.paros.view.AbstractParamPanel#saveParam(java.lang.Object)
	 */
	public void saveParam(Object obj) throws Exception {

	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableTest());
			jScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes categoryTableModel
	 * 
	 * @return com.proofsecure.paros.plugin.scanner.CategoryTableModel
	 */
	AllCategoryTableModel getAllCategoryTableModel() {
		if (allCategoryTableModel == null) {
			allCategoryTableModel = new AllCategoryTableModel();
			allCategoryTableModel.setTable(PluginFactory.getAllPlugin());
		}
		return allCategoryTableModel;
	}

	/**
	 * This method initializes btnEnableAll
	 * 
	 * @return JButton
	 */
	private JButton getBtnEnableAll() {
		if (btnEnableAll == null) {
			btnEnableAll = new JButton();
			btnEnableAll.setText("Enable All");
			btnEnableAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getAllCategoryTableModel().setAllCategoryEnabled(true);
				}
			});
		}
		return btnEnableAll;
	}

	/**
	 * This method initializes btnDisableAll
	 * 
	 * @return JButton
	 */
	private JButton getBtnDisableAll() {
		if (btnDisableAll == null) {
			btnDisableAll = new JButton();
			btnDisableAll.setText("Disable All");
			btnDisableAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getAllCategoryTableModel().setAllCategoryEnabled(false);
				}
			});
		}
		return btnDisableAll;
	}
}
