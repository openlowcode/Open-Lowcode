/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsSavetaskcommentAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.WorkflowInterface;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * an action to save a comment to the task and keep the task open
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SavetaskcommentAction
		extends
		AbsSavetaskcommentAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public SavetaskcommentAction(SModule parent) {
		super(parent);
	}

	@Override
	public DataObjectId<Task> executeActionLogic(
			DataObjectId<Task> taskid,
			String comment,
			Function<TableAlias, QueryFilter> datafilter) {
		Task task = Task.readone(taskid);
		DataObject<?> subject = task.getlinkedobjectidfortaskobject().lookupObject();
		if (!(subject instanceof WorkflowInterface))
			throw new RuntimeException("Internal Error: Object not of correct type for workflow : " + subject.getName()
					+ " for task " + task.getId().getId());
		WorkflowInterface subjectcasted = (WorkflowInterface) subject;
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		subjectcasted.savetaskcomment(taskid, userid, comment);
		return taskid;
	}

	@Override
	public SPage choosePage(DataObjectId<Task> taskidthru) {
		return ShowactivetaskAction.get().executeAndShowPage(taskidthru);
	}

}
