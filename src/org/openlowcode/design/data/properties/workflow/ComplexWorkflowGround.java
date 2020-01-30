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
 * A ground is a task ending a branch, without closing other potentially running
 * branches of the workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ComplexWorkflowGround
		extends
		WorkflowStep {
	/**
	 * creates a 'Ground' workflos step
	 * 
	 * @param parent parent workflow
	 */
	public ComplexWorkflowGround(ComplexWorkflow parent) {
		super(parent);
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws IOException {

		String taskvariable = this.gettaskid().toLowerCase();
		sg.wl("		// ----------------- ground ----------------------");
		sg.wl("		ComplexWorkflowGround<" + parentclass + "," + lifecycleclass + "ChoiceDefinition> " + taskvariable
				+ " ");
		sg.wl("			= new ComplexWorkflowGround<" + parentclass + "," + lifecycleclass + "ChoiceDefinition>();");
		sg.wl("");
	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module) throws IOException {

	}
}
