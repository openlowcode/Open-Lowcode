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

import org.openlowcode.module.system.page.generated.AbsOtpokPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * A page to notify that the high-security check has already been done
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.10
 *
 */
public class OtpokPage
		extends
		AbsOtpokPage {

	public OtpokPage() {
		super();
	}

	@Override
	public String generateTitle() {

		return "You already have high-security connection";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Your high-security connection is active", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText(
				"You already entered a valid One-Time Password (OTP) today for your computer. No further step is required to access secure applications on the server.",
				SPageText.TYPE_NORMAL, this));
		return mainband;
	}

}
