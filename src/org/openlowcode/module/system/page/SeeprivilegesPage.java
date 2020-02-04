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

import org.openlowcode.module.system.action.generated.AtgShowauthorityAction;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.page.generated.AbsSeeprivilegesPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * A page to show privileges of the current user. This is in free access
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SeeprivilegesPage
		extends
		AbsSeeprivilegesPage {

	/**
	 * creates the page
	 * 
	 * @param usersummary     summary of the user (id, name...)
	 * @param userauthorities the authorities granted by privilege to this user
	 */
	public SeeprivilegesPage(String usersummary, Authority[] userauthorities) {
		super(usersummary, userauthorities);

	}

	@Override
	public String generateTitle(String usersummary, Authority[] userauthorities) {

		return "Privileges for " + usersummary;
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Your privileges", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText(this.getUsersummary(), SPageText.TYPE_NORMAL, this));
		SObjectArray<Authority> privileges = new SObjectArray<Authority>("AUTHORITY", this.getUserauthorities(),
				Authority.getDefinition(), this);

		mainband.addElement(privileges);
		AtgShowauthorityAction.ActionRef showautorityaction = AtgShowauthorityAction.get().getActionRef();
		privileges.addDefaultAction(showautorityaction);
		showautorityaction.setId(privileges.getAttributeInput(Authority.getIdMarker()));
		mainband.addElement(new SActionButton("Show Authority", showautorityaction, this));
		return mainband;
	}

}
