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

import org.openlowcode.module.system.action.generated.AbsShowcurrentuserAction;
import org.openlowcode.module.system.action.generated.AtgShowappuserAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * This action gets the current user being connected to the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowcurrentuserAction
		extends
		AbsShowcurrentuserAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ShowcurrentuserAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		DataObjectId<Appuser> ownuserid = OLcServer.getServer().getCurrentUserId();
		return new ActionOutputData(ownuserid);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return AtgShowappuserAction.get().executeAndShowPage(logicoutput.getOwnuser());
	}

}
