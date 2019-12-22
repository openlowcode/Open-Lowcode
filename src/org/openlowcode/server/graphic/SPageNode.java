/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic;

import java.io.IOException;

import org.openlowcode.server.security.GalliumSecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * an element of a screen, as managed from the server side. Main capabilities
 * include:
 * <ul>
 * <li>provide business attributes necessary to display the page</li>
 * <li>write the content of the page in OLc Messages</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SPageNode {
	private SPageSignifPath path;
	private SPageSignifPath parentpath;
	private SPage parentpage;

	/**
	 * gets the parent page for this node
	 * @return the page
	 */
	public SPage getPage() {
		return parentpage;
	}

	/**
	 * this method should be recursively called from parent structure to all its
	 * children
	 * 
	 * @param parentpath       the parent significant path
	 * @param widgetpathtoroot an array of all the page nodes for traceability in
	 *                         case of problems
	 */
	public abstract void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot);

	public SPageNode[] addCurrentWidgetToRoot(SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = new SPageNode[widgetpathtoroot.length + 1];
		for (int i = 0; i < widgetpathtoroot.length; i++) {
			newwidgetpathtoroot[i] = widgetpathtoroot[i];
		}
		newwidgetpathtoroot[widgetpathtoroot.length] = this;
		return newwidgetpathtoroot;
	}

	/**
	 * creates a note for the specified page
	 * @param parentpage page this node is part of
	 */
	public SPageNode(SPage parentpage) {
		this.path = null;
		this.parentpage = parentpage;
	}

	/**
	 * writes the payload of the representation (without opening and closing
	 * structure)
	 * 
	 * @param writer the CDL writer on which to write the message
	 * @throws GalliumException
	 */
	public abstract void WritePayloadToCDL(MessageWriter writer, SPageData input, GalliumSecurityBuffer buffer)
			throws IOException;

	public abstract String getWidgetCode();

	public void WriteToCDL(MessageWriter writer, SPageData input, GalliumSecurityBuffer buffer) throws IOException {
		writer.startStructure(getWidgetCode());
		if (path == null) {
			writer.addStringField("SPT", "");
		} else {
			writer.addStringField("SPT", path.getName());
		}
		this.WritePayloadToCDL(writer, input, buffer);
		writer.endStructure(getWidgetCode());

	}

	/**
	 * @param path specifies the path for this node
	 */
	public void setSignifPath(SPageSignifPath path) {
		this.path = path;
	}

	/**
	 * @param parentpath specifies the path of the parent
	 */
	public void setParentSignifPath(SPageSignifPath parentpath) {
		this.parentpath = parentpath;
	}

	/**
	 * @return the text path
	 */
	public String printPath() {
		if (path == null)
			throw new RuntimeException(" path is null for SPageNode " + this.getWidgetCode() + " parent: "
					+ (parentpath != null ? parentpath.printPath() : "null"));
		return path.printPath();
	}



	/**
	 * This method allows to hide component in a layout if the user is not
	 * authorized
	 * 
	 * @return true if the component should be hidden, false is the component is to
	 *         be shown
	 */
	public abstract boolean hideComponent(SPageData input, GalliumSecurityBuffer buffer);

	public static final String DEFAULT_UNSAVED_EDITION_WARNING_MESSAGE = "Your data will be lost, do you really want to continue ?";
	public static final String DEFAULT_UNSAVED_EDITION_CONTINUE_MESSAGE = "Yes";
	public static final String DEFAULT_UNSAVED_EDITION_STOP_MESSAGE = "no";

}
