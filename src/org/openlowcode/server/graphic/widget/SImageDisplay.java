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
import org.openlowcode.tools.structure.LargeBinaryDataElt;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;

/**
 * This widget will display an image (typically the thumbnail), and offer the
 * possibility to display a second image in a popup with possibility to save the
 * file. This widget will manage the fact the image may be null and then does
 * not display anything
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class SImageDisplay
		extends
		SPageNode {
	private SInlineActionRef secondimagedisplayactionref;
	private LargeBinaryDataElt inputdata;
	private SActionOutputDataRef<LargeBinaryDataEltType> inlineoutputdata;
	private String id;
	private boolean isimagepopup;
	private boolean islabel;
	private String label;

	/**
	 * creates an image display widget
	 * 
	 * @param id         unique name of the widget in the scope of the next parent
	 *                   significant widget
	 * @param parentpage parent page
	 * @param inputdata  binary input attribute of the page
	 */
	public SImageDisplay(String id, SPage parentpage, LargeBinaryDataElt inputdata) {
		super(parentpage);
		this.id = id;
		this.inputdata = inputdata;
		this.secondimagedisplayactionref = null;
		this.inlineoutputdata = null;
		this.isimagepopup = false;
		this.islabel = false;
	}

	/**
	 * creates an image display widget allowing to trigger a wider image as popup
	 * when pressed (typically, thumbnail is shown on he object) without a label
	 * 
	 * @param id                          unique name of the widget in the scope of
	 *                                    the next parent significant widget
	 * @param parentpage                  parent page
	 * @param inputdata                   binary input attribute of the page for the
	 *                                    original image shown (the thumbnail)
	 * @param secondimagedisplayactionref reference to the inline action bringing
	 *                                    full content
	 * @param inlineoutputdata            output attribute of the inline action to
	 *                                    get content from to shown in popup.
	 */
	public SImageDisplay(
			String id,
			SPage parentpage,
			LargeBinaryDataElt inputdata,
			SInlineActionRef secondimagedisplayactionref,
			SActionOutputDataRef<LargeBinaryDataEltType> inlineoutputdata) {
		super(parentpage);
		this.id = id;
		this.inputdata = inputdata;
		this.secondimagedisplayactionref = secondimagedisplayactionref;
		this.inlineoutputdata = inlineoutputdata;
		this.isimagepopup = true;
		this.islabel = false;
	}

	/**
	 * creates an image display widget allowing to trigger a wider image as popup
	 * when pressed (typically, thumbnail is shown on he object) with a label
	 * 
	 * @param id                          unique name of the widget in the scope of
	 *                                    the next parent significant widget
	 * @param parentpage                  parent page
	 * @param inputdata                   binary input attribute of the page for the
	 *                                    original image shown (the thumbnail)
	 * @param secondimagedisplayactionref reference to the inline action bringing
	 *                                    full content
	 * @param inlineoutputdata            output attribute of the inline action to
	 *                                    get content from to shown in popup.
	 * @param label                       label of the widget (to be shown similarly
	 *                                    to a field with a label on the left)
	 */
	public SImageDisplay(
			String id,
			SPage parentpage,
			LargeBinaryDataElt inputdata,
			SInlineActionRef secondimagedisplayactionref,
			SActionOutputDataRef<LargeBinaryDataEltType> inlineoutputdata,
			String label) {
		super(parentpage);
		this.id = id;
		this.inputdata = inputdata;
		this.secondimagedisplayactionref = secondimagedisplayactionref;
		this.inlineoutputdata = inlineoutputdata;
		this.isimagepopup = true;
		this.islabel = false;
		if (label != null) {
			this.islabel = true;
			this.label = label;
		}

	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(id, this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("ID", id);
		inputdata.writeReferenceToCML(writer);
		writer.addBooleanField("PUP", this.isimagepopup);
		if (this.isimagepopup) {
			secondimagedisplayactionref.writeToCML(writer);
			secondimagedisplayactionref.writeReferenceToOutputCLM(writer, inlineoutputdata);

		}
		writer.addBooleanField("ISL", islabel);
		if (islabel)
			writer.addStringField("LBL", label);

	}

	@Override
	public String getWidgetCode() {
		return "IMD";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
