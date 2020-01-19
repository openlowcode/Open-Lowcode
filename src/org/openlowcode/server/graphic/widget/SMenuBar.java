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
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;


/**
 * A menu bar is made of several menus themselved made of several menu items
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SMenuBar extends SPageNode {
	private ArrayList<SMenu> childrenmenus;
	/**
	 * creates a menu bar widget
	 * 
	 * @param parentpage parent widget
	 */
	public SMenuBar(SPage parentpage) {
		super(parentpage);
		childrenmenus = new ArrayList<SMenu>();
		
	}
	/**
	 * adds a menu in the menubar
	 * 
	 * @param menu menu to add
	 */
	public void addSMenu(SMenu menu) {
		this.childrenmenus.add(menu);
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot)  {

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer,SPageData input,SecurityBuffer buffer) throws IOException {
		writer.startStructure("MENUS");
		for (int i=0;i<childrenmenus.size();i++) {
			SMenu thismenu = childrenmenus.get(i);
			if (!thismenu.hide(input,buffer)) {
			writer.startStructure("MENU");
			thismenu.writeToOLcMessage(writer,input,buffer);
			writer.endStructure("MENU");
			}
		}
		writer.endStructure("MENUS");
		

	}

	@Override
	public String getWidgetCode() {
		
		return "MNUBAR";
	}

	@Override
	public boolean hideComponent(SPageData input,SecurityBuffer buffer)  {
		return false;
	}

}
