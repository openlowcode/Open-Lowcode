/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.action.ConfirmotpAction;
import org.openlowcode.module.system.action.generated.AbsConfirmotpAction.ActionRef;
import org.openlowcode.module.system.page.generated.AbsEnterotppagePage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * A page to enter the OTP for the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class EnterotpPage
		extends
		AbsEnterotppagePage {

	
	
	/**
	 * creates the page
	 */
	public EnterotpPage() {
		super();
	}

	@Override
	public String generateTitle() {

		return "Enter OTP";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Create secure connection", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText("Enter your one-time password below to create a secure connection.", SPageText.TYPE_NORMAL, this));
		ActionRef confirmotpaction = ConfirmotpAction.get().getActionRef();
		STextField otp = new STextField("One-time password", "PASSWORD", "your password", 80, "", this,confirmotpaction);
		otp.hideDisplay();
		mainband.addElement(otp);
		confirmotpaction.setOtp(otp.getTextInput());
		SActionButton ok = new SActionButton("Confirm", confirmotpaction, this);
		mainband.addElement(ok);
		return mainband;
	}

}
