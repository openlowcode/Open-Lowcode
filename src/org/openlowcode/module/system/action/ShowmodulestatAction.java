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

import org.openlowcode.module.system.action.generated.AbsShowmodulestatAction;
import org.openlowcode.module.system.data.Modulereport;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.ShowmodulestatPage;
import org.openlowcode.server.data.storage.QueryFilter;

import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Action to show the module stats, mostly the version of the modules on this
 * server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowmodulestatAction
		extends
		AbsShowmodulestatAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ShowmodulestatAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		int modulenumber = OLcServer.getServer().getModuleNumber();
		Modulereport[] modulereports = new Modulereport[modulenumber];
		for (int i = 0; i < modulenumber; i++) {
			SModule module = OLcServer.getServer().getModule(i);
			modulereports[i] = new Modulereport();
			modulereports[i].setCode(module.getCode());
			modulereports[i].setCompiledate(module.getGenerationdate());
			modulereports[i].setModuleversion(module.getModuleversion());
			modulereports[i].setStable((module.isFrameworkfinalversion() ? BooleanChoiceDefinition.get().YES
					: BooleanChoiceDefinition.get().NO));
			modulereports[i].setName(module.getLabel());
			modulereports[i].setVersion(module.getFrameworkversion());

		}
		return new ActionOutputData(modulereports);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ShowmodulestatPage(logicoutput.getModulereport());
	}

}
