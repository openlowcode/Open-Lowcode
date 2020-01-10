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
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * a step in a complex workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition of the lifecycle of the object
 */
public abstract class ComplexWorkflowStep<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> {
	/**
	 * creates a complex workflow step
	 */
	public ComplexWorkflowStep() {

	}

	/**
	 * executes the workflow step
	 * 
	 * @param workflowid id of the workflow
	 * @param object     object that is the subject of the workflow
	 * @param helper     the workflow helper task
	 */
	public abstract void execute(DataObjectId<Workflow> workflowid, E object, ComplexWorkflowHelper<E, F> helper);

}
