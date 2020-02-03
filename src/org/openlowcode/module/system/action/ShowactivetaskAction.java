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

import org.openlowcode.module.system.action.generated.AbsShowactivetaskAction;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.ShowactivetaskPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * shows the screen for an active task to allow a user to validate it
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowactivetaskAction
		extends
		AbsShowactivetaskAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ShowactivetaskAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			DataObjectId<Task> taskid,
			Function<TableAlias, QueryFilter> datafilter) {
		Task task = Task.readone(taskid);
		DataObjectId<?> subjectid = task.getlinkedobjectidfortaskobject();
		Taskchoice[] choices = task.getallchildrenfortaskfortaskchoice(null);
		Taskuser[] taskusers = Taskuser.getalllinksfromleftid(taskid, null);
		ChoiceValue<BooleanChoiceDefinition> canaccept = BooleanChoiceDefinition.get().NO;
		ChoiceValue<BooleanChoiceDefinition> canreject = BooleanChoiceDefinition.get().NO;

		boolean canacceptflag = CanaccepttaskAction.get().executeActionLogic(taskid, null).getResult();
		boolean canrejectflag = CanrejecttaskAction.get().executeActionLogic(taskid, null).getResult();

		if (canacceptflag)
			canaccept = BooleanChoiceDefinition.get().YES;
		if (canrejectflag)
			canreject = BooleanChoiceDefinition.get().YES;

		return new ActionOutputData(task, subjectid, choices, taskusers, canaccept, canreject, task.getComment());
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ShowactivetaskPage(logicoutput.getTask(), logicoutput.getObjectid(), logicoutput.getTaskchoice(),
				logicoutput.getTaskusers(), logicoutput.getCanaccept(), logicoutput.getCanreject(),
				logicoutput.getComment());
	}

}
