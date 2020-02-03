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

import org.openlowcode.module.system.action.generated.AbsDisplaygenericobjectAction;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;


/**
 * An action to display any object from its id
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DisplaygenericobjectAction extends AbsDisplaygenericobjectAction {

	/**
	 * Creates the action 
	 * 
	 * @param parent parent module
	 */
	public DisplaygenericobjectAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(@SuppressWarnings("rawtypes") DataObjectId genericid,Function<TableAlias,QueryFilter> datafilter)  {
		return new ActionOutputData(genericid);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput)  {
		return logicoutput.getGenericid_thru().getShowObjectPage();
	}

}
