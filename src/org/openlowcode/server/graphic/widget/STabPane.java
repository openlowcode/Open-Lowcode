/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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
 * a pane with several tabs at the top. Each tab has a title and can be selected
 * by the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class STabPane
		extends
		SPageNode {
	private String nameforpath;
	private String callstackatcreation;
	private ArrayList<SPageNode> tabcontentlist;
	private ArrayList<String> tabtitlelist;

	public STabPane(SPage parentpage) {
		this(parentpage, null);
	}

	/**
	 * Creates a new empty tab pane
	 * 
	 * @param parentpage  parent page
	 * @param nameforpath name for the path (must be unique for the parent)
	 */
	public STabPane(SPage parentpage, String nameforpath) {
		super(parentpage);
		tabcontentlist = new ArrayList<SPageNode>();
		tabtitlelist = new ArrayList<String>();
		this.nameforpath = nameforpath;
		this.callstackatcreation = Thread.currentThread().getStackTrace()[2].toString();
	}

	/**
	 * adds an element to the tab pane
	 * 
	 * @param element  page node that is the content of the tab pane
	 * @param tabtitle title of the tab
	 */
	public void addElement(SPageNode element, String tabtitle) {
		this.tabcontentlist.add(element);
		this.tabtitlelist.add(tabtitle);

	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageSignifPath pathforelements = parentpath;
		if (this.nameforpath != null) {
			pathforelements = new SPageSignifPath(nameforpath, this.getPage(), parentpath, widgetpathtoroot);
			this.setSignifPath(pathforelements);
		}
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);

		for (int i = 0; i < this.tabcontentlist.size(); i++) {
			this.tabcontentlist.get(i).populateDown(pathforelements, newwidgetpathtoroot);
		}

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.startStructure("ELTS");
		for (int i = 0; i < this.tabcontentlist.size(); i++) {
			SPageNode currentelement = this.tabcontentlist.get(i);
			if (!currentelement.hideComponent(input, buffer)) {
				writer.startStructure("ELT");
				writer.addStringField("TABNAME", this.tabtitlelist.get(i));
				currentelement.WriteToCDL(writer, input, buffer);
				writer.endStructure("ELT");
			}
		}
		writer.endStructure("ELTS");

	}

	@Override
	public String getWidgetCode() {
		return "TABPANE";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public String toString() {
		return "SCOMPONENTBAND[" + callstackatcreation + "]";
	}
}
