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
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * A widget to store a string on the page without showing it, to be used as
 * argument for an action on the page.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SStringStorage
		extends
		SPageNode
		implements
		SDefaultPath {

	private String name;
	private TextDataElt inputdata;

	/**
	 * creates a string storage widget
	 * 
	 * @param name       unique name of the widget
	 * @param parentpage parent page
	 * @param inputdata  text to store in the widget
	 */
	public SStringStorage(String name, SPage parentpage, TextDataElt inputdata) {
		super(parentpage);
		this.name = name;
		this.inputdata = inputdata;
	}

	/**
	 * get text content reference to be used as argument to an action on a page
	 * 
	 * @return text content reference
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getTextInput() {
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		inputdata.writeReferenceToCML(writer);

	}

	@Override
	public String getWidgetCode() {
		return "STRSTO";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
