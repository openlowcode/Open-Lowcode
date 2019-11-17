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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;

import java.util.logging.Logger;
import javafx.scene.Node;

import org.openlowcode.client.action.CActionData;
import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.runtime.PageActionManager.ActionSourceTransformer;

import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageError;
import org.openlowcode.tools.messages.MessageReader;

import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NiceFormatters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.Event;

import javafx.geometry.Insets;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Client managing a session to a given server.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientSession {

	private boolean techdetails = false;

	private ArrayList<ClientDisplay> displaysforconnection;
	private int activedisplayindex;
	private ConnectionToServer connectiontoserver;
	private ClientMainFrame mainframe;

	private ClientData clientdata;
	private PageBuffer pagebuffer;
	private String locale;
	private static String cid = null;

	private TabPane tabpane;

	private ActionSourceTransformer transformer;

	private String questionmarkicon;

	/**
	 * @return gets the locale on the machine
	 */
	public String getLocale() {
		return this.locale;
	}

	/**
	 * @return the tab pane of this client session. There is one child tab with
	 *         client sessions
	 */
	public TabPane getClientSessionNode() {
		return this.tabpane;
	}

	/**
	 * @return theconnection to server (holding the socket connection to the server)
	 */
	public ConnectionToServer getConnectionToServer() {
		return this.connectiontoserver;
	}

	/**
	 * sets the title on the active tab (corresponding to the active client display)
	 * 
	 * @param newtitle the text title to show
	 */
	public void setTitle(String newtitle) {
		Tab tab = this.tabpane.getTabs().get(activedisplayindex);
		tab.setText((newtitle.length() > 20 ? newtitle.substring(0, 20) + "..." : newtitle));
		tab.setTooltip(new Tooltip(newtitle));
	}

	/**
	 * sets the show tech details parameter (memory usage, speed of page display,
	 * network capacity used
	 * 
	 * @param techdetails true to show tech details, false else
	 */
	public void setShowTechDetails(boolean techdetails) {
		this.techdetails = techdetails;
	}

	/**
	 * creates a client session and sets up a display that will show the specified
	 * URL
	 * 
	 * @param mainframe        parent main frame of the application
	 * @param transformer      action source transformer for event handler
	 * @param urltoconnecto    first URL to connect to, null if not used
	 * @param questionmarkicon question mark icon URL
	 */
	public ClientSession(ClientMainFrame mainframe, ActionSourceTransformer transformer, String urltoconnecto,
			String questionmarkicon) {
		this.mainframe = mainframe;
		this.displaysforconnection = new ArrayList<ClientDisplay>();
		connectiontoserver = new ConnectionToServer();
		this.clientdata = new ClientData();
		this.pagebuffer = new PageBuffer();
		this.transformer = transformer;
		this.questionmarkicon = questionmarkicon;
		setupDisplay(new ClientDisplay(this, transformer, urltoconnecto, questionmarkicon));
		this.displaysforconnection.get(activedisplayindex).triggerLaunchAddress();
	}

	/**
	 * adds a new empty display
	 */
	public void addNewDisplay() {
		logger.info("Adding new display tab");
		setupDisplay(new ClientDisplay(this, transformer, null, questionmarkicon));
	}

	/**
	 * creates the tab pane if required, and adds a tab with the client display
	 * 
	 * @param clientdisplay the display to show
	 */
	private void setupDisplay(ClientDisplay clientdisplay) {
		// initiates the tabpane if called for the first time
		if (this.tabpane == null) {
			this.tabpane = new TabPane();
			this.tabpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			this.tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
			this.tabpane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
					int index = newValue.intValue();
					activedisplayindex = index;
					logger.warning(" --> Set active display index afterselection to " + index);
				}
			});
		}
		displayTab(clientdisplay);

	}

	private void displayTab(ClientDisplay clientdisplay) {
		logger.info("Adding display tab, new index = " + displaysforconnection.size());
		this.displaysforconnection.add(clientdisplay);
		this.activedisplayindex = displaysforconnection.size() - 1;
		Tab tab = new Tab();
		tab.setOnCloseRequest(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				try {
					Tab tab = (Tab) event.getSource();
					ObservableList<Tab> listoftabs = tabpane.getTabs();
					if (listoftabs.size() == 1) {
						// cannot close last tab
						event.consume();
						return;
					}
					int index = -1;
					logger.finer(" ----- ---- reviewing list of tabs , size = " + listoftabs.size());
					for (int i = 0; i < listoftabs.size(); i++) {
						logger.finer("comparing tab " + i + " : " + listoftabs.get(i) + "/" + listoftabs.get(i).getId()
								+ " with closing tab " + tab + "/" + tab.getId());
						if (listoftabs.get(i) == tab) {
							index = i;
							break;
						}

					}
					if (index == -1) {
						logger.finer("Did not find tab in tabpane");
						return;
					}
					displaysforconnection.remove(index);
					if (index == displaysforconnection.size()) {
						activedisplayindex = index - 1;
					} else {
						activedisplayindex = index;
					}
					tabpane.getSelectionModel().select(activedisplayindex);
					logger.warning(" --> Selecting active display after closure, index = " + activedisplayindex);
				} catch (Exception e) {
					logger.warning("Exception in close request listener " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						String element = e.getStackTrace()[i].toString();
						if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
							logger.warning(e.getStackTrace()[i].toString());
					}
				}
			}

		});

		Node clientdisplaynode = clientdisplay.getEmptyDisplay();
		if (clientdisplaynode instanceof Region) {
			Region region = (Region) clientdisplaynode;
			region.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, Color.RED, Color.RED, Color.RED,
					BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
					CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
			region.heightProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					tabpane.requestLayout();
					if (tabpane.getParent() != null)
						tabpane.getParent().requestLayout();

				}

			});
		}
		tab.setContent(clientdisplaynode);
		tab.setText("New Tab");
		this.tabpane.getTabs().add(tab);
		this.tabpane.getSelectionModel().select(activedisplayindex);
		logger.info("tab succesfully added");

	}

	/**
	 * @return parent mainframe
	 */
	public ClientMainFrame getMainFrame() {
		return mainframe;
	}

	private static Logger logger = Logger.getLogger(ClientSession.class.getName());

	/**
	 * @return the list of previous pages visited
	 */
	public ClientData getClientData() {
		return this.clientdata;
	}

	/**
	 * @param actionname       name of the action to launch
	 * @param modulename       nae of the module to launch
	 * @param local            true if the inline action is local
	 * @param actionattributes list of attributes for the inline action
	 * @param page             page to show the data coming back from the inline
	 *                         action
	 */
	public void sendInlineAction(String actionname, String modulename, boolean local, CActionData actionattributes,
			CPage page) {
		ClientDisplay activedisplay = displaysforconnection.get(activedisplayindex);

		long startaction = System.currentTimeMillis();
		this.setBusinessScreenFrozen(true);
		activedisplay.updateStatusBar("Send action to the server " + modulename + "." + actionname);
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					activedisplay.updateStatusBar("Connection Established with " + connectiontoserver.getServer() + " "
							+ connectiontoserver.getPort());
					connectiontoserver.sendMessage((writer) -> {
						writer.startNewMessage();
						writer.startStructure("REQUEST");
						writer.addStringField("CID", cid);
						writer.startStructure("INLINEACTION");
						writer.addStringField("NAME", actionname);
						writer.addStringField("MODULE", modulename);

						actionattributes.writeToMessage(writer);
						writer.endStructure("INLINEACTION"); // ACTION
						writer.endStructure("REQUEST"); // REQUEST
						writer.endMessage();
						writer.flushMessage();
					});

					DisplayPageFeedback exceptionmessage = enrichPageWithInlineData(page, activedisplay, startaction,
							techdetails, modulename, actionname);
					long endaction = System.currentTimeMillis();
					if (exceptionmessage.getErrormessage() != null) {

						activedisplay
								.updateStatusBar("Error message from server : " + exceptionmessage.getErrormessage()
										+ " - execution time (ms) : " + (endaction - startaction), true);

					}
					setBusinessScreenFrozen(false);
				} catch (Exception e) {
					treatException(e);
					setBusinessScreenFrozen(false);

				}
			}
		};
		thread.start();

	}

	/**
	 * sends an action that will result in the display of a new page
	 * 
	 * @param actionname       name of the action
	 * @param modulename       name of the module
	 * @param actionattributes attributes that will be sent to the server for the
	 *                         action
	 * @param openinnewtab     true if the new page should be opened in a new tab,
	 *                         keeping the current page in the current tab
	 */
	public void sendaction(String actionname, String modulename, CActionData actionattributes, boolean openinnewtab) {
		ClientDisplay activedisplay = displaysforconnection.get(activedisplayindex);

		long startaction = System.currentTimeMillis();
		this.setBusinessScreenFrozen(true);
		activedisplay.updateStatusBar("Send action to the server " + modulename + "." + actionname);
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					activedisplay.updateStatusBar("Connection Established with " + connectiontoserver.getServer() + " "
							+ connectiontoserver.getPort());
					connectiontoserver.sendMessage((writer) -> {
						writer.startNewMessage();
						writer.startStructure("REQUEST");
						writer.addStringField("CID", cid);
						writer.startStructure("ACTION");
						writer.addStringField("NAME", actionname);
						writer.addStringField("MODULE", modulename);
						pagebuffer.writeBufferedPages(modulename, actionname, writer);
						actionattributes.writeToMessage(writer);
						writer.endStructure("ACTION"); // ACTION

						writer.endStructure("REQUEST"); // REQUEST
						writer.endMessage();
						writer.flushMessage();
					});

					DisplayPageFeedback exceptionmessage = displayPage(activedisplay, startaction, techdetails, null,
							null, openinnewtab);
					long endaction = System.currentTimeMillis();
					if (exceptionmessage.getErrormessage() != null) {

						activedisplay
								.updateStatusBar("Error message from server : " + exceptionmessage.getErrormessage()
										+ " - execution time (ms) : " + (endaction - startaction), true);

					}

				} catch (Exception e) {
					treatException(e);
					setBusinessScreenFrozen(false);

				}
			}
		};
		thread.start();

	}

	/**
	 * displays in the current tab the page with the address specified
	 * 
	 * @param address the address to show
	 * @param back    true if back is possible
	 */
	public void sendLink(String address, boolean back) {
		try {
			ClientDisplay activedisplay = displaysforconnection.get(activedisplayindex);
			boolean confirmcontinue = activedisplay.checkContinueWarning();
			if (confirmcontinue) {
				long startaction = System.currentTimeMillis();
				setBusinessScreenFrozen(true);
				activedisplay.updateStatusBar("requested connection to the address " + address);
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							String application = connectiontoserver.connectToAddressAndGetApplication(address);
							activedisplay.updateStatusBar("Connection Established with "
									+ connectiontoserver.getServer() + " " + connectiontoserver.getPort());
							connectiontoserver.sendMessage((writer) -> {
								writer.startNewMessage();
								writer.startStructure("REQUEST");
								if (cid != null)
									writer.addStringField("CID", cid);
								writer.startStructure("CLINK");
								writer.addStringField("VALUE", application);
								writer.endStructure("CLINK");
								writer.addStringField("CVR", mainframe.getClientversion());
								writer.endStructure("REQUEST");

								writer.endMessage();
								writer.flushMessage();
							});

							DisplayPageFeedback exceptionmessage = displayPage(activedisplay, startaction, techdetails,
									null, null, false);
							long endaction = System.currentTimeMillis();
							if (exceptionmessage.getErrormessage() != null) {

								activedisplay
										.updateStatusBar(
												"Error message from server : " + exceptionmessage.getErrormessage()
														+ " - execution time (ms) : " + (endaction - startaction),
												true);

							}

						} catch (Exception e) {
							treatException(e);
							setBusinessScreenFrozen(false);

						}
					}
				};
				thread.start();
			}
		} catch (Exception e) {
			treatException(e);
			setBusinessScreenFrozen(false);

		}
	}

	/**
	 * @param page            current page
	 * @param activedisplay   client display for the action
	 * @param starttime       start time of the interaction with the server (for
	 *                        performance audit purposes)
	 * @param showtechdetails true if all technical details (memory, speed of
	 *                        request, network bandwidth used)
	 * @param module          name of the module
	 * @param action          name of the line action
	 * @return a feedback if processing happened well or not
	 * @throws IOException        if any network breadkown
	 * @throws OLcRemoteException if anythink bad happened on the server during the
	 *                            request
	 */
	public DisplayPageFeedback enrichPageWithInlineData(CPage page, ClientDisplay activedisplay, long starttime,
			boolean showtechdetails, String module, String action) throws IOException, OLcRemoteException {
		MessageReader reader = connectiontoserver.getReader();
		MessageElement startelement = reader.getNextElement();
		if (startelement instanceof MessageError) {
			MessageError messageerror = (MessageError) startelement;
			DisplayPageFeedback feedback = new DisplayPageFeedback(
					messageerror.getErrorcode() + ":" + messageerror.getErrormessage(),
					reader.charcountsinceStartMessage(), null);
			return feedback;
		}
		activedisplay.updateStatusBar("receives INLINEDATA for action = " + action + ", starting reading");
		reader.returnNextStartStructure("INLINEDATA");
		CPageData newdata = new CPageData(reader);
		reader.returnNextEndStructure("INLINEDATA");
		reader.returnNextEndMessage();
		String extrastatusmessage = "";
		if (newdata.getMessage() != null)
			if (newdata.getMessage().length() > 0)
				if (!newdata.isPopup()) {
					extrastatusmessage = " - " + newdata.getMessage();
				}

		activedisplay.updateStatusBar(
				"received INLINE DATA from SERVER, and read data fully name = " + action + extrastatusmessage);

		page.processInlineAction(module, action, newdata);
		long endaction = System.currentTimeMillis();
		if (showtechdetails) {
			activedisplay.updateStatusBar("Executed inline action, execution time (ms) : " + (endaction - starttime)
					+ ", data read (ch): " + NiceFormatters.formatNumber(reader.charcountsinceStartMessage()) + " "
					+ ClientTools.memoryStatement() + extrastatusmessage);
		} else {
			activedisplay.updateStatusBar("Executed inline action " + extrastatusmessage);

		}
		return new DisplayPageFeedback(null, reader.charcountsinceStartMessage(), null);
	}

	/**
	 * @param activedisplay   current display
	 * @param starttime       start time of the exchange with the server
	 * @param showtechdetails true to show tech details
	 * @param module          name of the module
	 * @param action          name of the action
	 * @param openinnewtab    true to open in new tabs
	 * @return a feedback
	 * @throws IOException        if any network breadkown
	 * @throws OLcRemoteException if anythink bad happened on the server during the
	 *                            request
	 */
	public DisplayPageFeedback displayPage(ClientDisplay activedisplay, long starttime, boolean showtechdetails,
			String module, String action, boolean openinnewtab) throws IOException, OLcRemoteException {
		MessageReader reader = connectiontoserver.getReader();
		logger.info("starts displaying page in thread " + Thread.currentThread().getId() + " for " + module + "."
				+ action + " with openinnewtab=" + openinnewtab);
		MessageElement startelement = reader.getNextElement();
		if (startelement instanceof MessageError) {
			MessageError messageerror = (MessageError) startelement;
			DisplayPageFeedback feedback = new DisplayPageFeedback(
					messageerror.getErrorcode() + ":" + messageerror.getErrormessage(),
					reader.charcountsinceStartMessage(), null);
			return feedback;
		}
		activedisplay.updateStatusBar("receives message from server, starting reading");

		String message = reader.returnNextStartStructure();
		// ---------------------------- NORMAL CASE 1 DISPLAY PAGE
		// ------------------------------------------------

		if (message.compareTo("DISPLAYPAGE") == 0) {
			// processing cid
			cid = reader.returnNextStringField("CID");
			// --- locale from the user, id defined

			locale = reader.returnNextStringField("LCL");
			// -- processing display page
			String name = reader.returnNextStringField("NAME");
			MessageElement element = reader.getNextElement();
			if (!(element instanceof MessageStringField))
				throw new RuntimeException("was expecting a String Field called ADDRESS or TITLE, GOT a"
						+ element.getClass().getName() + " drop =  " + element.toString());
			MessageStringField stringelement = (MessageStringField) element;
			String address = null;
			String title = null;
			if (stringelement.getFieldName().compareTo("ADDRESS") == 0) {
				address = stringelement.getFieldcontent();
				title = reader.returnNextStringField("TITLE");
			} else {
				if (stringelement.getFieldName().compareTo("TITLE") == 0) {
					title = stringelement.getFieldcontent();
				} else {
					throw new RuntimeException("Expected title, got " + stringelement.getFieldName());
				}

			}
			logger.info("displaying page title = " + title);

			String fulladdress = null;
			if (address != null) {
				fulladdress = connectiontoserver.completeAddress(address);

				this.clientdata.addAddress(fulladdress, title);

			} else {
				this.clientdata.addAddress("", title);
			}

			CPage page = new CPage(name, reader, module, action, this.pagebuffer);
			String pagestring = null;
			if (page.getPagedescription() != null)
				pagestring = "{\n" + page.getPagedescription() + "\n}\n";

			reader.returnNextEndStructure("DISPLAYPAGE");
			reader.returnNextEndMessage();
			logger.finer("finishes parsing");

			activedisplay.setandDisplayPage(title, fulladdress, page, address, starttime,
					reader.charcountsinceStartMessage(), page.getBufferedDataUsed() / 1024,
					this.pagebuffer.getTotalBufferSize() / 1024, showtechdetails, openinnewtab);

			return new DisplayPageFeedback(null, reader.charcountsinceStartMessage(), pagestring);
		}
		// ---------------------------- EXCEPTION CASE 1 - DISPLAY SERVER ERROR
		// ------------------------------------------------

		if (message.compareTo("SERVERERROR") == 0) {
			String errorcode = reader.returnNextStringField("ERC");
			String errormessage = reader.returnNextStringField("ERM");
			reader.returnNextEndStructure("SERVERERROR");
			reader.returnNextEndMessage();
			return new DisplayPageFeedback("Server Error : " + errorcode + " - " + errormessage,
					reader.charcountsinceStartMessage(), null);
		}
		// ---------------------------- EXCEPTION CASE 2 - CLIENT VERSION ERROR
		// ------------------------------------------------

		if (message.compareTo("CLIENTUPDATE") == 0) {

			String clientversion = reader.returnNextStringField("CLV");
			String serverversion = reader.returnNextStringField("SVV");
			Date serverversiondate = reader.returnNextDateField("SVD");
			reader.returnNextEndStructure("CLIENTUPDATE");
			reader.returnNextEndMessage();
			CPage clientupgradepage = mainframe.getClientUpgradePage(clientversion, serverversion, serverversiondate);

			activedisplay.setandDisplayPage("Client Upgrade", null, clientupgradepage,
					this.getActiveClientDisplay().getConnectionBar().getAddress().getText(), starttime,
					reader.charcountsinceStartMessage(), 0, pagebuffer.getTotalBufferSize(), showtechdetails, false);

			return new DisplayPageFeedback(null, reader.charcountsinceStartMessage(), null);
		}

		return new DisplayPageFeedback(null, reader.charcountsinceStartMessage(), null);

	}

	/**
	 * @return the active display
	 */
	public ClientDisplay getActiveClientDisplay() {
		if (this.displaysforconnection == null)
			throw new RuntimeException("Displays for connection not initialized");
		if ((activedisplayindex < 0) || (activedisplayindex >= this.displaysforconnection.size()))
			throw new RuntimeException("Getting active client display with index out or range, index = "
					+ activedisplayindex + ", tabs stored = " + this.displaysforconnection.size());
		logger.finest("Get Display For Connection: " + this.activedisplayindex + " - "
				+ this.displaysforconnection.get(this.activedisplayindex));
		return this.displaysforconnection.get(this.activedisplayindex);
	}

	/**
	 * Logs and displays on status bar an error
	 * 
	 * @param e exceptin to tread
	 */
	public void treatException(Exception e) {
		if (e instanceof OLcRemoteException) {
			OLcRemoteException remoteexception = (OLcRemoteException) e;
			this.getActiveClientDisplay().updateStatusBar(
					"Server Error :" + remoteexception.getRemoteErrorCode() + " " + remoteexception.getMessage(), true);
		} else {
			this.getActiveClientDisplay().updateStatusBar("Connectionerror " + e.getMessage(), true);
		}
		printException(e);
		try {
			this.connectiontoserver.stopConnection();
		} catch (IOException e2) {
			// will not print exception in screen as first exception is likely more useful
			// for user to figure out
			logger.warning(" Error in stopping connection " + e2.getMessage());
			for (int i = 0; i < e2.getStackTrace().length; i++) {
				logger.warning(e2.getStackTrace()[i].toString());
			}
		}
	}

	public static void printException(Exception e) {
		int i = 0;
		boolean exit = false;
		logger.warning("<!!!> Exception " + e.getClass().getName() + " - " + e.getMessage());
		while (!exit) {
			logger.warning(e.getStackTrace()[i].toString());
			if (e.getStackTrace()[i].toString().indexOf("GalliumClientConnection") != -1)
				exit = true; // only show stack trace until mangager
			i++;
			if (i >= e.getStackTrace().length)
				exit = true;

		}
	}

	/**
	 * @param frozen true if widgets should be frozen as one communication is
	 *               already happening in the server
	 */
	public void setBusinessScreenFrozen(boolean frozen) {
		// closing message if required (if error while sending the message)
		connectiontoserver.resetSendingMessage();
		// making connection bar active again
		for (int i = 0; i < this.displaysforconnection.size(); i++)
			displaysforconnection.get(i).setBusinessScreenFrozen(frozen);
		;

	}

	/**
	 * Stops the current connection
	 */
	public void stopconnection() {

		try {
			this.connectiontoserver.stopConnection();
		} catch (IOException e) {
			int i = 0;
			logger.warning("caught IO Exception " + e.getMessage());
			boolean exit = false;
			while (!exit) {
				logger.warning(e.getStackTrace()[i].toString());
				// only show stack trace until current class to get rid of the
				// very long javafx stacktrace
				if (e.getStackTrace()[i].toString().indexOf("ClientSession") != -1)
					exit = true;
				i++;
				if (i >= e.getStackTrace().length)
					exit = true;

			}
			this.getActiveClientDisplay().updateStatusBar("Connectionerror " + e.getMessage());

			setBusinessScreenFrozen(false);
		}

	}

}