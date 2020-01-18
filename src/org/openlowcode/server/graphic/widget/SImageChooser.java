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
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;

/**
 * A component with the following capabilities:
 * <ul>
 * <li>read a picture from clipboard</li>
 * <li>read a picture copy pasted from the file explorer (not implemented in
 * this version)</li>
 * <li>possibility to select full picture or perform an on-screen selection</li>
 * <li>generation of a thumbnail and a full picture in PNG format (as better for
 * text and blocking bug currently with potential competitor format jpeg
 * (smaller but also less nice with text)</li>
 * </ul>
 * The imagechooser will launch an action after image is selected
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SImageChooser
		extends
		SPageNode {
	private int thumbnailsize;
	private String title;
	private SActionRef actiontolaunch;
	private String id;

	/**
	 * creates an impage chooser
	 * 
	 * @param id             unique name of the image chooser in the parent widget
	 *                       namespace
	 * @param parentpage     parent page
	 * @param thumbnailsize  size of the thumbnail to generate (biggest dimension in
	 *                       pixel)
	 * @param actiontolaunch action to launch after selection
	 * @param title          title of the image chooser widget
	 */
	public SImageChooser(String id, SPage parentpage, int thumbnailsize, SActionRef actiontolaunch, String title) {
		super(parentpage);
		this.thumbnailsize = thumbnailsize;
		this.title = title;
		this.actiontolaunch = actiontolaunch;
		this.id = id;
	}

	/**
	 * a reference to the full image to be used as action input
	 * 
	 * @return reference to the full image
	 */
	public Function<
			SActionInputDataRef<LargeBinaryDataEltType>,
			SActionDataLoc<LargeBinaryDataEltType>> getFullImageDataInput() {
		return (a) -> (new SActionDataLoc<LargeBinaryDataEltType>(this, a, "FULL"));
	}

	/**
	 * a reference to the thumbnail image to be used as action input
	 * 
	 * @return reference to the thumbnail image
	 */
	public Function<
			SActionInputDataRef<LargeBinaryDataEltType>,
			SActionDataLoc<LargeBinaryDataEltType>> getThumbnailImageDataInput() {
		return (a) -> (new SActionDataLoc<LargeBinaryDataEltType>(this, a, "THUMBNAIL"));
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(id, this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("ID", id);
		writer.addIntegerField("TBNSIZ", thumbnailsize);
		writer.addStringField("TTL", title);
		actiontolaunch.writeToCML(writer);
	}

	@Override
	public String getWidgetCode() {

		return "IMC";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

}
