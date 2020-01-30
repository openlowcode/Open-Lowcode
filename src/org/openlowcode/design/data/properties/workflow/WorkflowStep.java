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
 * A workflow step is an action performed by a workflow. A workflow is made of a
 * success of steps, with potentially several branches, starting at a root step
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class WorkflowStep {
	private ComplexWorkflow parent;

	/**
	 * Creates a workflow step for the given complex workflow
	 * 
	 * @param parent parent complex workflow
	 */
	public WorkflowStep(ComplexWorkflow parent) {
		this.parent = parent;
		registerToParentWorkflow();
	}

	/**
	 * @return get the parent workflow of the workflow step
	 */
	public ComplexWorkflow getParent() {
		return parent;
	}

	/**
	 * register the step in the parent workflow
	 */
	public void registerToParentWorkflow() {
		parent.registerNonTaskWorkflowStep(this);
	}

	/**
	 * writes the import for this workflow step
	 * 
	 * @param sg     sourcce generator
	 * @param module parent module
	 * @throws IOException if anything bad happens in the workflow
	 */
	public abstract void writeimport(SourceGenerator sg, Module module) throws IOException;

	/**
	 * @param sg
	 * @param module
	 * @param parentclass
	 * @param lifecycleclass
	 * @throws IOException
	 */
	public abstract void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws IOException;

	/**
	 * @return the task id (based on the provided sequence)
	 */
	public String gettaskid() {
		return "TASK" + sequence;
	}

	private int sequence;

	/**
	 * sets the unique sequence of the workflow step (should be unique for the
	 * complex workflow)
	 * 
	 * @param sequence a unique sequence for the workflow
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * a recursive method writing the source code for the next steps of the workflow
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens in the workflow
	 */
	public abstract void writeNextSteps(SourceGenerator sg, Module module) throws IOException;

}
