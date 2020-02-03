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

import org.openlowcode.module.system.action.generated.AbsLaunchtechnicaltoolsAction;
import org.openlowcode.module.system.page.TechnicaltoolsPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * Launching the technical tools page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LaunchtechnicaltoolsAction extends AbsLaunchtechnicaltoolsAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LaunchtechnicaltoolsAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		return new ActionOutputData();
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput)  {
		return new TechnicaltoolsPage();
	}

}
