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

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.module.system.page.generated.AbsShowuserstatPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SObjectDisplay;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * A page showing a user and its sessions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowuserstatPage
		extends
		AbsShowuserstatPage {

	/**
	 * creates the page to show user stats
	 * 
	 * @param appuser     application user
	 * @param usersession sessions of this application user
	 */
	public ShowuserstatPage(Appuser appuser, Usersession[] usersession) {
		super(appuser, usersession);

	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("User", SPageText.TYPE_TITLE, this));
		mainband.addElement(
				new SObjectDisplay<Appuser>("USER", this.getAppuser(), Appuser.getDefinition(), this, true));
		mainband.addElement(new SPageText("Connections to this server", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SObjectArray<Usersession>("USERSESSIONS", this.getUsersession(),
				Usersession.getDefinition(), this));

		return mainband;
	}

	@Override
	public String generateTitle(Appuser appuser, Usersession[] usersession) {
		return "Show statistics for user " + appuser.getName() + " (" + appuser.getNr();
	}

}
