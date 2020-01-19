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
 * A user selection for task that is using specific logic to get the good user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the worfklow is about
 * @param <F> transition choice used for the lifecycle of the object
 */
public class ObjectParentUserSelectionForTask<
		E extends DataObject<E> & LifecycleInterface<E, F>,
		F extends TransitionFieldChoiceDefinition<F>>
		extends
		UserSelectionForTask<E, F> {
	private DataObjectExtractor<E, DataObjectId<Appuser>> userextractor;

	/**
	 * Creates a user selection for task based on object
	 * 
	 * @param userextractor extractor getting the user if from the object that the
	 *                      workflow is running on
	 */
	public ObjectParentUserSelectionForTask(DataObjectExtractor<E, DataObjectId<Appuser>> userextractor) {
		this.userextractor = userextractor;
	}

	@Override
	public ArrayList<DataObjectId<Appuser>> select(DataObjectId<Workflow> workflowid, E object) {
		DataObjectId<Appuser> user = userextractor.extract(object);
		ArrayList<DataObjectId<Appuser>> appuserlist = new ArrayList<DataObjectId<Appuser>>();
		appuserlist.add(user);
		return appuserlist;
	}

}
