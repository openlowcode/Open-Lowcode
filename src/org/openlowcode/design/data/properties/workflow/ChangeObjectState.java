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

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.ComplexWorkflow;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A workflow step changing the state of a workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChangeObjectState
		extends
		WorkflowStep {
	private DataObjectDefinition object;
	private ChoiceValue value;
	private WorkflowStep nextstep;

	/**
	 * create a change object state
	 * 
	 * @param parentworkflow parent workflow
	 * @param object         definition of the object that the workflow is running
	 *                       on
	 * @param value          next state in the transition choice category used by
	 *                       the object workflow
	 * @param nextstep       next step for the workflow
	 */
	public ChangeObjectState(
			ComplexWorkflow parentworkflow,
			DataObjectDefinition object,
			ChoiceValue value,
			WorkflowStep nextstep) {
		super(parentworkflow);
		this.object = object;
		this.value = value;
		this.nextstep = nextstep;
		if (nextstep.getParent() != this.getParent())
			throw new RuntimeException("Can only link change state task (wf:" + this.getParent().getInstancename()
					+ ") for nextstep  (wf: " + nextstep.getParent() + ") with a task of same workflow ");

	}

	/**
	 * @return get the parent object
	 */
	public DataObjectDefinition getObject() {
		return object;
	}

	/**
	 * @return get the choice value representing the next state
	 */
	public ChoiceValue getValue() {
		return value;
	}

	/**
	 * @return get next workflow step
	 */
	public WorkflowStep getNextstep() {
		return nextstep;
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws IOException {
		String taskvariable = this.gettaskid().toLowerCase();
		String choiceclass = StringFormatter.formatForJavaClass(value.getName());
		sg.wl("		// ----------------- change state " + choiceclass + " ----------------------");
		sg.wl("		ChangeStateWorkflowStep<" + parentclass + "," + lifecycleclass + "ChoiceDefinition> "
				+ taskvariable);
		sg.wl("			= new ChangeStateWorkflowStep<" + parentclass + "," + lifecycleclass + "ChoiceDefinition>("
				+ lifecycleclass + "ChoiceDefinition.getChoice" + choiceclass + "());");
		sg.wl("");

	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module) throws IOException {
		sg.wl("		" + this.gettaskid().toLowerCase() + ".setNextStep(" + nextstep.gettaskid().toLowerCase() + ");");

	}

}
