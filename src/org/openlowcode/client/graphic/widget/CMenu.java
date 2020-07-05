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
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.runtime.PageActionManager;
import javafx.stage.Window;
import javafx.scene.control.Menu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A menu element that is part of a menu bar
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CMenu {
	private String label;
	private ArrayList<CMenuItem> listofitems;
	private String icon;

	/**
	 * create a menu from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CMenu(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {

		listofitems = new ArrayList<CMenuItem>();
		label = reader.returnNextStringField("LBL");
		icon = reader.returnNextStringField("ICN");
		reader.startStructureArray("ITM");
		while (reader.structureArrayHasNextElement("ITM")) {
			reader.returnNextStartStructure("MNUITM");
			listofitems.add(new CMenuItem(reader, parentpath));
			reader.returnNextEndStructure("ITM");
		}
		reader.returnNextEndStructure("MENU");
	}

	/**
	 * gets a javafx menu corresponding to this element
	 * 
	 * @param actionmanager action manager of the page
	 * @param inputdata     input data of the page
	 * @param parentwindow  parent window
	 * @return javafx menu
	 */
	public Menu getMenu(PageActionManager actionmanager, CPageData inputdata, Window parentwindow) {
		Menu menu = new Menu(label);
		if (icon != null)
			if (icon.length() > 0) {
				ImageView image = new ImageView(new Image(icon));
				image.setFitHeight(16);
				image.setFitWidth(16);

				if (image != null)
					menu.setGraphic(image);
			}
		for (int i = 0; i < listofitems.size(); i++) {
			menu.getItems().add(listofitems.get(i).getMenuItem(actionmanager, inputdata, parentwindow));
		}
		return menu;
	}

}
