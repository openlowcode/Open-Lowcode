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
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * a workflow step that will change the state of the subject object of the
 * workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition choice of the lifecycle of the object
 */
public class ChangeStateWorkflowStep<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends ComplexWorkflowStep<E, F> {

	private ComplexWorkflowStep<E, F> nextstep;
	private ChoiceValue<F> nextstate;

	/**
	 * creates the change state workflow
	 * 
	 * @param nextstate the state to change the object to
	 */
	public ChangeStateWorkflowStep(ChoiceValue<F> nextstate) {
		this.nextstate = nextstate;
	}

	/**
	 * defines the next step in the workflow after the change state
	 * 
	 * @param nextstep next step to execute after
	 */
	public void setNextStep(ComplexWorkflowStep<E, F> nextstep) {
		this.nextstep = nextstep;
	}

	@Override
	public void execute(DataObjectId<Workflow> workflowid, E object, ComplexWorkflowHelper<E, F> workflowhelper) {
		object.changestate(nextstate);
		nextstep.execute(workflowid, object, workflowhelper);

	}

}
