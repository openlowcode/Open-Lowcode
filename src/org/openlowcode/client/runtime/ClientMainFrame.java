/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageNodeCatalog;
import org.openlowcode.client.runtime.PageActionManager.ActionSourceTransformer;
import org.openlowcode.tools.misc.OpenLowcodeLogFilter;
import org.openlowcode.tools.trace.ConsoleFormatter;
import org.openlowcode.tools.trace.FileFormatter;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * the main class of the client. It manages the main JAVAFX stage of the
 * application, the logging, and the management of the client automatic upgrader
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientMainFrame {

	private static Logger logger = Logger.getLogger(ClientMainFrame.class.getName());
	private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
	/**
	 * The client upgrade page generator is a page shown when client has an older
	 * client to connect to the server
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	@FunctionalInterface
	public interface ClientUpgradePageGenerator {

		/**
		 * @param clientversion
		 * @param serverversions
		 * @param serverupdatedate
		 * @return
		 */
		public CPage generateClientUpgradePage(String clientversion, String serverversions, Date serverupdatedate);
	}

	private Stage stage;

	/**
	 * @return the primary JAVAFX stage to the application
	 */
	public Stage getPrimaryStage() {
		return this.stage;
	}

	private OpenLowcodeLogFilter consolefilter;
	private OpenLowcodeLogFilter logfilefilter;

	private boolean nolog;

	/**
	 * @return the console log filter
	 */
	public OpenLowcodeLogFilter getConsoleFilter() {
		return consolefilter;

	}

	/**
	 * @return the log file filter
	 */
	public OpenLowcodeLogFilter getLogFileFilter() {
		return logfilefilter;
	}

	/**
	 * @param clientversion    current client version
	 * @param serverversions   current server version
	 * @param serverupdatedate date of the last update of the Open Lowcode framework
	 *                         on the server
	 * @return
	 */
	public CPage getClientUpgradePage(String clientversion, String serverversions, Date serverupdatedate) {
		return this.clientupgradepage.generateClientUpgradePage(clientversion, serverversions, serverupdatedate);
	}

	private ClientUpgradePageGenerator clientupgradepage;
	private String clientversion;
	private Date clientversiondate;

	private ClientSession uniqueclientsession;

	/**
	 * @return client version
	 */
	public String getClientversion() {
		return clientversion;
	}

	/**
	 * @return date the used client version was compiled
	 */
	public Date getClientversiondate() {
		return clientversiondate;
	}

	/**
	 * Creates a client mainframe and showing it
	 * 
	 * @param stage                   javafx application stage
	 * @param clientversion           version of the client
	 * @param clientversiondate       date of the version of the client
	 * @param clientupgradepage       the page to show in case of upgrade
	 * @param actionsourcetransformer transformer of widgets sending events to
	 *                                action event to their significant parent (e.g.
	 *                                tablecell -> tableview)
	 * @param pagenodecatalog         list of CPageNodes managed
	 * @param urltoconnectto          URL to connect to at start
	 * @param nolog                   no logs are shown in the server
	 * @param smallicon               URL to the small icon (32x32)
	 * @param bigicon                 URL to the big icon (64x64)
	 * @param questionmarkicon        URL of the question mark uicon
	 * @param cssfile                 URL to the CSS file
	 * @throws IOException if any bad happens while setting up the logs
	 */
	public ClientMainFrame(Stage stage, String clientversion, Date clientversiondate,
			ClientUpgradePageGenerator clientupgradepage, ActionSourceTransformer actionsourcetransformer,
			CPageNodeCatalog pagenodecatalog, String urltoconnectto, boolean nolog, String smallicon, String bigicon,
			String questionmarkicon, String cssfile) throws IOException {
		this.nolog = nolog;
		this.stage = stage;
		this.clientversion = clientversion;
		this.clientversiondate = clientversiondate;
		this.clientupgradepage = clientupgradepage;

		CPageNode.setPageCatelog(pagenodecatalog);
		initiateLog();
		logger.severe("--------------- * * * Open Lowcode client * * * ---------------");
		logger.severe(" * * version="+clientversion+", client built date="+sdf.format(clientversiondate)+"* *");
		// ---------------------------------------- initiates the first tab
		uniqueclientsession = new ClientSession(this, actionsourcetransformer, urltoconnectto, questionmarkicon);
		if (smallicon != null)
			stage.getIcons().add(new Image(smallicon));
		if (bigicon != null)
			stage.getIcons().add(new Image(bigicon));
		Scene scene = new Scene(this.uniqueclientsession.getClientSessionNode(), Color.WHITE);
		if (cssfile != null)
			scene.getStylesheets().add(cssfile);
		stage.setScene(scene);
		stage.setTitle("Open Lowcode client");
		// ---------------------------------------- show the stage
		stage.setMaximized(true);
		this.stage.show();
	}

	/**
	 * Initiate console log and file log
	 * 
	 * @throws IOException if anything bad happens creating log giles
	 */
	public void initiateLog() throws IOException {
		Logger mainlogger = Logger.getLogger("");

		for (int i = 0; i < mainlogger.getHandlers().length; i++) {
			mainlogger.removeHandler(mainlogger.getHandlers()[i]);
		}
		if (!nolog) {
			ConsoleHandler consolehandler = new ConsoleHandler();
			consolehandler.setFormatter(new ConsoleFormatter());
			consolehandler.setLevel(Level.ALL);
			consolefilter = new OpenLowcodeLogFilter(Level.FINER, "Console Filter", consolehandler);
			consolehandler.setFilter(consolefilter);
			mainlogger.addHandler(consolehandler);
			File file = new File("./log/");
			if (!file.exists()) {
				boolean result = file.mkdir();
				if (!result)
					throw new RuntimeException("Trying to create log folder " + file.getPath() + ", does not work");
			}
			System.err.println("log folder = " + file.getAbsolutePath());
			FileHandler logfilehandler = new FileHandler("./log/OLcClient%g.log", 10000000, 1000, true);
			logfilefilter = new OpenLowcodeLogFilter(Level.FINER, "Log File Filter", logfilehandler);
			logfilehandler.setFilter(logfilefilter);

			logfilehandler.setLevel(Level.ALL);
			logfilehandler.setFormatter(new FileFormatter(true));
			mainlogger.addHandler(logfilehandler);

			mainlogger.setUseParentHandlers(false);
			
			Logger rootlogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			for (int i = 0; i < rootlogger.getHandlers().length; i++) {
				rootlogger.removeHandler(rootlogger.getHandlers()[i]);
			}
			rootlogger.addHandler(logfilehandler);
			rootlogger.setLevel(Level.ALL);

			rootlogger.addHandler(consolehandler);
			rootlogger.setUseParentHandlers(false);
		}
	}

}
