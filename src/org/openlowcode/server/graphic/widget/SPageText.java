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
import org.openlowcode.tools.structure.TextDataElt;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SPageText
		extends
		SPageNode {
	private String text;
	private TextDataElt externaltext;

	private int type;
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_TITLE = 1;
	public static final int TYPE_WARNING = 2;
	private boolean isvisiblehelper;
	private boolean hasurl;
	private String url;
	private TextDataElt externalurl;
	private String visiblehelpertext;

	/**
	 * creates a text with a visible helper
	 * 
	 * @param text              text to show
	 * @param type              type of text (as defined in a static integer in this
	 *                          class)
	 * @param parent            parent class
	 * @param visiblehelpertext
	 */
	public SPageText(String text, int type, SPage parent, String visiblehelpertext) {
		this(text, type, parent);
		this.setVisibleHelper(visiblehelpertext);

	}

	/**
	 * creates a text showing the content of a page argument
	 * 
	 * @param externaltext the text input argument of the page to show
	 * @param type         type of text (as defined in a static integer in this
	 *                     class)
	 * @param parent       parent class
	 */
	public SPageText(TextDataElt externaltext, int type, SPage parent) {
		super(parent);
		this.text = null;
		boolean istypesupported = false;
		if (type == TYPE_NORMAL)
			istypesupported = true;
		if (type == TYPE_TITLE)
			istypesupported = true;
		if (type == TYPE_WARNING)
			istypesupported = true;

		if (!istypesupported)
			throw new RuntimeException("the type is not supported " + type);
		this.type = type;
		this.externaltext = externaltext;
		this.isvisiblehelper = false;
		hasurl = false;
	}

	/**
	 * create a link text
	 * 
	 * @param externaltext the text input argument of the page to show
	 * @param externalurl  an URL to show as link
	 * @param type         type of text (as defined in a static integer in this
	 *                     class)
	 * @param parent       parent class
	 */
	public SPageText(TextDataElt externaltext, TextDataElt externalurl, int type, SPage parent) {
		this(externaltext, type, parent);
		this.hasurl = true;
		this.externalurl = externalurl;
		this.url = null;
	}

	/**
	 * create a simple page text
	 * 
	 * @param text   text to show
	 * @param type   type of text (as defined in a static integer in this class)
	 * @param parent parent class
	 */
	public SPageText(String text, int type, SPage parent) {
		super(parent);
		this.text = text;
		this.externaltext = null;
		boolean istypesupported = false;
		if (type == TYPE_NORMAL)
			istypesupported = true;
		if (type == TYPE_TITLE)
			istypesupported = true;
		if (type == TYPE_WARNING)
			istypesupported = true;
		if (!istypesupported)
			throw new RuntimeException("the type is not supported " + type);
		this.type = type;
		this.isvisiblehelper = false;
		hasurl = false;
	}

	/**
	 * creates a constant URL link
	 * 
	 * @param text   text of the link
	 * @param url    address of the link
	 * @param type   type of text (as defined in a static integer in this class)
	 * @param parent parent class
	 */
	public SPageText(String text, String url, int type, SPage parent) {
		this(text, type, parent);
		this.url = url;
		this.hasurl = true;
		this.externalurl = null;
	}

	/**
	 * sets a visible helper text
	 * 
	 * @param visiblehelpertext text to show
	 */
	public void setVisibleHelper(String visiblehelpertext) {
		this.isvisiblehelper = true;
		this.visiblehelpertext = visiblehelpertext;

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {

		if (this.text != null) {
			writer.addStringField("DATA", "LOCAL");
			writer.addStringField("TEXT", text);
		} else {
			writer.addStringField("DATA", "EXT");
			externaltext.writeReferenceToCML(writer);
		}

		writer.addIntegerField("TYPE", type);
		writer.addBooleanField("VSH", isvisiblehelper);
		if (this.isvisiblehelper)
			writer.addStringField("HLT", this.visiblehelpertext);
		writer.addBooleanField("HUR", this.hasurl);
		if (this.hasurl) {
			if (this.url != null) {
				writer.addBooleanField("EXU", false);
				writer.addStringField("URL", this.url);
			} else {
				writer.addBooleanField("EXU", true);
				externalurl.writeReferenceToCML(writer);

			}
		}
	}

	@Override
	public String getWidgetCode() {
		return "PAGETEXT";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
	}

}
