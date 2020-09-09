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

import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * A widget to ensure a vertical component band will have the minimum width
 * specified.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.11
 */

public class SWidthProtector
		extends
		SPageNode
		implements
		SDefaultPath {
	private int minwidth;
	public SWidthProtector(int minwidth,SPage parentpage) {
		super(parentpage);
		this.minwidth = minwidth;
	}
	@Override
	public String getPathName() {
		return "WDP";
	}
	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		
	}
	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addIntegerField("MNW", minwidth);
		
	}
	@Override
	public String getWidgetCode() {
		return "WDP";
	}
	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}
	
}
