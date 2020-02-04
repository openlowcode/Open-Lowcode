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

import org.openlowcode.module.system.action.CopyuserstogroupAction;
import org.openlowcode.module.system.action.generated.AtgShowusergroupAction;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.page.generated.AbsCopyuserstogroupPage;
import org.openlowcode.module.system.page.generated.AtgSearchusergroupPage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectIdStorage;
import org.openlowcode.server.graphic.widget.SObjectSearcher;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * page to choose the target group to copy all users from an origin group to a
 * target group
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CopyuserstogroupPage
		extends
		AbsCopyuserstogroupPage {

	/**
	 * creates the page
	 * 
	 * @param groupid_thru       the id of the origin group
	 * @param origingroupsummary a summary of the origin group (number, name, number
	 *                           of users...)
	 */
	public CopyuserstogroupPage(DataObjectId<Usergroup> groupid_thru, String origingroupsummary) {
		super(groupid_thru, origingroupsummary);
	}

	@Override
	public String generateTitle(DataObjectId<Usergroup> groupid_thru, String origingroupsummary) {
		return "Copy users from group " + origingroupsummary;
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SObjectSearcher<Usergroup> searchtargetgroup = AtgSearchusergroupPage.getsearchpanel(this, "SEARCHTARGET");
		mainband.addElement(new SPageText("Copy users to new group", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText(this.getOrigingroupsummary(), SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SPageText("Search Target group", SPageText.TYPE_TITLE, this));
		mainband.addElement(searchtargetgroup);
		SObjectIdStorage<Usergroup> origingroupidid = new SObjectIdStorage<Usergroup>("ORIGINGROUPID", this,
				this.getGroupid_thru());
		mainband.addElement(origingroupidid);
		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);
		AtgShowusergroupAction.ActionRef cancel = AtgShowusergroupAction.get().getActionRef();
		cancel.setId(origingroupidid.getObjectIdInput());
		CopyuserstogroupAction.ActionRef copyusers = CopyuserstogroupAction.get().getActionRef();
		copyusers.setOrigingroupid(origingroupidid.getObjectIdInput());
		copyusers.setTargetgroupid(searchtargetgroup.getresultarray().getAttributeInput(Usergroup.getIdMarker()));
		SActionButton cancelbutton = new SActionButton("Cancel", cancel, this);
		SActionButton copyusersbutton = new SActionButton("Copy users to target group", copyusers, this);
		buttonband.addElement(cancelbutton);
		buttonband.addElement(copyusersbutton);
		mainband.addElement(buttonband);
		return mainband;
	}

}
