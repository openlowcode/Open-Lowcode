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

import org.openlowcode.module.system.action.generated.AbsModuleusagesummaryAction;
import org.openlowcode.module.system.data.Basicdiagramrecord;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.ReportingfrequencyChoiceDefinition;
import org.openlowcode.module.system.page.ModuleusagesummaryPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * generates a default module usage summary
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ModuleusagesummaryAction
		extends
		AbsModuleusagesummaryAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ModuleusagesummaryAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {

		return new ActionOutputData(
				SpecificmoduleusagesummaryAction.generatedata(BooleanChoiceDefinition.get().NO,
						ReportingfrequencyChoiceDefinition.get().DAILY, null).toArray(new Basicdiagramrecord[0]),
				BooleanChoiceDefinition.get().NO, ReportingfrequencyChoiceDefinition.get().DAILY, new Integer(0));
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ModuleusagesummaryPage(logicoutput.getStattable(), logicoutput.getExcludeadmin(),
				logicoutput.getFrequency(), logicoutput.getHistory());
	}

}
