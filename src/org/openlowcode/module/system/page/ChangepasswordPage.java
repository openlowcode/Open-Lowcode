/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.action.ChangepasswordAction;
import org.openlowcode.module.system.page.generated.AbsChangepasswordPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * A page to show the result of a change password page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChangepasswordPage
		extends
		AbsChangepasswordPage {
	/**
	 * creates the page
	 * 
	 * @param message related to the change password
	 */
	public ChangepasswordPage(String message) {
		super(message);

	}

	@Override
	public String generateTitle(String message) {
		return "Change Password";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Change password", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText(this.getMessage(), SPageText.TYPE_NORMAL, this));
		ChangepasswordAction.ActionRef changepassword = ChangepasswordAction.get().getActionRef();
		STextField oldpassword = new STextField("Current password", "OLDPASSWORD", "your password", 80, "", false, this,
				false, false, false, null);
		changepassword.setOldpassword(oldpassword.getTextInput());
		STextField newpassword = new STextField("New password", "NEWPASSWORD", "your password", 80, "", false, this,
				false, false, false, null);
		changepassword.setNewpassword(newpassword.getTextInput());
		STextField newpasswordrepeat = new STextField("Repeat new password", "NEWPASSWORDREPEAT", "your password", 80,
				"", false, this, false, false, false, changepassword);
		changepassword.setNewpasswordrepeat(newpasswordrepeat.getTextInput());
		oldpassword.hideDisplay();
		newpassword.hideDisplay();
		newpasswordrepeat.hideDisplay();
		mainband.addElement(oldpassword);
		mainband.addElement(newpassword);
		mainband.addElement(newpasswordrepeat);
		SActionButton changepasswordbutton = new SActionButton("Change", changepassword, this);
		mainband.addElement(changepasswordbutton);
		return mainband;
	}

}
