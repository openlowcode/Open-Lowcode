/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.tools;

import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.layout.VBox;

/**
 * The nice popup will display a node in two modes:
 * <ul>
 * <li>A heavy weight mode that will look like a popup with typically several
 * fields and buttons</li>
 * <li>A light weight mode suitable to display detail on a widget (e.g. list for
 * selection)</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NicePopup {
	private static Logger logger = Logger.getLogger(NicePopup.class.getName());
	private Stage dialog;
	private Popup resultpopup;
	private Node parentnode;
	private Node popupnode;
	private Window parentwindow;
	private boolean allowscroll;
	private boolean showunderwidget;

	/**
	 * shows the widget
	 */
	public void show() {
		try {

			if (!showunderwidget) {
				// -----------------------------------------------------------------------------------------
				// MIDDLE

				dialog = new Stage();
				dialog.initModality(Modality.NONE);
				dialog.initOwner(parentwindow);
				dialog.initStyle(StageStyle.UTILITY);

				VBox scrollpanepackaged = new VBox();
				scrollpanepackaged.getChildren().add(popupnode);
				scrollpanepackaged.setBorder(Border.EMPTY);
				VBox.setMargin(popupnode, new Insets(8, 5, 5, 18));
				scrollpanepackaged.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));

				Scene dialogScene = null;

				if (allowscroll) {
					ScrollPane scrollpane = new ScrollPane();

					scrollpane.setContent(scrollpanepackaged);
					scrollpane.hbarPolicyProperty().setValue(ScrollBarPolicy.AS_NEEDED);
					scrollpane.vbarPolicyProperty().setValue(ScrollBarPolicy.AS_NEEDED);
					scrollpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
					dialogScene = new Scene(scrollpane);

				} else {
					dialogScene = new Scene(scrollpanepackaged);

				}

				dialogScene.getStylesheets().add("css/openlowcode.css");
				dialog.setScene(dialogScene);
				dialog.show();
				dialog.sizeToScene();
				if (!allowscroll) {
					if (popupnode instanceof Pane) {
						Pane popuppane = (Pane) popupnode;
						popuppane.heightProperty().addListener(new ChangeListener<Number>() {

							@Override
							public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
								logger.finest("Size of content changed");
								dialog.sizeToScene();
							}

						});
					}

				}

				double mainwindowheight = parentwindow.getHeight();
				double mainwindowwidth = parentwindow.getWidth();
				double mainwindowx = parentwindow.getX();
				double mainwindowy = parentwindow.getY();

				double maxwidth = dialog.getWidth();
				if (mainwindowwidth - 60 < maxwidth)
					maxwidth = mainwindowwidth - 60;
				dialog.setMaxWidth(maxwidth);
				parentnode.localToScene(parentnode.getBoundsInLocal());
				double subwindowx = (mainwindowwidth - maxwidth) / 2 + mainwindowx;
				double subwindowy = (mainwindowheight - dialog.getHeight()) / 2 + mainwindowy;
				if (subwindowy + dialog.getHeight() > mainwindowy + mainwindowwidth)
					subwindowy = mainwindowy + mainwindowwidth - dialog.getHeight();
				dialog.setX(subwindowx);
				dialog.setY(subwindowy);

				dialog.setMaxHeight(dialog.getHeight() + 200);
				dialog.show();
				logger.fine("size of content width = " + scrollpanepackaged.getWidth());
				parentwindow.focusedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						dialog.close();
						parentwindow.focusedProperty().removeListener(this);
					}

				});

				parentnode.localToSceneTransformProperty().addListener(new ChangeListener<Transform>() {

					@Override
					public void changed(ObservableValue<? extends Transform> arg0, Transform arg1, Transform arg2) {
						if (dialog.isShowing()) {
							dialog.hide();

						}

					}

				});

			} else {
				// -----------------------------------------------------------------------------------------
				// LOCAL
				resultpopup = new Popup();

				double mainwindowx = parentwindow.getX();
				double mainwindowy = parentwindow.getY();
				double widgetx = parentnode.getScene().getX();
				double widgety = parentnode.getScene().getY();
				Point2D point = parentnode.localToScene(0, 0);
				if (parentnode instanceof Region)
					widgety = widgety + ((Region) parentnode).getHeight();
				BorderPane pane = new BorderPane();

				pane.setPadding(new Insets(5, 15, 10, 20));
				pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
				pane.centerProperty().set(popupnode);
				DropShadow ds = new DropShadow();
				ds.setOffsetY(3.0);
				ds.setOffsetX(3.0);
				ds.setColor(Color.GRAY);
				pane.setEffect(ds);
				pane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
						BorderWidths.DEFAULT)));
				resultpopup.getContent().add(pane);

				resultpopup.show(parentnode, mainwindowx + widgetx + point.getX(),
						mainwindowy + widgety + point.getY());

				parentnode.focusedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
						if (!newvalue) {

							resultpopup.hide();
							resultpopup.getContent().clear();
							parentnode.focusedProperty().removeListener(this);
						}
					}

				});
				parentnode.localToSceneTransformProperty().addListener(new ChangeListener<Transform>() {

					@Override
					public void changed(ObservableValue<? extends Transform> arg0, Transform arg1, Transform arg2) {
						if (resultpopup.isShowing()) {
							resultpopup.hide();
							resultpopup.getContent().clear();
						}

					}

				});
			}

		} catch (Exception e) {
			logger.severe("Unexpected exception in setting up popup " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe("   + " + e.getStackTrace()[i].toString());
		}

	}

	/**
	 * creates a Nice popup
	 * 
	 * @param parentnode      parent node
	 * @param popupnode       popup node
	 * @param parentwindow    parent window
	 * @param allowscroll     if true, scroll is allowed
	 * @param showunderwidget true for a lightweight component, false for a
	 *                        heavyweigth component
	 * @param shownow         if true, show now, if false, show later
	 */
	public NicePopup(
			Node parentnode,
			Node popupnode,
			Window parentwindow,
			boolean allowscroll,
			boolean showunderwidget,
			boolean shownow) {
		this.parentnode = parentnode;
		this.popupnode = popupnode;
		this.parentwindow = parentwindow;
		this.allowscroll = allowscroll;
		this.showunderwidget = showunderwidget;
		if (shownow)
			show();
	}

	/**
	 * creates a Nice popup shown immediately
	 * 
	 * @param parentnode      parent node
	 * @param popupnode       popup node
	 * @param parentwindow    parent window
	 * @param allowscroll     if true, scroll is allowed
	 * @param showunderwidget true for a lightweight component, false for a
	 *                        heavyweigth component
	 */
	public NicePopup(
			Node parentnode,
			Node popupnode,
			Window parentwindow,
			boolean allowscroll,
			boolean showunderwidget) {
		this(parentnode, popupnode, parentwindow, allowscroll, showunderwidget, true);
	}

	/**
	 * @return the stage of the popup
	 */
	public Stage getSubScene() {
		return this.dialog;

	}
}
