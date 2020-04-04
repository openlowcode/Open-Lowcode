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
import java.util.logging.Logger;

import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;

/**
 * A button to trigger an action on a page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SActionButton
		extends
		SPageNode
		implements
		SDefaultPath {
	private String label;
	private String rollovertip; // note: can be void;
	private SActionRef actionref;
	private ChoiceDataElt<?> conditionalshow = null;
	private boolean forcepopupcloseforinline;
	private boolean hasconfirmationmessage = false;
	Logger logger = Logger.getLogger("");
	private String confirmationmessage;
	private String oklabel;
	private String kolabel;

	/**
	 * this method will ensure that if button is pressed for online action, the
	 * popup window is closed.
	 */
	public void setForcePopupCloseForInline() {
		this.forcepopupcloseforinline = true;
	}

	/**
	 * puts condition to show the button depending on data sent to the page
	 * 
	 * @param choiceDataElt a system boolean choice value. button will display if
	 *                      value is YES
	 */
	public void setConditionalShow(ChoiceDataElt<?> conditionalshow) {
		this.conditionalshow = conditionalshow;
	}

	/**
	 * will send a confirmation message when button is clicked with continue and
	 * cancel as options. Action will proceed only if continue is pressed
	 * 
	 * @param confirmationmessage the confirmation message
	 */
	public void setConfirmationMessage(String confirmationmessage) {
		setConfirmationMessage(confirmationmessage, "Continue", "Cancel");

	}

	/**
	 * sets a confirmation with defined OK and KO label. Action will proceed only if
	 * OK label is pressed
	 * 
	 * @param confirmationmessage message shown in confirmation popup
	 * @param oklabel             label to press to continue
	 * @param kolabel             label to press to stio
	 */
	public void setConfirmationMessage(String confirmationmessage, String oklabel, String kolabel) {
		this.hasconfirmationmessage = true;
		this.confirmationmessage = confirmationmessage;
		this.oklabel = oklabel;
		this.kolabel = kolabel;
	}

	/**
	 * creates an action button
	 * 
	 * @param label  label in main language
	 * @param action action reference
	 * @param parent parent page
	 */
	public SActionButton(String label, SActionRef action, SPage parent) {
		super(parent);
		if (parent == null)
			throw new RuntimeException(
					String.format("Incorrect syntax, Action in button %s has no parent page.", label));
		if (action == null)
			throw new RuntimeException(String.format("Incorrect syntax, Action in button %s in page %s is null.", label,
					parent.getName()));

		this.label = label;
		this.rollovertip = "";
		this.actionref = action;
		this.forcepopupcloseforinline = false;
	}

	/**
	 * creates an action button with the given roll-over tip
	 * 
	 * @param label       label in main language
	 * @param rollovertip tip shown in mouse roll-over
	 * @param action      action reference
	 * @param parent      parent page
	 */
	public SActionButton(String label, String rollovertip, SActionRef action, SPage parent) {
		super(parent);
		this.label = label;
		this.rollovertip = rollovertip;
		this.actionref = action;
		this.forcepopupcloseforinline = false;
	}

	/**
	 * creates an action button that will close the popup the button is part of
	 * 
	 * @param label                    label in main language
	 * @param action                   action reference
	 * @param forcepopupcloseforinline will close the popup window the button is
	 *                                 part on while executing an inline action
	 * @param parent                   parent page
	 */
	public SActionButton(String label, SActionRef action, boolean forcepopupcloseforinline, SPage parent) {
		super(parent);
		if (parent == null)
			throw new RuntimeException(
					String.format("Incorrect syntax, Action in button %s has no parent page.", label));
		if (action == null)
			throw new RuntimeException(String.format("Incorrect syntax, Action in button %s in page %s is null.", label,
					parent.getName()));

		this.label = label;
		this.rollovertip = "";
		this.actionref = action;
		this.forcepopupcloseforinline = forcepopupcloseforinline;
	}

	/**
	 * Creates an action button that will close the popup the button is part of and
	 * with a roll-over tip
	 * 
	 * @param label                    label in main language
	 * @param rollovertip              tip shown in mouse roll-over
	 * @param action                   action reference
	 * @param forcepopupcloseforinline will close the popup window the button is
	 *                                 part on while executing an inline action
	 * @param parent                   parent page
	 */
	public SActionButton(
			String label,
			String rollovertip,
			SActionRef action,
			boolean forcepopupcloseforinline,
			SPage parent) {
		super(parent);
		this.label = label;
		this.rollovertip = rollovertip;
		this.actionref = action;
		this.forcepopupcloseforinline = forcepopupcloseforinline;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LABEL", label);
		writer.addStringField("ROLLOVERTIP", rollovertip);
		writer.addBooleanField("FORCEPOPUPCLOSE", this.forcepopupcloseforinline);
		actionref.writeToCML(writer);
		if (conditionalshow != null) {
			writer.addBooleanField("CDS", true);
			conditionalshow.writeReferenceToCML(writer);
		} else {
			writer.addBooleanField("CDS", false);
		}
		writer.addBooleanField("HCF", this.hasconfirmationmessage);
		if (this.hasconfirmationmessage) {
			writer.addStringField("CFM", this.confirmationmessage);
			writer.addStringField("CFC", this.oklabel);
			writer.addStringField("CFS", this.kolabel);
		}
	}

	@Override
	public String getPathName() {
		return this.label;
	}

	@Override
	public String getWidgetCode() {
		return "ACTIONBUTTON";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		logger.info("---------- Checking security for SActionButton " + this.label);
		ActionSecurityManager[] securitymanagers = actionref.getAction().getActionSecurityManager();
		if (securitymanagers == null) {
			logger.info(" no security manager, hide = true");
			return true;
		}
		if (buffer == null)
			return false; // this happens only on login page
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			SActionData actiondata = actionref.generatePotentialActionDataForSecurity(input);
			if (thismanager.isAuthorizedForCurrentUser("SActionButton " + this.label, actiondata, buffer)) {
				logger.info(" found security manager with authorization for user " + thismanager.toString());
				return false;
			}
		}
		return true;

	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		// button has no children, nothing to do
	}
}
