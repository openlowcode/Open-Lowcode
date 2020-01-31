/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.data.properties.basic.ComplexWorkflow;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * a switch will go to one branch of the workflow or another depending on a
 * condition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Switch
		extends
		WorkflowStep {
	private SwitchCondition condition;

	/**
	 * creates a switch for the workflow for the given condition
	 * 
	 * @param parent    parent workflow
	 * @param condition condition of the switch
	 */
	public Switch(ComplexWorkflow parent, SwitchCondition condition) {
		super(parent);
		this.condition = condition;
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {
		condition.writeimport(sg, module);

	}

	@Override
	public void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws IOException {
		String taskvariable = this.gettaskid().toLowerCase();
		condition.writedeclaration(sg, module, parentclass, lifecycleclass, taskvariable);

	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module) throws IOException {
		condition.writeNextSteps(sg, module, this.gettaskid().toLowerCase());

	}

}
