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
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * A fix split with a main component and a side component. The side component
 * gets the minimum space necessary for layout
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SFixSplit
		extends
		SPageNode {
	private int direction;
	/**
	 * main component on the right
	 */
	public static final int DIRECTION_RIGHT = 1;
	/**
	 * main component on the bottom
	 */
	public static final int DIRECTION_DOWN = 2;
	/**
	 * main component on the top
	 */
	public static final int DIRECTION_UP = 3;
	/**
	 * main component on the left
	 */
	public static final int DIRECTION_LEFT = 4;

	private SPageNode secundarycomponent;
	private SPageNode maincomponent;

	/**
	 * creates a fix split component
	 * 
	 * @param direction direction indicates the side of the screen where the main
	 *                  component is shown. The secundary component is limited to
	 *                  the minimum side.
	 * @param parent    parent page for the widget
	 */
	public SFixSplit(int direction, SPage parent) {
		super(parent);
		this.direction = direction;

	}

	/**
	 * adds the main component
	 * 
	 * @param node main component node
	 */
	public void addMainComponent(SPageNode node) {
		this.maincomponent = node;

	}

	/**
	 * adds the secondary (side) component
	 * 
	 * @param node secondary component
	 */
	public void addSecundaryComponent(SPageNode node) {
		this.secundarycomponent = node;

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.sendMessageElement(new MessageStringField("DIR", "" + direction)); // TODO - would be better to have int
																					// attribute
		writer.startStructure("SEC");
		secundarycomponent.WriteToCDL(writer, input, buffer);
		writer.endStructure("FIXSPLIT");

		// ----------- main
		writer.startStructure("MAIN");
		maincomponent.WriteToCDL(writer, input, buffer);
		writer.endStructure("MAIN");

	}

	@Override
	public String getWidgetCode() {
		// TODO Auto-generated method stub
		return "FIXSPLIT";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		this.maincomponent.populateDown(parentpath, newwidgetpathtoroot);
		this.secundarycomponent.populateDown(parentpath, newwidgetpathtoroot);
	}

}