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

import org.openlowcode.module.system.action.generated.AbsLaunchcleanpendingemailsAction;
import org.openlowcode.module.system.data.Email;
import org.openlowcode.module.system.data.choice.EmailstatusChoiceDefinition;
import org.openlowcode.module.system.page.CleanpendingemailsPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.LifecycleQueryHelper;
import org.openlowcode.server.data.properties.StoredobjectQueryHelper;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * launch the clean pending e-mail page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LaunchcleanpendingemailsAction
		extends
		AbsLaunchcleanpendingemailsAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LaunchcleanpendingemailsAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		Email[] openmails = Email.getallactive(

				QueryFilter.get(LifecycleQueryHelper.get().getStateSelectionQueryCondition(
						Email.getDefinition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive),
						new ChoiceValue[] { EmailstatusChoiceDefinition.getChoiceReadytosend(),
								EmailstatusChoiceDefinition.getChoiceError(),
								EmailstatusChoiceDefinition.getChoiceSending() },
						Email.getDefinition())));
		return new ActionOutputData(openmails);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new CleanpendingemailsPage(logicoutput.getPendingemails());
	}

}
