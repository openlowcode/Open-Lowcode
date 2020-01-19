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

import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * an item in a menu to show in the application
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SMenuItem {
	private SActionRef actionforitem;
	private String label;

	/**
	 * creates a menu item
	 * 
	 * @param parentpage    parent page
	 * @param label         label to show
	 * @param actionforitem action to triggered when menu is pressed
	 */
	public SMenuItem(SPage parentpage, String label, SActionRef actionforitem) {
		this.label = label;
		this.actionforitem = actionforitem;
	}

	/**
	 * hide the component if not authorized
	 * 
	 * @param input  page data input
	 * @param buffer security buffer holding all users privileges
	 * @return true if component should be hidden
	 */
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		ActionSecurityManager[] securitymanagers = actionforitem.getAction().getActionSecurityManager();
		if (securitymanagers == null)
			return true;
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			SActionData actiondata = actionforitem.generatePotentialActionDataForSecurity(input);
			if (thismanager.isAuthorizedForCurrentUser("SMenuItem " + this.label, actiondata, buffer))
				return false;
		}
		return true;
	}

	/**
	 * writes in a message the content of the menu item
	 * 
	 * @param writer writer of the message
	 * @param input  input page data
	 * @param buffer input security buffer
	 * @throws IOException if any exception is encountered during the transmission
	 */
	public void writeToOLcMessage(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.startStructure("MNUITM");
		writer.addStringField("LBL", label);
		writer.startStructure("ACN");
		actionforitem.writeToCML(writer);
		writer.endStructure("ACN");
		writer.endStructure("MNUITM");

	}

}
