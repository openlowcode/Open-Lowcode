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

import org.openlowcode.module.system.action.LoginAction;
import org.openlowcode.module.system.page.generated.AbsSimpleloginPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;
import org.openlowcode.server.graphic.widget.STextStorage;

/**
 * The simple login page for the application. Allows to authenticate with user
 * and password
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleloginPage
		extends
		AbsSimpleloginPage {

	/**
	 * displays the simple login page, with potentially the context action
	 * 
	 * @param contextaction context action encoded in an Open Lowcode message
	 */
	public SimpleloginPage(String contextaction) {
		super(contextaction);
	}

	@Override
	public SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		LoginAction.ActionRef login = LoginAction.get().getActionRef();
		STextField user = new STextField("User", "USER", "your user account.", 80, "", this, login);
		STextField password = new STextField("Password", "PASSWORD", "your password", 80, "", this, login);
		
		password.hideDisplay();
		STextField otp = new STextField("One-Time Password","OTP","Please enter One-Time password (OTP) to access secured apps",80,"",this,login);
		otp.hideDisplay();
		otp.setShowHelperBefore();
		STextStorage context = new STextStorage("CONTEXT", this, this.getContextaction());
		mainband.addElement(context);
		login.setUser(user.getTextInput());
		login.setPassword(password.getTextInput());
		
		login.setOtp(otp.getTextInput());
		login.setContextaction(context.getTextInput());
		SActionButton send = new SActionButton("OK", login, this);
		mainband.addElement(new SPageText("Login", SPageText.TYPE_TITLE, this));

		mainband.addElement(user);
		mainband.addElement(password);
		mainband.addElement(otp);
		mainband.addElement(send);
		mainband.addElement(new SPageText("Welcome to Open Lowcode Server.", SPageText.TYPE_NORMAL, this));
		return mainband;
	}

	@Override
	public String generateTitle(String contextaction) {
		return "Login";
	}

}
