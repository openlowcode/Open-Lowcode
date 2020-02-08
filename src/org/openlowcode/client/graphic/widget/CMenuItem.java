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

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.runtime.PageActionManager;

import javafx.stage.Window;
import javafx.scene.control.MenuItem;

/**
 * create a menu item as part of a CMenuBar widget
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CMenuItem {
	private String label;
	private CPageAction action;

	/**
	 * Create a menu item from the server message
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CMenuItem(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {

		label = reader.returnNextStringField("LBL");
		reader.returnNextStartStructure("ACN");
		reader.returnNextStartStructure("ACTION");
		action = new CPageAction(reader);
		reader.returnNextEndStructure("ACN");
		reader.returnNextEndStructure("MNUITM");
	}

	/**
	 * get a javafx menu item
	 * 
	 * @param actionmanager page action manager
	 * @param inputdata     input data
	 * @param parentwindow  parent window
	 * @return javafx menu item
	 */
	public MenuItem getMenuItem(PageActionManager actionmanager, CPageData inputdata, Window parentwindow) {
		MenuItem menuitem = new MenuItem(label);
		menuitem.setOnAction(actionmanager);
		actionmanager.registerEvent(menuitem, action);
		return menuitem;
	}

}
