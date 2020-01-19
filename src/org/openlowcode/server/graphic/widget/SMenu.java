/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * one menu of a menu bar in the SMenuBar widget
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SMenu {
	private ArrayList<SMenuItem> menuitemlist;
	private String label;

	/**
	 * creates a menu for the given page and the given label
	 * 
	 * @param parentpage parent page
	 * @param label      label in default language
	 */
	public SMenu(SPage parentpage, String label) {

		this.label = label;
		menuitemlist = new ArrayList<SMenuItem>();
	}

	/**
	 * adds a menu item to the menu
	 * 
	 * @param menuitem menu item to add
	 */
	public void addMenuItem(SMenuItem menuitem) {
		menuitemlist.add(menuitem);
	}

	/**
	 * checks if the menu should be displayed. Menu is displayed if at least one of
	 * the menu items should be shown (not hiddent)
	 * 
	 * @param input  input data for the page
	 * @param buffer security buffer of the server
	 * @return true if menu should be hidden
	 */
	public boolean hide(SPageData input, SecurityBuffer buffer) {
		for (int i = 0; i < menuitemlist.size(); i++) {
			if (!menuitemlist.get(i).hideComponent(input, buffer))
				return false;
		}
		return true;
	}

	/**
	 * writes the menu content inside a message
	 * 
	 * @param writer writer of the message
	 * @param input  page input data
	 * @param buffer security buffer
	 * @throws IOException if any error is encountered during the transmission
	 */
	public void writeToOLcMessage(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.startStructure("MENU");
		writer.addStringField("LBL", label);
		writer.startStructure("ITMS");
		for (int i = 0; i < menuitemlist.size(); i++) {
			SMenuItem menuitem = menuitemlist.get(i);
			if (!menuitem.hideComponent(input, buffer)) {
				writer.startStructure("ITM");
				menuitem.writeToOLcMessage(writer, input, buffer);
				writer.endStructure("ITM");
			}
		}
		writer.endStructure("ITMS");
		writer.endStructure("MENU");

	}

}
