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
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;

/**
 * A pop-up button displays a child page as a pop-up when pressed.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SPopupButton extends SPageNode {
	private SPageNode payload;
	private String label;
	private String rollovertip;
	private boolean allowscroll;
	private boolean showunderwidget;
	private SActionRef mainpopupaction;

	/**
	 * sets the main popup action. When this action is launched, the popup will
	 * close.
	 * 
	 * @param mainpopupaction main action popup
	 */
	public void setMainPopupAction(SActionRef mainpopupaction) {
		this.mainpopupaction = mainpopupaction;
	}

	/**
	 * creates a popup button allowing scroll on the popup content
	 * 
	 * @param parentpage  parent page of the popup buttons
	 * @param payload     content of the popup window displayed when popup button is
	 *                    pressed
	 * @param label       label of the popup button
	 * @param rollovertip rollover tip for the popup button
	 */
	public SPopupButton(SPage parentpage, SPageNode payload, String label, String rollovertip) {
		super(parentpage);
		this.payload = payload;
		this.label = label;
		this.rollovertip = rollovertip;
		this.allowscroll = true;
		this.showunderwidget = false;
	}

	/**
	 * creates a popup button specifying if scroll on popup content is allowed or
	 * not
	 * 
	 * @param parentpage  parent page of the popup buttons
	 * @param payload     content of the popup window displayed when popup button is
	 *                    pressed
	 * @param label       label of the popup button
	 * @param rollovertip rollover tip for the popup button
	 * @param allowscroll true if scroll is allowed on the popup payload, false else
	 */
	public SPopupButton(SPage parentpage, SPageNode payload, String label, String rollovertip, boolean allowscroll) {
		super(parentpage);
		this.payload = payload;
		this.label = label;
		this.rollovertip = rollovertip;
		this.allowscroll = allowscroll;
		this.showunderwidget = false;
	}

	/**
	 * creates a popup button allowing scroll, with a specified main popup action
	 * 
	 * @param parentpage      parent page of the popup buttons
	 * @param payload         content of the popup window displayed when popup
	 *                        button is pressed
	 * @param label           label of the popup button
	 * @param rollovertip     rollover tip for the popup button
	 * @param mainpopupaction main popup action
	 */
	public SPopupButton(SPage parentpage, SPageNode payload, String label, String rollovertip,
			SActionRef mainpopupaction) {
		super(parentpage);
		this.payload = payload;
		this.label = label;
		this.rollovertip = rollovertip;
		this.allowscroll = true;
		this.mainpopupaction = mainpopupaction;
		this.showunderwidget = false;
	}

	/**
	 * creates a popup button specifying if scroll is allowed, and specifying the
	 * main popup action
	 * 
	 * @param parentpage      parent page of the popup buttons
	 * @param payload         content of the popup window displayed when popup
	 *                        button is pressed
	 * @param label           label of the popup button
	 * @param rollovertip     rollover tip for the popup button
	 * @param allowscroll     true if scroll is allowed or false if not
	 * @param mainpopupaction main popup action
	 */
	public SPopupButton(SPage parentpage, SPageNode payload, String label, String rollovertip, boolean allowscroll,
			SActionRef mainpopupaction) {
		super(parentpage);
		this.payload = payload;
		this.label = label;
		this.rollovertip = rollovertip;
		this.allowscroll = allowscroll;
		this.mainpopupaction = mainpopupaction;
		this.showunderwidget = false;
	}

	/**
	 * creates a popup button specifying if scroll is allowed, and specifying the
	 * main popup action
	 * 
	 * @param parentpage      parent page of the popup buttons
	 * @param payload         content of the popup window displayed when popup
	 *                        button is pressed
	 * @param label           label of the popup button
	 * @param rollovertip     rollover tip for the popup button
	 * @param allowscroll     true if scroll is allowed or false if not
	 * @param showunderwidget if true, content of the popup is shown under the
	 *                        button, if false, shown at the middle of the page
	 * @param mainpopupaction main popup action
	 */
	public SPopupButton(SPage parentpage, SPageNode payload, String label, String rollovertip, boolean allowscroll,
			boolean showunderwidget, SActionRef mainpopupaction) {
		super(parentpage);
		this.payload = payload;
		this.label = label;
		this.rollovertip = rollovertip;
		this.allowscroll = allowscroll;
		this.mainpopupaction = mainpopupaction;
		this.showunderwidget = showunderwidget;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		if (this.rollovertip == null)
			this.rollovertip = "";
		writer.addStringField("RLV", rollovertip);

		writer.startStructure("PPAGE");
		payload.WriteToCDL(writer, input, buffer);
		writer.endStructure("PPAGE");
		writer.addBooleanField("SCL", allowscroll);
		writer.addBooleanField("SUW", showunderwidget);
	}

	@Override
	public String getWidgetCode() {
		return "POPUPBTN";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		if (this.mainpopupaction != null) {
			ActionSecurityManager[] securitymanagers = mainpopupaction.getAction().getActionSecurityManager();
			if (securitymanagers == null)
				return true;
			if (buffer == null)
				return false; // this happens only on login page
			for (int i = 0; i < securitymanagers.length; i++) {
				ActionSecurityManager thismanager = securitymanagers[i];
				SActionData actiondata = mainpopupaction.generatePotentialActionDataForSecurity(input);
				if (thismanager.isAuthorizedForCurrentUser("SPopupButton " + this.label, actiondata, buffer))
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		payload.populateDown(parentpath, newwidgetpathtoroot);

	}

}
