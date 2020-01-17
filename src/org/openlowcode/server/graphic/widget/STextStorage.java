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
 * a widget to store, without showing it, some text on the page to be used by
 * further actions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class STextStorage
		extends
		SPageNode
		implements
		SDefaultPath {
	private String name;
	private TextDataElt inputdata;

	/**
	 * creates a new text storage
	 * 
	 * @param name       name of the widget (must be unique in parent widget)
	 * @param parentpage parent page
	 * @param inputdata  input data to store in the text storage
	 */
	public STextStorage(String name, SPage parentpage, TextDataElt inputdata) {
		super(parentpage);
		this.name = name;
		this.inputdata = inputdata;
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

		return "TEXTST";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

	/**
	 * provides the content of this text storage as the argument for an action
	 * 
	 * @return a reference to the content of this text storage
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getTextInput() {
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
