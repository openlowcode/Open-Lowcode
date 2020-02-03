/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.HashMap;
import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsCopyuserstogroupAction;
import org.openlowcode.module.system.action.generated.AtgShowusergroupAction;
import org.openlowcode.module.system.data.Groupadminlink;
import org.openlowcode.module.system.data.Groupmemberlink;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * Copy all the users of a group to another
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CopyuserstogroupAction
		extends
		AbsCopyuserstogroupAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public CopyuserstogroupAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			DataObjectId<Usergroup> origingroupid,
			DataObjectId<Usergroup> targetgroupid,
			Function<TableAlias, QueryFilter> datafilter) {

		Groupadminlink[] origingroupadmins = Groupadminlink.getalllinksfromleftid(origingroupid, null);
		Groupmemberlink[] origingroupmembers = Groupmemberlink.getalllinksfromleftid(origingroupid, null);

		Groupadminlink[] targetgrouporiginadmins = Groupadminlink.getalllinksfromleftid(targetgroupid, null);
		Groupmemberlink[] targetgrouporiginmembers = Groupmemberlink.getalllinksfromleftid(targetgroupid, null);
		HashMap<String, String> targetgrouporiginadminsperid = new HashMap<String, String>();
		for (int i = 0; i < targetgrouporiginadmins.length; i++)
			targetgrouporiginadminsperid.put(targetgrouporiginadmins[i].getRgid().getId(), "VOID");

		HashMap<String, String> targetgrouporiginmembersperid = new HashMap<String, String>();
		for (int i = 0; i < targetgrouporiginmembers.length; i++)
			targetgrouporiginmembersperid.put(targetgrouporiginmembers[i].getRgid().getId(), "VOID");

		for (int i = 0; i < origingroupadmins.length; i++) {
			Groupadminlink origingroupadmin = origingroupadmins[i];
			if (!targetgrouporiginadminsperid.containsKey(origingroupadmin.getRgid().getId())) {
				Groupadminlink newadminlinkfortarget = new Groupadminlink();
				newadminlinkfortarget.setrightobject(origingroupadmin.getRgid());
				newadminlinkfortarget.setleftobject(targetgroupid);
				newadminlinkfortarget.insert();
			}
		}
		for (int i = 0; i < origingroupmembers.length; i++) {
			Groupmemberlink origingroupmember = origingroupmembers[i];
			if (!targetgrouporiginmembersperid.containsKey(origingroupmember.getRgid().getId())) {
				Groupmemberlink newmemberlinkfortarget = new Groupmemberlink();
				newmemberlinkfortarget.setrightobject(origingroupmember.getRgid());
				newmemberlinkfortarget.setleftobject(targetgroupid);
				newmemberlinkfortarget.insert();
			}
		}

		return new ActionOutputData(targetgroupid);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return AtgShowusergroupAction.get().executeAndShowPage(logicoutput.getTargetgroupid_thru());
	}

}
