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

import org.openlowcode.server.action.SActionOutputDataRef;
import org.openlowcode.server.action.SInlineActionRef;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;

/**
 * a widget allowing to download a file from the server and open it in the
 * client temp folder
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class SFileDownloader
		extends
		SPageNode {

	private SInlineActionRef filedownloadactionref;
	private SActionOutputDataRef<LargeBinaryDataEltType> inlineoutputdata;
	private String id;

	/**
	 * creates a file downloader
	 * 
	 * @param id                    unique id of the download in the signifiant
	 *                              parent widget
	 * @param parentpage            parent page
	 * @param filedownloadactionref reference of the action providing the file
	 * @param inlineoutputdata      output data of the action providing the file to
	 *                              use as input for this component
	 */
	public SFileDownloader(
			String id,
			SPage parentpage,
			SInlineActionRef filedownloadactionref,
			SActionOutputDataRef<LargeBinaryDataEltType> inlineoutputdata) {
		super(parentpage);
		this.filedownloadactionref = filedownloadactionref;
		this.inlineoutputdata = inlineoutputdata;
		this.id = id;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(id, this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("ID", id);
		filedownloadactionref.writeToCML(writer);
		filedownloadactionref.writeReferenceToOutputCLM(writer, inlineoutputdata);

	}

	@Override
	public String getWidgetCode() {
		return "FLD";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
