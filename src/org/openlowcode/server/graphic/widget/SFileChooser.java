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
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;

/**
 * a widget allowing to choose a file in the local system
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SFileChooser
		extends
		SPageNode {
	private String title;
	private String id;

	/**
	 * create a file chooser widget
	 * 
	 * @param parentpage parent page
	 * @param id         id of the widget, should be unique for the parent namespace
	 * @param title      title of the file chooser
	 */
	public SFileChooser(SPage parentpage, String id, String title) {
		super(parentpage);
		this.id = id;
		this.title = title;
	}

	/**
	 * get the reference to the file to be used as input for an action
	 * 
	 * @return the reference of the file to be used in an action
	 */
	public Function<
			SActionInputDataRef<LargeBinaryDataEltType>, SActionDataLoc<LargeBinaryDataEltType>> getLargeBinaryInput() {
		return (a) -> (new SActionDataLoc<LargeBinaryDataEltType>(this, a));
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(id, this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("ID", id);
		writer.addStringField("TTL", title);

	}

	@Override
	public String getWidgetCode() {
		return "FLC";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
