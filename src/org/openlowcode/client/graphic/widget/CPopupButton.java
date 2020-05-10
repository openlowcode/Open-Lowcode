/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;

import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.tools.NicePopup;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;

/**
 * A popup button that will show a node when pressed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPopupButton
		extends
		CPageNode {
	private static Logger logger = Logger.getLogger(CPopupButton.class.getName());
	private String label;
	private String rollovertip;
	private CPageNode payload;
	private boolean allowscroll;
	private boolean showunderwidget;

	/**
	 * generate and shows the popup
	 * 
	 * @param parentnode      parent javafx node
	 * @param popupnode       node to show in the popup pane
	 * @param inputdata       page input data
	 * @param parentwindow    parent javafx window
	 * @param allowscroll     if true, allows scroll
	 * @param showunderwidget if true, shows under widget, if false, shows at the
	 *                        middle of the application window
	 */
	public static void generateAndShowPopup(
			Node parentnode,
			Node popupnode,
			CPageData inputdata,
			Window parentwindow,
			boolean allowscroll,
			boolean showunderwidget) {
		NicePopup nicepopup = new NicePopup(parentnode, popupnode, parentwindow, allowscroll, showunderwidget);
		if (nicepopup.getSubScene() != null)
			inputdata.addSubScene(nicepopup.getSubScene());

	}

	/**
	 * creates the widget from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CPopupButton(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.label = reader.returnNextStringField("LBL");
		this.rollovertip = reader.returnNextStringField("RLV");
		reader.returnNextStartStructure("PPAGE");
		payload = CPageNode.parseNode(reader, parentpath);
		reader.returnNextEndStructure("PPAGE");
		allowscroll = reader.returnNextBooleanField("SCL");
		showunderwidget = reader.returnNextBooleanField("SUW");
		reader.returnNextEndStructure("POPUPBTN");

	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		Button button = new Button(label);
		button.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		Label hamburgerlabel = new Label("\u2630");
		hamburgerlabel.setFont(Font.font(hamburgerlabel.getFont().getFamily(), FontWeight.THIN,
				hamburgerlabel.getFont().getSize() * 0.5f));
		button.setGraphic(hamburgerlabel);
		button.setContentDisplay(ContentDisplay.RIGHT);
		button.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
		button.textOverrunProperty().set(OverrunStyle.CLIP);
		if (rollovertip != null)
			if (rollovertip.length() > 0)
				button.setTooltip(new Tooltip(rollovertip));

		// --------- setup popup window

		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				try {
					CPopupButton.generateAndShowPopup(button,
							payload.getNode(actionmanager, inputdata, parentwindow, new TabPane[0]), inputdata,
							parentwindow, allowscroll, showunderwidget);
				} catch (Exception e) {
					logger.severe("Unexpected exception " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.severe("   + " + e.getStackTrace()[i].toString());
				}
			}
		});

		// ------------------------

		return button;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException(
				String.format("request of action data of type %s, but CPopupButton cannot provide any data", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {
	}
}
