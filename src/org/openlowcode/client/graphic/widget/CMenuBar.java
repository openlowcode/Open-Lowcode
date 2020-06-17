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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import javafx.stage.Window;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;

/**
 * creates a MenuBar widget
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CMenuBar
		extends
		CPageNode {
	private ArrayList<CMenu> listofmenus;

	/**
	 * creates a menu bar from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CMenuBar(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		listofmenus = new ArrayList<CMenu>();
		reader.startStructureArray("MENU");
		while (reader.structureArrayHasNextElement("MENU")) {
			// directly takes menu
			reader.returnNextStartStructure("MENU");
			listofmenus.add(new CMenu(reader, parentpath));
			reader.returnNextEndStructure("MENU");
		}
		reader.returnNextEndStructure("MNUBAR");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public MenuBar getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		MenuBar menubar = new MenuBar();
		menubar.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		for (int i = 0; i < listofmenus.size(); i++) {
			menubar.getMenus().add(listofmenus.get(i).getMenu(actionmanager, inputdata, parentwindow));
		}
		return menubar;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		return null;
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {

	}

	@Override
	public void mothball() {
	}

}
