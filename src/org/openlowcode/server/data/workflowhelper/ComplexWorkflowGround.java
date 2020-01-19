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
 * A ground workflow element that will basically do nothing. It can be put at
 * the end of the processing branch, when other branches may be potentially
 * active
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the worfklow is about
 * @param <F> transition choice used for the lifecycle of the object
 */
public class ComplexWorkflowGround<
		E extends DataObject<E> & LifecycleInterface<E, F>,
		F extends TransitionFieldChoiceDefinition<F>>
		extends
		ComplexWorkflowStep<E, F> {

	@Override
	public void execute(DataObjectId<Workflow> workflowid, E object, ComplexWorkflowHelper<E, F> workflowhelper) {
		// do nothing
	}

}
