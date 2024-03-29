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
package org.parosproxy.paros.model;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.xml.sax.SAXException;

/**
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Model {

	private static Model model = null;

	private static final String DBNAME_TEMPLATE = Constant.DBNAME_TEMPLATE;
	private String DBNAME_UNTITLED = Constant.getInstance().DBNAME_UNTITLED;

	private Session session = null;
	private OptionsParam optionsParam = null;
	private Database db = null;
	private String currentDBNameUntitled = "";
	// ZAP: Added logger
	private Logger logger = Logger.getLogger(Model.class);
	
	

	public Model() {
		// make sure the variable here will not refer back to model itself.
		// DO it in init or respective getter.

		session = new Session(this);
		optionsParam = new OptionsParam();

	}

	/**
	 * @return Returns the optionsParam.
	 */
	public OptionsParam getOptionsParam() {
		if (optionsParam == null) {
			optionsParam = new OptionsParam();
		}
		return optionsParam;
	}

	/**
	 * @param optionsParam
	 *            The optionsParam to set.
	 */
	public void setOptionsParam(OptionsParam param) {
		optionsParam = param;
	}

	/**
	 * @return Returns the session.
	 */
	public Session getSession() {
		if (session == null) {
			session = new Session(this);
		}
		return session;
	}

	/**
	 * @param session
	 *            The session to set.
	 */
	public void setSession(Session paramSession) {
		session = paramSession;
	}

	public void init() throws SAXException, IOException, Exception {
		db = Database.getSingleton();

		createAndOpenUntitledDb();
		HistoryReference.setTableHistory(getDb().getTableHistory());
		// ZAP: Support for multiple tags
		HistoryReference.setTableTag(getDb().getTableTag());
		HistoryReference.setTableAlert(getDb().getTableAlert());
		getOptionsParam().load(Constant.getInstance().FILE_CONFIG);
	}

	public static Model getSingleton() {
		if (model == null) {
			model = new Model();
		}
		return model;
	}

	/**
	 * @return Returns the db.
	 */
	public Database getDb() {
		return db;
	}

	public void moveSessionDb(String destFile) throws Exception {

		// always use copySession because moving file does not work in Debian,
		// and for Windows renaming file acrossing different drives does not
		// work.

		copySessionDb(currentDBNameUntitled, destFile);

	}

	public void copySessionDb(String currentFile, String destFile) throws Exception {

		getDb().close(false);

		// copy session related files to the path specified
		FileCopier copier = new FileCopier();

		File fileIn1 = new File(currentFile + ".data");
		File fileOut1 = new File(destFile + ".data");
		copier.copy(fileIn1, fileOut1);

		File fileIn2 = new File(currentFile + ".script");
		File fileOut2 = new File(destFile + ".script");
		copier.copy(fileIn2, fileOut2);

		File fileIn3 = new File(currentFile + ".properties");
		File fileOut3 = new File(destFile + ".properties");
		copier.copy(fileIn3, fileOut3);

		File fileIn4 = new File(currentFile + ".backup");
		if (fileIn4.exists()) {
			File fileOut4 = new File(destFile + ".backup");
			copier.copy(fileIn4, fileOut4);
		}

		getDb().open(destFile);

	}

	public void createAndOpenUntitledDb() throws ClassNotFoundException, Exception {

		getDb().close(false);

		String timestamp = new java.text.SimpleDateFormat("ddMMyyyy-HHmmss").format(new java.util.Date(java.lang.System.currentTimeMillis()));
		// copy and create new template db
		currentDBNameUntitled = DBNAME_UNTITLED + "_" + timestamp;
		FileCopier copier = new FileCopier();
		File fileIn = new File(DBNAME_TEMPLATE + ".data");
		File fileOut = new File(currentDBNameUntitled + ".data");
		if (fileOut.exists() && ! fileOut.delete()) {
        	// ZAP: Log failure to delete file
	    	logger.error("Failed to delete file " + fileOut.getAbsolutePath());
	    }

		copier.copy(fileIn, fileOut);

		fileIn = new File(DBNAME_TEMPLATE + ".properties");
		fileOut = new File(currentDBNameUntitled + ".properties");
		if (fileOut.exists() && ! fileOut.delete()) {
        	// ZAP: Log failure to delete file
	    	logger.error("Failed to delete file " + fileOut.getAbsolutePath());
	    }

		copier.copy(fileIn, fileOut);

		fileIn = new File(DBNAME_TEMPLATE + ".script");
		fileOut = new File(currentDBNameUntitled + ".script");
		if (fileOut.exists() && ! fileOut.delete()) {
        	// ZAP: Log failure to delete file
	    	logger.error("Failed to delete file " + fileOut.getAbsolutePath());
	    }
		copier.copy(fileIn, fileOut);

		fileIn = new File(currentDBNameUntitled + ".backup");
		if (fileIn.exists()) {
			fileIn.delete();
		}

		getDb().open(currentDBNameUntitled);
	}

}
