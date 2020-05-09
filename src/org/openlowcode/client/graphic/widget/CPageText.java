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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.desktop.DesktopServices;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import javafx.stage.Window;
import javafx.scene.layout.HBox;

import javafx.scene.paint.Color;

/**
 * a text shown in a page. It can either be hard-coded or variable
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPageText
		extends
		CPageNode {
	private String text;
	private String data;
	private CPageDataRef dataref;
	private int type;
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_WARNING = 2;
	private boolean isvisiblehelper;
	private String visiblehelpertext;
	private boolean hasurl;
	private String url;
	private boolean hasexternalurl;
	private CPageDataRef urldataref;

	/**
	 * A specific constructor to create hard-coded pages on the client
	 * 
	 * @param text       text to show
	 * @param title      if true, show as title
	 * @param parentpath path of the parent widget
	 */
	public CPageText(String text, boolean title, CPageSignifPath parentpath) {
		super(parentpath, null);
		this.text = text;
		if (title) {
			this.type = TYPE_TITLE;
		} else {
			this.type = TYPE_NORMAL;
		}
		this.isvisiblehelper = false;
		this.data = "LOCAL";
	}

	/**
	 * creates a page text from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CPageText(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.data = reader.returnNextStringField("DATA");

		if (this.data.compareTo("LOCAL") == 0) {
			this.text = reader.returnNextStringField("TEXT");

		} else {
			dataref = CPageDataRef.parseCPageDataRef(reader);
			if (!dataref.getType().equals(new TextDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted TextDataEltType, got %s in CPage ",
						dataref.getName(), dataref));
		}
		boolean istypesupported = false;
		this.type = reader.returnNextIntegerField("TYPE");
		if (type == TYPE_NORMAL)
			istypesupported = true;
		if (type == TYPE_TITLE)
			istypesupported = true;
		if (type == TYPE_WARNING)
			istypesupported = true;
		if (!istypesupported)
			throw new RuntimeException("the type is not supported " + type);
		this.isvisiblehelper = reader.returnNextBooleanField("VSH");
		if (this.isvisiblehelper)
			this.visiblehelpertext = reader.returnNextStringField("HLT");
		this.hasurl = reader.returnNextBooleanField("HUR");
		if (this.hasurl) {
			this.hasexternalurl = reader.returnNextBooleanField("EXU");
			if (!this.hasexternalurl) {
				this.url = reader.returnNextStringField("URL");
			} else {
				this.urldataref = CPageDataRef.parseCPageDataRef(reader);
			}

		}
		reader.returnNextEndStructure("PAGETEXT");
	}

	private static String getExternalReference(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = ", dataref.getName()));
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		TextDataElt thistextelement = (TextDataElt) thiselement;
		return thistextelement.getPayload();
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		if (this.data.compareTo("EXT") == 0) {
			this.text = getExternalReference(inputdata, dataref);
		}
		if (this.hasexternalurl) {
			this.url = getExternalReference(inputdata, urldataref);
		}

		Label thislabel = new Label(text);
		if (this.hasurl) {
			thislabel.setUnderline(true);
			thislabel.setTextFill(Color.CRIMSON);
		}

		if (type == TYPE_TITLE) {
			thislabel.setFont(
					Font.font(thislabel.getFont().getName(), FontWeight.BOLD, thislabel.getFont().getSize() * 1.2));
			DropShadow ds = new DropShadow();
			ds.setRadius(1.);
			ds.setOffsetX(1.);
			ds.setOffsetY(1.);
			ds.setColor(Color.color(0.8, 0.8, 0.8));
			thislabel.setEffect(ds);

			thislabel.setTextFill(Color.web("#17184B"));
			thislabel.setPadding(new Insets(12, 5, 3, 50));

		}

		if (type == TYPE_WARNING) {

			thislabel.setFont(
					(Font.font(thislabel.getFont().getName(), FontWeight.BOLD, thislabel.getFont().getSize() * 1.05)));

			thislabel.setTextFill(Color.CRIMSON);

			logger.finest("  --------------> ThisLabel setTextFill " + thislabel.getTextFill());
		}

		if (this.hasurl) {
			thislabel.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent mouseevent) {
					if (mouseevent.getClickCount() == 2) // doubleclick
					{
						DesktopServices.launchBrowser(url);
					}
				}

			});
		}

		if (!this.isvisiblehelper) {
			return thislabel;
		} else {
			HBox box = new HBox();

			box.getChildren().add(thislabel);
			Label helper = new Label(" ? ");
			BorderPane helperpane = new BorderPane();
			if (type == TYPE_TITLE) {
				helperpane.setPadding(new Insets(5, 0, 10, 0));
				helper.setFont(
						Font.font(helper.getFont().getName(), FontWeight.BOLD, helper.getFont().getSize() * 1.2));

			} else {
				helper.setFont(Font.font(helper.getFont().getName(), FontWeight.BOLD, helper.getFont().getSize()));
			}
			helperpane.setCenter(helper);
			DropShadow ds = new DropShadow();
			ds.setRadius(3.0);
			ds.setOffsetX(1.5);
			ds.setOffsetY(1.5);
			ds.setColor(Color.color(0.2, 0.2, 0.2));

			Tooltip helpertooltip = new Tooltip(this.visiblehelpertext);
			helpertooltip.setFont(Font.font(helpertooltip.getFont().getName(), FontPosture.ITALIC,
					helpertooltip.getFont().getSize() * 0.9));
			helpertooltip.setWrapText(true);
			helper.setTextFill(Color.web("#BC2D29"));
			helper.setStyle("-fx-color: #BC2D29;-fx-border-color: #BC2D29;-fx-border-radius: 5 5 5 5;");
			helper.setEffect(ds);
			helper.setTooltip(helpertooltip);
			box.getChildren().add(helperpane);
			return box;
		}
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		throw new RuntimeException(
				String.format("request of action data of type %s, but CPageText cannot provide any data", type));
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
