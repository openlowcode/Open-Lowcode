/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.module.system.data.choice.WorkflowlifecycleChoiceDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * the workflow step finishing the workflow. When reached, workflow is finished
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the worfklow is about
 * @param <F> transition choice used for the lifecycle of the object
 */
public class ComplexWorkflowClose<
		E extends DataObject<E> & LifecycleInterface<E, F>,
		F extends TransitionFieldChoiceDefinition<F>>
		extends
		ComplexWorkflowStep<E, F> {
	/**
	 * creates a complex workflow close widget
	 */
	public ComplexWorkflowClose() {

	}

	@Override
	public void execute(DataObjectId<Workflow> workflowid, E object, ComplexWorkflowHelper<E, F> workflowhelper) {
		Workflow workflow = Workflow.readone(workflowid);
		workflow.changestate(WorkflowlifecycleChoiceDefinition.getChoiceFinished());

	}

}
