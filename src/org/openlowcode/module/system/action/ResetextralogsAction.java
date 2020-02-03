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

import org.openlowcode.module.system.action.generated.AbsResetextralogsAction;
import org.openlowcode.module.system.page.AddlogsPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Reset the extra logs on the server and goes back to normal level of logging
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ResetextralogsAction
		extends
		AbsResetextralogsAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ResetextralogsAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		OLcServer.getServer().removeLogExceptions(true);
		OLcServer.getServer().removeLogExceptions(false);
		return new ActionOutputData();
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new AddlogsPage();
	}

}
