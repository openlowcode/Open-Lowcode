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

import org.openlowcode.module.system.action.generated.AbsGetfrontpagedataAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.page.Frontpage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * gets the data from the default front-page (showing the active tasks for the
 * user)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GetfrontpagedataAction
		extends
		AbsGetfrontpagedataAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public GetfrontpagedataAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		Task[] activetask = ActivetaskcomplexqueryAction.get().executeActionLogic(null, null).getTask();
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		if (userid == null)
			throw new RuntimeException("Object with creationlog cannot be used in context with ip = "
					+ OLcServer.getServer().getIpForConnection() + ", cid = "
					+ OLcServer.getServer().getCidForConnection());
		Appuser user = Appuser.readone(userid);

		String frontpagemessage = "Connected as " + user.getNr() + " (" + user.getObjectname() + ") \n\n"
				+ OLcServer.getServer().getMainmodule().getFrontPageMessage();

		ActionOutputData outputdata = new ActionOutputData(frontpagemessage, activetask);
		return outputdata;
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new Frontpage(logicoutput.getSpecificmessage(), logicoutput.getActivetasks());
	}

}
