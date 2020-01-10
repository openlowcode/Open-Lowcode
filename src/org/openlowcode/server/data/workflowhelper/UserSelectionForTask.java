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

import java.util.ArrayList;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * User election for task
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition of the lifecycle of the object
 */
public abstract class UserSelectionForTask<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> {

	/**
	 * generates the list of potential users for the workflow task
	 * 
	 * @param workflowid object id of workflow
	 * @param object     parent data object
	 * @return list of users for the task
	 */
	public abstract ArrayList<DataObjectId<Appuser>> select(DataObjectId<Workflow> workflowid, E object);

}
