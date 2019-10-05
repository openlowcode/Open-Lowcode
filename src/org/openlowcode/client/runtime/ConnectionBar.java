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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.tools.desktop.DesktopServices;
import org.openlowcode.tools.misc.NiceFormatters;
import org.openlowcode.tools.misc.OpenLowcodeLogFilter;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * The browser-style connection bar used by theclient
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConnectionBar implements UserInteractionWidget {
	private TextField address;
	public static final SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm");
	private Logger logger = Logger.getLogger(ConnectionBar.class.toString());
	Pane content;
	private Button stopconnection;
	private Button connectionbutton;
	private Button back;
	private Button more;

	private TableView<AddressLink> linkaddress;
	private Popup linkaddresspopup;

	private ClientSession parentsession;

	/**
	 * @return the text field where address is entered
	 */
	public TextField getAddress() {
		return this.address;
	}

	/**
	 * starts the process to connecting to the server with the addres given in the
	 * connection bar
	 */
	public void triggerLaunchAddress() {
		if (address.getText() != null)
			if (address.getText().length() > 0)
				parentsession.sendLink(address.getText(), false);
	}

	/**
	 * Creates a new connection bar, and configures the JAVAFX nodes
	 * 
	 * @param parentdisplay    parent client display
	 * @param originurl        URL to show if specified (may be null)
	 * @param questionmarkicon URL of the icon to display the client
	 */
	public ConnectionBar(ClientDisplay parentdisplay, String originurl, String questionmarkicon) {

		this.parentsession = parentdisplay.getParentServerConnection();
		HBox connectionbar = new HBox(12);
		connectionbar.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		connectionbar.setAlignment(Pos.CENTER_LEFT);

		this.more = new Button("<<");
		this.more.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		this.back = new Button("<");
		this.back.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		connectionbar.getChildren().add(more);
		connectionbar.getChildren().add(back);
		more.setDisable(true);
		more.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				ClientData clientdata = parentsession.getClientData();
				boolean shownew = false;
				if (linkaddresspopup == null)
					shownew = true;
				if (linkaddresspopup != null)
					if (!linkaddresspopup.isShowing())
						shownew = true;
				if (shownew) {
					linkaddresspopup = new Popup();

					linkaddress = new TableView<AddressLink>();
					linkaddress.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
					TableColumn<AddressLink, String> addresscolumn = new TableColumn<AddressLink, String>("Page");
					addresscolumn.setMinWidth(300);
					addresscolumn.setCellValueFactory(
							new Callback<CellDataFeatures<AddressLink, String>, ObservableValue<String>>() {
								public ObservableValue<String> call(CellDataFeatures<AddressLink, String> p) {
									return new ReadOnlyObjectWrapper<String>(p.getValue().getTitle());
								}
							});
					TableColumn<AddressLink, String> lastvisitcolumn = new TableColumn<AddressLink, String>(
							"Last Visit");
					lastvisitcolumn.setCellValueFactory(
							new Callback<CellDataFeatures<AddressLink, String>, ObservableValue<String>>() {
								public ObservableValue<String> call(CellDataFeatures<AddressLink, String> p) {
									return new ReadOnlyObjectWrapper<String>(
											NiceFormatters.formatNiceDate(p.getValue().getDate(), false));
								}
							});
					lastvisitcolumn.setMinWidth(100);
					linkaddress.getColumns().add(lastvisitcolumn);
					linkaddress.getColumns().add(addresscolumn);
					ObservableList<AddressLink> data = FXCollections
							.observableArrayList(clientdata.getOrderedAddressLink());
					linkaddress.setItems(data);
					linkaddress.setOnMouseClicked(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent mousevent) {
							AddressLink selectedaddress = linkaddress.getSelectionModel().getSelectedItem();
							linkaddresspopup.hide();
							if (selectedaddress != null) {
								parentsession.sendLink(selectedaddress.getClink(), true);
							}
						}

					});
					linkaddress.focusedProperty().addListener(new ChangeListener<Boolean>() {

						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue,
								Boolean newvalue) {
							if (!newvalue)
								linkaddresspopup.hide();

						}

					});
					linkaddress.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
					linkaddress.setMinWidth(400);
					linkaddresspopup.getContent().add(linkaddress);
					linkaddresspopup.setWidth(400);
					double wx = more.getScene().getWindow().getX();
					double wy = more.getScene().getWindow().getY();
					Bounds buttonbounds = more.localToScene(more.getBoundsInLocal());
					linkaddresspopup.show(more, wx + buttonbounds.getMaxX(),
							wy + buttonbounds.getMaxY() + more.getHeight() + 5);
				} else {
					linkaddresspopup.hide();
				}
			}

		});

		more.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
				if (newvalue.booleanValue()) {

				} else {
					if (linkaddresspopup != null)
						linkaddresspopup.hide();
				}

			}
		});

		back.setDisable(true);
		back.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				String lastaddress = parentsession.getClientData().getLastAddress();
				address.setText(lastaddress);
				parentsession.sendLink(address.getText(), true);

			}

		});
		Label applicationlabel = new Label("Application");
		connectionbar.getChildren().add(applicationlabel);
		address = new TextField();
		if (originurl != null)
			address.setText(originurl);
		address.setPrefColumnCount(40);
		connectionbar.getChildren().add(address);
		connectionbutton = new Button("Connect");
		connectionbutton.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		connectionbutton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				parentsession.sendLink(address.getText(), false);
			}
		});
		// launching it too soon before page has done layout
		// if (originurl!=null) parentsession.sendLink(address.getText(),false);

		connectionbar.getChildren().add(connectionbutton);

		stopconnection = new Button("Stop");
		stopconnection.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		stopconnection.setDisable(true);
		stopconnection.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				enableAfterServerResponse();
				parentsession.stopconnection();
			}
		});
		address.setOnAction(connectionbutton.getOnAction());
		connectionbar.getChildren().add(stopconnection);
		BorderPane barwithright = new BorderPane();
		barwithright.setCenter(connectionbar);
		MenuBar morebar = new MenuBar();
		ImageView questionmark = new ImageView(new Image(questionmarkicon));
		questionmark.setFitHeight(16);
		questionmark.setFitWidth(16);

		morebar.setStyle("-fx-background-color: transparent;");
		Menu moremenu = new Menu("", questionmark);
		morebar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		morebar.getMenus().add(moremenu);

		CheckMenuItem techdetails = new CheckMenuItem("Show tech details");

		techdetails.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (techdetails.isSelected()) {
					parentsession.setShowTechDetails(true);
				} else {
					parentsession.setShowTechDetails(false);
				}

			}

		});
		MenuItem extralogs = new MenuItem("Extra Logs");

		moremenu.getItems().add(techdetails);
		moremenu.getItems().add(extralogs);

		extralogs.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				GridPane dialogpane = new GridPane();
				dialogpane.setVgap(4);
				dialogpane.setHgap(8);
				dialogpane.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID,
						CornerRadii.EMPTY, new BorderWidths(3, 6, 3, 6))));
				dialogpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
				Label classforlog = new Label("Class");
				Label level = new Label("Log Level");
				TextField classforlogtf = new TextField();
				classforlogtf.setPrefColumnCount(30);
				ChoiceBox<String> levelcb = new ChoiceBox<String>(FXCollections.observableArrayList("Severe", "Warning",
						"Info", "Config", "Fine", "Finer", "Finest"));
				levelcb.setValue("Finest");
				dialogpane.add(classforlog, 0, 0);
				dialogpane.add(classforlogtf, 1, 0);
				dialogpane.add(level, 0, 1);
				dialogpane.add(levelcb, 1, 1);
				HBox buttonpane = new HBox(6);
				dialogpane.add(buttonpane, 1, 2);
				Button add = new Button("Add");
				buttonpane.getChildren().add(add);
				Button removeall = new Button("Remove All");
				buttonpane.getChildren().add(removeall);
				Stage dialog = new Stage();
				dialog.setTitle("Add extra logs for debugging");
				dialog.setScene(new Scene(dialogpane));
				dialog.initOwner(parentsession.getMainFrame().getPrimaryStage());
				dialog.initModality(Modality.APPLICATION_MODAL);
				removeall.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						logger.severe("Starting handling remove log exception");
						dialog.close();
						OpenLowcodeLogFilter logfilefilter = parentsession.getMainFrame().getLogFileFilter();
						OpenLowcodeLogFilter consolefilter = parentsession.getMainFrame().getConsoleFilter();

						if (logfilefilter != null)
							logfilefilter.cleanException();
						if (consolefilter != null)
							consolefilter.cleanException();

					}

				});
				add.setOnAction(new EventHandler<ActionEvent>() {

					public Level getLevel(String level) {
						if (level.equals("Severe"))
							return Level.SEVERE;
						if (level.equals("Warning"))
							return Level.WARNING;
						if (level.equals("Info"))
							return Level.INFO;
						if (level.equals("Config"))
							return Level.CONFIG;
						if (level.equals("Fine"))
							return Level.FINE;
						if (level.equals("Finer"))
							return Level.FINER;
						if (level.equals("Finest"))
							return Level.FINEST;
						return null;
					}

					@Override
					public void handle(ActionEvent actionevent) {

						try {
							logger.severe("Starting handling add logs");
							dialog.close();
							OpenLowcodeLogFilter logfilefilter = parentsession.getMainFrame().getLogFileFilter();
							OpenLowcodeLogFilter consolefilter = parentsession.getMainFrame().getConsoleFilter();
							if (logfilefilter != null) {
								Level level = getLevel(levelcb.getValue());
								String exceptionclass = classforlogtf.getText();
								if (exceptionclass == null)
									exceptionclass = "";
								exceptionclass = exceptionclass.trim();
								if ((level != null) && (exceptionclass.length() > 0)) {
									logfilefilter.addException(exceptionclass, level);
									logger.severe("Adding exception logs Level for file " + level + " for path = "
											+ exceptionclass);
								}
							}
							if (consolefilter != null) {
								Level level = getLevel(levelcb.getValue());
								String exceptionclass = classforlogtf.getText();
								if (exceptionclass == null)
									exceptionclass = "";
								exceptionclass = exceptionclass.trim();
								if ((level != null) && (exceptionclass.length() > 0)) {
									consolefilter.addException(exceptionclass, level);
									logger.severe("Adding exception logs Level for console " + level + " for path = "
											+ exceptionclass);

								}

							}
						} catch (Exception e) {
							logger.severe("Exception is setting extra logs " + e.getClass().getName() + " - "
									+ e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++)
								logger.severe("    - " + e.getStackTrace()[i]);
							Alert exceptionalert = new Alert(Alert.AlertType.ERROR, "Error in setting extra logs");
							exceptionalert.showAndWait();
						}
					}

				});

				dialog.showAndWait();

			}

		});

		String infotext = "Open Lowcode Client version " + parentsession.getMainFrame().getClientversion()
				+ "\n updated on " + sdf.format(parentsession.getMainFrame().getClientversiondate());
		Label appversion = new Label(infotext);
		appversion.setWrapText(true);
		;
		appversion.setTextAlignment(TextAlignment.LEFT);
		CustomMenuItem appversionitem = new CustomMenuItem(appversion);
		moremenu.getItems().add(appversionitem);

		MenuItem knowmore = new MenuItem("Powered by Open Lowcode");
		knowmore.setStyle("-fx-font-weight: bold;");
		moremenu.getItems().add(knowmore);
		knowmore.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DesktopServices.launchBrowser("https://openlowcode.com/");
			}

		});
		barwithright.setRight(morebar);
		content = barwithright;
	}

	/**
	 * @return gets the JAVAFX pane of the connection bar
	 */
	public Pane getPane() {
		return content;
	}

	/**
	 * puts the connection bar back active
	 */
	public void setBackActive() {

		if (parentsession.getClientData().isBackPossible()) {
			this.back.setDisable(false);
			this.more.setDisable(false);
		} else {
			this.back.setDisable(true);
			this.more.setDisable(true);

		}
	}

	@Override
	public void disableDuringServerRequest() {
		stopconnection.setDisable(false);
		back.setDisable(true);
		more.setDisable(true);
		connectionbutton.setDisable(true);
		logger.finer("disable during server request inside connection bar");
	}

	@Override
	public void enableAfterServerResponse() {
		stopconnection.setDisable(true);
		connectionbutton.setDisable(false);
		if (parentsession.getClientData().isBackPossible()) {
			this.back.setDisable(false);
			this.more.setDisable(false);
		} else {
			this.back.setDisable(true);
			this.more.setDisable(true);
		}
		logger.finer("enable during server request inside connection bar");
	}

	/**
	 * @param fulladdress enters the given address in the connection bar
	 */
	public void setPageAddress(String fulladdress) {
		address.setText(fulladdress);

	}
}
