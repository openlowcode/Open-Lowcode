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

import java.util.HashMap;

import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * A switch mapping a workflow step to specific information being extracted from
 * the data object the workflow is running on
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the worfklow is about
 * @param <F> transition choice used for the lifecycle of the object
 * @param <G> the type of the object being used for criteria for the switch
 */
public class ObjectElementSwitchComplexWorkflowStep<
		E extends DataObject<E> & LifecycleInterface<E, F>,
		F extends TransitionFieldChoiceDefinition<F>,
		G extends Object>
		extends
		SwitchComplexWorkflowStep<E, F> {
	private ComplexWorkflowStep<E, F> defaultstep;
	private DataObjectExtractor<E, G> elementextractor;
	private HashMap<G, ComplexWorkflowStep<E, F>> specificsteps;

	/**
	 * creates a complex switch
	 * 
	 * @param elementextractor function to extract the criteria object from the
	 *                         parent data object
	 */
	public ObjectElementSwitchComplexWorkflowStep(DataObjectExtractor<E, G> elementextractor) {
		this.elementextractor = elementextractor;
		specificsteps = new HashMap<G, ComplexWorkflowStep<E, F>>();
	}

	/**
	 * sets the default next step 
	 * 
	 * @param defaultstep default next step
	 */
	public void setDefaultNextStep(ComplexWorkflowStep<E, F> defaultstep) {
		this.defaultstep = defaultstep;
	}

	/**
	 * adds a specific mapping for a criteria
	 * 
	 * @param element value of the criteria object
	 * @param specificnextstep next step to trigger
	 */
	public void addSpecificStep(G element, ComplexWorkflowStep<E, F> specificnextstep) {
		specificsteps.put(element, specificnextstep);
	}

	@Override
	public ComplexWorkflowStep<E, F> getstepselection(DataObjectId<Workflow> workflowid, E object) {
		ComplexWorkflowStep<E, F> specificstep = specificsteps.get(elementextractor.extract(object));
		if (specificstep != null)
			return specificstep;
		return defaultstep;
	}

}
