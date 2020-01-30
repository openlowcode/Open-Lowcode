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
 * A task closing a complex workflow. Even if there are tasks pending on another
 * branch, will close the workflow and suppressed all running tasks
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ComplexWorkflowClose
		extends
		WorkflowStep {

	/**
	 * Creates a complex workflow close
	 * 
	 * @param parent parent workflow
	 */
	public ComplexWorkflowClose(ComplexWorkflow parent) {
		super(parent);
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws IOException {

		String taskvariable = this.gettaskid().toLowerCase();
		sg.wl("		// ----------------- close ----------------------");
		sg.wl("		ComplexWorkflowClose<" + parentclass + "," + lifecycleclass + "ChoiceDefinition> " + taskvariable
				+ " ");
		sg.wl("			= new ComplexWorkflowClose<" + parentclass + "," + lifecycleclass + "ChoiceDefinition>();");
		sg.wl("");
	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module) throws IOException {

	}

}
