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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.runtime.PageActionManager.ActionSourceTransformer;
import javafx.scene.input.MouseEvent;

import javafx.event.EventHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * One tab display of the navigator
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientDisplay {
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private ClientSession parent;
	private PageActionManager pageactionmanager;
	private CPage currentpage;
	private String previousaddress;
	private ScrollBar contentholderverticalscrollbar;
	private ScrollBar contentholderhorizontalscrollbar;
	private ScrollPane contentholder;
	private String urltoconnectto;
	private ArrayList<UserInteractionWidget> userinteractionwidgets;
	private ConnectionBar connectionbar;
	private Label statuslabel;
	private String questionmarkicon;

	/**
	 * @return the connection bar of the client display
	 */
	public ConnectionBar getConnectionBar() {
		return this.connectionbar;
	}

	/**
	 * scrolls the display to make sure the node specified is shown in the scroll pane
	 * @param node a node displayed
	 */
	public void ensureNodeVisible(Node node) {
		if (contentholder != null) {
			Bounds viewport = contentholder.getViewportBounds();
			double contentHeight = contentholder.getContent()
					.localToScene(contentholder.getContent().getBoundsInLocal()).getHeight();
			double nodeMinY = node.localToScene(node.getBoundsInLocal()).getMinY();
			double nodeMaxY = node.localToScene(node.getBoundsInLocal()).getMaxY();
			double scrollpaneMinY = contentholder.localToScene(contentholder.getBoundsInLocal()).getMinY();
			double scrollpaneMaxY = contentholder.localToScene(contentholder.getBoundsInLocal()).getMaxY();
			double vValueDelta = 0;
			double vValueCurrent = contentholder.getVvalue();
			logger.finest("nodeMinY=" + nodeMinY + ", nodeMaxY=" + nodeMaxY + ", vValueCurrent=" + vValueCurrent
					+ ", contentHeight=" + contentHeight);
			logger.finest("scrollpaneMinY=" + scrollpaneMinY + ", scrollpaneMaxY=" + scrollpaneMaxY);

			logger.finest("viewport.height=" + viewport.getHeight() + ", viewport.minY=" + viewport.getMinY()
					+ ", viewport.maxY=" + viewport.getMaxY());
			if (nodeMinY < scrollpaneMinY) {
				logger.finest(" --- **** out UP");
				// currently located above (remember, top left is (0,0))
				vValueDelta = (nodeMinY - scrollpaneMinY) / (contentHeight - viewport.getHeight());

			} else if (nodeMaxY > scrollpaneMaxY) {
				logger.finest(" --- **** out DOWN");
				vValueDelta = (nodeMaxY - scrollpaneMaxY) / (contentHeight - viewport.getHeight());
		
			}
			contentholder.setVvalue(vValueCurrent + vValueDelta);
		}
	}

	/**
	 * Creates a new client display. Note that this will just create the class and 
	 * @param parent parent client session
	 * @param actionsourcetransformer specific transformer to transform a node sending an action (for example a table cell) into the parent node holding thedata (e.g. a table view) 
	 * @param urltoconnectto if specified, the URL to connect to first
	 * @param questionmarkicon the path to the icon to display for the question mark 
	 */
	public ClientDisplay(ClientSession parent, ActionSourceTransformer actionsourcetransformer, String urltoconnectto,
			String questionmarkicon) {
		this.parent = parent;
		this.pageactionmanager = new PageActionManager(this, actionsourcetransformer);
		this.urltoconnectto = urltoconnectto;
		this.userinteractionwidgets = new ArrayList<UserInteractionWidget>();
		this.lastpages = new ArrayList<PageHistory>();
		this.questionmarkicon = questionmarkicon;
	}

	private final static Logger logger = Logger.getLogger(ClientDisplay.class.getName());

	/**
	 * @return displays potential warning popups if some unsaved data is present on the page
	 */
	public boolean checkContinueWarning() {
		logger.fine("  -- calling check warning with null arguments");
		UnsavedDataWarning warning = pageactionmanager.checkwarnings((CPageAction) null);
		if (warning == null)
			return true;
		return displayModalPopup(warning.getMessage(), warning.getContinuemessage(), warning.getStopmessage());
	}

	/**
	 * @return the parent ClientSession
	 */
	public ClientSession getParentServerConnection() {
		return parent;
	}

	/**
	 * displays a modal popup and waits for answer
	 * 
	 * @param message what to display as main message
	 * @param yesmessage what to display on button for yes
	 * @param nomessage what to display on button for no
	 * @return true if answer is yes, false if answer of the popup is false
	 */
	public boolean displayModalPopup(String message, String yesmessage, String nomessage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Unsaved data warning");
		alert.setContentText("There is unsaved data. Do you want to continue and lose data ?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK)
			return true;
		return false;
	}

	/**
	 * Displays a page on this client display. Note: can be called outside of the Javafx Application thread
	 * @param title title to display
	 * @param fulladdress address to display in theconnection bar
	 * @param page the page to show
	 * @param address limited address (without server coordinates)
	 * @param starttime when the processing of the request started
	 * @param messagesize size of the message read from server
	 * @param pagedatachedkb page data that was retrieved from the cache
	 * @param totalpagecachekb total size of the page cache
	 * @param showtechdetails will display all technical details (speed,network used) 
	 * @param createnewtab if true, create a new tab to display the page
	 */
	public void setandDisplayPage(String title, String fulladdress, CPage page, String address, long starttime,
			long messagesize, long pagedatachedkb, long totalpagecachekb, boolean showtechdetails,
			boolean createnewtab) {

		Platform.runLater(new Thread() {

			@Override
			public void run() {

				try {

					if (createnewtab) {

						// if needed, create new tab, and launch display on new tab. Note:
						// this is not beautiful, but allows
						// to execute everything in the application thread
						parent.addNewDisplay();
						parent.getActiveClientDisplay().setandDisplayPage(title, fulladdress, page, address, starttime,
								messagesize, pagedatachedkb, totalpagecachekb, showtechdetails, false);
						return;
					}
					pageactionmanager.reset();
					pageactionmanager.setPage(page);
					final CPage previouspage = currentpage;
					currentpage = page;
					long endservertalk = new Date().getTime();
					long serverconnection = endservertalk - starttime;
					boolean firstshow = showtechdetails;
					if (serverconnection > 5000)
						firstshow = true;
					if (firstshow) {
						updateStatusBar(
								"Received message from server succesfully, start drawing new page. server time (ms) : "
										+ (serverconnection) + ", data read (ch): " + (messagesize / 1024) + "KB");
					} else {
						updateStatusBar("Received message from server succesfully, start drawing new page");

					}
					if (previousaddress != null)
						if (previousaddress.length() > 0)
							if (contentholderverticalscrollbar != null)
								if (contentholderverticalscrollbar.isVisible()) {
									double scrollheight = contentholderverticalscrollbar.getValue()
											* contentholderverticalscrollbar.getHeight();
									addPageHistory(new PageHistory(previousaddress, scrollheight));

								}
					previousaddress = address;

					if (title != null)
						parent.setTitle(title);
					if (fulladdress != null)
						connectionbar.setPageAddress(fulladdress);

					Node pagenode = page.getNode().getNode(pageactionmanager, page.getAllInputData(),
							parent.getMainFrame().getPrimaryStage());
					VBox nodeb = new VBox();
					nodeb.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
					VBox.setMargin(pagenode, new Insets(8, 5, 5, 18));
					nodeb.getChildren().add(pagenode);

					AnchorPane backgroundpane = new AnchorPane();
					AnchorPane.setTopAnchor(nodeb, 10.0);
					AnchorPane.setLeftAnchor(nodeb, 10.0);
					backgroundpane.getChildren().add(nodeb);
					backgroundpane.setPadding(new Insets(7, 7, 7, 7));
					backgroundpane.setBackground(new Background(
							new BackgroundFill(Color.WHITE, new CornerRadii(5), new Insets(5, 5, 5, 5))));
					backgroundpane.setBorder(new Border(new BorderStroke(Color.web("#17184B"), BorderStrokeStyle.SOLID,
							new CornerRadii(5), BorderWidths.DEFAULT, new Insets(5, 5, 5, 5))));
					for (Node node : contentholder.getChildrenUnmodifiable()) {
						if (node instanceof ScrollBar) {
							ScrollBar thisscrollbar = (ScrollBar) node;
							if (thisscrollbar.getOrientation() == Orientation.HORIZONTAL) {
								contentholderhorizontalscrollbar = thisscrollbar;
								contentholderhorizontalscrollbar.setValue(contentholderhorizontalscrollbar.getMin());
							} else {
								contentholderverticalscrollbar = thisscrollbar;
							}
						}
					}

					contentholder.requestLayout();
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							try {
								double oldvalueinpixel = 0;
								if (contentholderverticalscrollbar != null)
									if (contentholderverticalscrollbar.isVisible()) {
										PageHistory pagehistory = lookupPageForAddress(address);
										if (pagehistory != null)
											oldvalueinpixel = pagehistory.scrollheight;
										logger.fine(" --- calculated old value in pixel = " + oldvalueinpixel
												+ " for address = " + address);
									}
								if (oldvalueinpixel != 0) {
									double verticalscrollvalue = oldvalueinpixel
											/ contentholderverticalscrollbar.getHeight()
											* contentholderverticalscrollbar.getMax();
									contentholderverticalscrollbar.setValue(verticalscrollvalue);
									logger.fine(" vertical scrollbar set to " + verticalscrollvalue
											+ " , value after change =  " + contentholderverticalscrollbar.getValue());
								} else {
									contentholderverticalscrollbar.setValue(contentholderverticalscrollbar.getMin());
								}

							} catch (Exception e) {
								logger.warning(
										"Exception while treating specific thread for contentholder scroollbars");
								logger.warning("Error in displaying page " + e.getMessage());
								for (int i = 0; i < e.getStackTrace().length; i++) {
									String element = e.getStackTrace()[i].toString();
									if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
										logger.warning(e.getStackTrace()[i].toString());
								}
							}
						}
					});

					if (contentholderverticalscrollbar.isVisible()) {
						backgroundpane
								.setPrefWidth(contentholder.getWidth() - 2 - contentholderverticalscrollbar.getWidth());

					} else {
						backgroundpane.setPrefWidth(contentholder.getWidth() - 2);

					}

					if (contentholderhorizontalscrollbar.isVisible()) {
						backgroundpane.setPrefHeight(
								contentholder.getHeight() - 2 - contentholderhorizontalscrollbar.getHeight());

					} else {
						backgroundpane.setPrefHeight(contentholder.getHeight() - 2);

					}

					setnewContentHolderWidthChangeListener(new ChangeListener<Number>() {

						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldvalue,
								Number newvalue) {
							if (contentholderverticalscrollbar.isVisible()) {
								backgroundpane.setPrefWidth(
										newvalue.doubleValue() - 2 - contentholderverticalscrollbar.getWidth());
							} else {

								backgroundpane.setPrefWidth(newvalue.doubleValue() - 2);
							}

						}

					});

					setnewContentHolderHeightChangeListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> observable, Number oldvalue,
								Number newvalue) {
							if (contentholderhorizontalscrollbar.isVisible()) {
								backgroundpane.setPrefHeight(
										newvalue.doubleValue() - 2 - contentholderhorizontalscrollbar.getHeight());

							} else {
								backgroundpane.setPrefHeight(newvalue.doubleValue() - 2);

							}

						}
					});

					DropShadow ds = new DropShadow();
					ds.setRadius(3.0);
					ds.setOffsetX(1.5);
					ds.setOffsetY(1.5);
					ds.setColor(Color.color(0.2, 0.2, 0.2));
					backgroundpane.setEffect(ds);

					BorderPane border = new BorderPane();
					border.setCenter(backgroundpane);
					Stop[] stops = new Stop[] { new Stop(0, Color.web("#BBC8E6")), new Stop(1, Color.web("#89C3EB")) };
					LinearGradient lg = new LinearGradient(0, 1, 1, 0, true, CycleMethod.NO_CYCLE, stops);

					border.setBackground(new Background(new BackgroundFill(lg, null, null)));
					contentholder.setContent(border);
					contentholder.setContent(border);

					contentholder.requestFocus();
					contentholder.fireEvent(
							new KeyEvent(KeyEvent.KEY_PRESSED, null, null, KeyCode.TAB, false, false, false, false));
					logger.finer(" -------------- starting mothball of old page --------------");
					if (previouspage != null)
						if (previouspage.getNode() != null)
							previouspage.getNode().mothball();
					logger.finer(" -------------- ending mothball of old page --------------");

					long displaytime = new Date().getTime() - endservertalk;
					boolean show = showtechdetails;
					if (serverconnection + displaytime > 5000)
						show = true;

					if (show) {
						updateStatusBar("displayed page succesfully, server time (ms) : " + (serverconnection)
								+ ", data read (ch): " + (messagesize / 1024) + "KB display (ms) " + displaytime
								+ ClientTools.memoryStatement() + " cached data " + pagedatachedkb
								+ "KB, total cache size " + totalpagecachekb + "KB");
					} else {
						updateStatusBar("displayed page succesfully");

					}
					parent.setBusinessScreenFrozen(false);

				} catch (Exception e) {
					logger.warning("Error in displaying page " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						String element = e.getStackTrace()[i].toString();
						if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
							logger.warning(e.getStackTrace()[i].toString());
					}

					updateStatusBar("Error in displaying page :" + e.getMessage(), true);
					parent.setBusinessScreenFrozen(false);

				}
			}

		});

	}

	/**
	 * a class to record the last scroll height of a page
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
	 *
	 */
	private class PageHistory {
		private String address;
		private double scrollheight;

		/**
		 * @param address address of the page
		 * @param scrollheight scroll height when last visited the page
		 */
		public PageHistory(String address, double scrollheight) {
			super();
			this.address = address;
			this.scrollheight = scrollheight;
		}

		@Override
		public String toString() {
			return "  ## PageHistory: " + address + ",scrollheight=" + scrollheight;
		}

	}

	private ArrayList<PageHistory> lastpages;

	/**
	 * checks if the page was already visited
	 * @param address looks up for the page history address in the local repository
	 * @return page history if it exists
	 */
	public PageHistory lookupPageForAddress(String address) {
		if (address == null)
			return null;
		if (address.length() == 0)
			return null;
		for (int i = 0; i < lastpages.size(); i++) {
			PageHistory thispage = lastpages.get(i);
			if (thispage.address.equals(address))
				return thispage;
		}
		return null;

	}

	/**
	 * adds a page history to the local repository
	 * @param pagehistory
	 */
	public void addPageHistory(PageHistory pagehistory) {

		for (int i = 0; i < lastpages.size(); i++) {
			if (lastpages.get(i).address.equals(pagehistory.address)) {
				logger.fine(" ---- remove page history for replacement " + lastpages.get(i));
				lastpages.remove(i);
			}
		}

		lastpages.add(pagehistory);
		logger.fine(" ---- adding page history " + pagehistory);
		if (lastpages.size() > 3) {
			logger.fine(" ---- remove page history as capacity exceeded");
			;
			lastpages.remove(0);
		}
		logger.fine(" --------------------------- PAGE HISTORY ------------------------ ");
		for (int i = 0; i < lastpages.size(); i++) {
			logger.fine(lastpages.get(i).toString());
		}
		logger.fine("---------------------------- PAGE HISTORY END ---------------------");
	}

	/**
	 * refreshes the change listener to the content holder height
	 * @param newheightlistener new change listener
	 */
	public void setnewContentHolderHeightChangeListener(ChangeListener<Number> newheightlistener) {
		if (heightlistener != null)
			contentholder.heightProperty().removeListener(heightlistener);
		heightlistener = newheightlistener;
		contentholder.heightProperty().addListener(newheightlistener);
	}

	/**
	 * refreshes the change listener to the content holder width
	 * @param newwidthlistener new change listener
	 */
	public void setnewContentHolderWidthChangeListener(ChangeListener<Number> newwidthlistener) {
		if (widthlistener != null)
			contentholder.widthProperty().removeListener(widthlistener);
		widthlistener = newwidthlistener;
		contentholder.widthProperty().addListener(newwidthlistener);
	}

	private ChangeListener<Number> heightlistener;
	private ChangeListener<Number> widthlistener;

	/**
	 * @param title      null if user pressed cancel, or the text entered if pressed
	 *                   OK (if pressed OK with no text entered, empty string is
	 *                   brought back
	 * @param textlength the maximum length of text to enter
	 * @return
	 */
	public String showModalTextEntry(String title, int textlength) {
		logger.fine("NormalTextEntry " + title + " - " + textlength);

		logger.fine(" prepare to launch dialog");
		TextInputDialog dialog = new TextInputDialog();

		dialog.setHeaderText("Enter Update Note below");
		dialog.setContentText("");

		Optional<String> result = dialog.showAndWait();
		logger.fine(" dialog is displayed");

		if (!result.isPresent())
			return null;
		return result.get();

	}

	/**
	 * will launch the sending to the server of the address shown in the connection bar
	 */
	public void triggerLaunchAddress() {
		connectionbar.triggerLaunchAddress();
	}

	/**
	 * @return a display with no page shown
	 */
	public Node getEmptyDisplay() {
		logger.severe("Setting empty display");
		BorderPane mainpane = new BorderPane();
		mainpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		connectionbar = new ConnectionBar(this, urltoconnectto, this.questionmarkicon);

		this.userinteractionwidgets.add(connectionbar);

		Pane connectionpanel = connectionbar.getPane();
		mainpane.setTop(connectionpanel);
		BorderPane.setMargin(connectionpanel, new Insets(3, 5, 3, 5));
		Pane statusbar = generateStatusBar();

		mainpane.setBottom(statusbar);
		BorderPane.setMargin(statusbar, new Insets(3, 5, 3, 5));
		ScrollPane contentholder = new ScrollPane();
		contentholder.setStyle("-fx-background: rgb(255,255,255);");
		contentholder.setBorder(Border.EMPTY);
		mainpane.setCenter(contentholder);
		this.contentholder = contentholder;
		return mainpane;

	}

	/**
	 * generates the JAVAFX component for the status bar
	 * @return the Pane
	 */
	public Pane generateStatusBar() {
		Label statusbar = new Label("");
		statusbar.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (event.getButton().equals(MouseButton.SECONDARY)) {

					final ClipboardContent content = new ClipboardContent();
					content.putString(statusbar.getText());
					Clipboard.getSystemClipboard().setContent(content);
				} else {

					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Status Message");
					alert.setHeaderText("Status Message");
					alert.setContentText(statusbar.getText());

					alert.showAndWait();
				}

			}
		});
		this.statuslabel = statusbar;
		HBox statusbarpane = new HBox(5);
		statusbarpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		statusbarpane.getChildren().add(statusbar);
		return statusbarpane;

	}

	

	/**
	 * displays a message in the status bar
	 * @param message the message to show
	 * @param showaserror if true, message is shown as error (in yellow), if false, displays it as simple text
	 */
	public void updateStatusBar(String message, boolean showaserror) {
		if (showaserror) {
			Platform.runLater(new Thread() {
				@Override
				public void run() {
					try {
						Date date = new Date();
						String output = "" + sdf.format(date) + " " + message;
						logger.info(output);
						statuslabel.setBackground(
								new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
						statuslabel.setText(output);
					} catch (Exception e) {
						logger.warning("Exception in setting status bar " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++) {
							String element = e.getStackTrace()[i].toString();
							if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
								logger.warning(e.getStackTrace()[i].toString());
						}
					}
				}
			});
		} else
			updateStatusBar(message);
	}

	/**
	 * @param message Displays a message in the status bar with normal status
	 */
	public void updateStatusBar(String message) {
		Date date = new Date();
		String output = ""+sdf.format(date)+" "+message;
		logger.info(output);
		Platform.runLater(new Thread() {
		@Override
		public void run() {
			
			
			
			try {
			statuslabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			
			statuslabel.setText(output);
			} catch (Exception e ) {
				logger.warning("Exception in setting status bar "+e.getMessage());
				for (int i=0;i<e.getStackTrace().length;i++) {
					String element = e.getStackTrace()[i].toString();
					if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
						logger.warning(e.getStackTrace()[i].toString());
				}
			}
		}
		});
		
		
	}

	/**
	 * In this version, only the connection bar is frozen when an event is sent to the server. In further versions,
	 * it is planned to also freeze pushing other buttons.
	 * @param frozen true to freeze the screen, false to unfreeze the screen
	 */
	public void setBusinessScreenFrozen(boolean frozen) {
		Platform.runLater(new Thread() {

			@Override
			public void run() {
				try {
					if (frozen) {
						for (int i = 0; i < userinteractionwidgets.size(); i++) {
							logger.finest("+-+ disabling one widget " + userinteractionwidgets.get(i));
							userinteractionwidgets.get(i).disableDuringServerRequest();
						}
					} else {
						for (int i = 0; i < userinteractionwidgets.size(); i++) {
							userinteractionwidgets.get(i).enableAfterServerResponse();
							logger.finest("+-+ enabling one widget " + userinteractionwidgets.get(i));
						}
					}
				} catch (Exception e) {
					logger.warning("Exception in setting business screen frozen " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						String element = e.getStackTrace()[i].toString();
						if ((element.startsWith("gallium") || (element.startsWith("org.openlowcode"))))
							logger.warning(e.getStackTrace()[i].toString());
					}
				}
			}
		});

	}

}