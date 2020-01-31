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

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.properties.basic.Lifecycle;

/**
 * Definition of the trigger condition for the workflow to start
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class WorkflowTriggerCondition {
	private DataObjectDefinition object;
	private ChoiceValue workflowstartlifecyclestate;

	/**
	 * creates a workflow trigger condition on the defined state
	 * 
	 * @param object                      data object definition
	 * @param workflowstartlifecyclestate the lifecycle state the workflowshould
	 *                                    start on
	 */
	public WorkflowTriggerCondition(DataObjectDefinition object, ChoiceValue workflowstartlifecyclestate) {
		this.object = object;
		Property<?> lifecycle = object.getPropertyByName("LIFECYCLE");
		if (lifecycle == null)
			throw new RuntimeException(
					"For inclusion in workflow, the object " + object.getName() + " should have lifecycle.");
		Lifecycle lifecyclecasted = (Lifecycle) lifecycle;
		if (!lifecyclecasted.getTransitionChoiceCategory().hasChoiceValue(workflowstartlifecyclestate))
			throw new RuntimeException("Choice Value " + workflowstartlifecyclestate.getName()
					+ " does not exist in lifecycle of object " + object.getName() + ".");
		this.workflowstartlifecyclestate = workflowstartlifecyclestate;
	}

	/**
	 * @return the parent object for the workflow
	 */
	public DataObjectDefinition getObject() {
		return object;
	}

	/**
	 * @return the lifecycle state the workflow should start on
	 */
	public ChoiceValue getNewlifecyclestate() {
		return workflowstartlifecyclestate;
	}

}
