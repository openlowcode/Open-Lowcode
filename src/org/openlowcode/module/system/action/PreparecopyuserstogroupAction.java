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

import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsPreparecopyuserstogroupAction;
import org.openlowcode.module.system.data.Groupadminlink;
import org.openlowcode.module.system.data.Groupmemberlink;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.page.CopyuserstogroupPage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * an action to prepare data on the origin group to open the copy users to group
 * page.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PreparecopyuserstogroupAction
		extends
		AbsPreparecopyuserstogroupAction {

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public PreparecopyuserstogroupAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			DataObjectId<Usergroup> groupid,
			Function<TableAlias, QueryFilter> datafilter) {
		Usergroup group = Usergroup.readone(groupid);
		Groupadminlink[] adminlinks = Groupadminlink.getalllinksfromleftid(groupid, null);
		Groupmemberlink[] memberlinks = Groupmemberlink.getalllinksfromleftid(groupid, null);

		String origingroupsummary = group.getNr() + " - " + group.getName() + " (" + adminlinks.length + " admin(s), "
				+ memberlinks.length + " member(s) )";
		return new ActionOutputData(groupid, origingroupsummary);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new CopyuserstogroupPage(logicoutput.getGroupid_thru(), logicoutput.getOrigingroupsummary());
	}

}
