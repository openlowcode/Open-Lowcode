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

import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * A node providing collapsible content.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SCollapsibleBand
		extends
		SPageNode {

	private SPageNode payload;
	private String title;
	private boolean openbydefault;
	private boolean closewheninlineactioninside;

	/**
	 * creates a collapsible band that does not close when inline action is
	 * triggered inside
	 * 
	 * @param parentpage    parent page
	 * @param payload       payload inside the collapsible pane
	 * @param title         title of the collapsible pane (when collapsed, only
	 *                      title text is shown)
	 * @param openbydefault true if collapsible pane should be opened by default
	 */
	public SCollapsibleBand(SPage parentpage, SPageNode payload, String title, boolean openbydefault) {
		this(parentpage, payload, title, openbydefault, false);
	}

	/**
	 * creates a collapsible band
	 * 
	 * @param parentpage                  parent page
	 * @param payload                     payload inside the collapsible pane
	 * @param title                       title of the collapsible pane (when
	 *                                    collapsed, only title text is shown)
	 * @param openbydefault               true if collapsible pane should be opened
	 *                                    by default
	 * @param closewheninlineactioninside close the band if inline action is
	 *                                    triggered inside
	 */
	public SCollapsibleBand(
			SPage parentpage,
			SPageNode payload,
			String title,
			boolean openbydefault,
			boolean closewheninlineactioninside) {
		super(parentpage);
		this.payload = payload;
		this.title = title;
		this.openbydefault = openbydefault;
		this.closewheninlineactioninside = closewheninlineactioninside;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageSignifPath pathforelements = parentpath;
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		payload.populateDown(pathforelements, newwidgetpathtoroot);

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("TTL", title);
		writer.addBooleanField("OBD", openbydefault);
		writer.addBooleanField("CII", closewheninlineactioninside);
		writer.startStructure("PLD");
		payload.WriteToCDL(writer, input, buffer);
		writer.endStructure("PLD");

	}

	@Override
	public String getWidgetCode() {
		return "CLB";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
