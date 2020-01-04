/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;

/**
 * A common interface to all types of workflows.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface WorkflowInterface {

	/**
	 * accepts a task meaning the user keeps the task for him/herself to treat it
	 * 
	 * @param taskid id of the task
	 * @param userid id of the user
	 */
	public void accepttask(DataObjectId<Task> taskid, DataObjectId<Appuser> userid);

	/**
	 * checks if the task can be accepted by the user (i.e. if the user is a
	 * potential assignee
	 * 
	 * @param taskid id of the task
	 * @param userid id of the user
	 * @return true if the task can be accepted
	 */
	public boolean canaccepttask(DataObjectId<Task> taskid, DataObjectId<Appuser> userid);

	/**
	 * by rejecting a task, the user will signify he/she will to treat the task, and
	 * one of the other potential assignees should treat the task
	 * 
	 * @param taskid id of the task
	 * @param userid id of the user
	 */
	public void rejecttask(DataObjectId<Task> taskid, DataObjectId<Appuser> userid);

	/**
	 * checks if the user can reject the task (i.e. is a potential assignee)
	 * 
	 * @param taskid id of the task
	 * @param userid id of the user
	 * @return true if the user can reject the task
	 */
	public boolean canrejecttask(DataObjectId<Task> taskid, DataObjectId<Appuser> userid);

	/**
	 * processes the task and choose one of the potential outcomes for the task
	 * (e.g. for an application, an outcome can be 'Accepted' or 'Rejected')
	 * 
	 * @param taskid      id of the task
	 * @param userid      id of the user
	 * @param choiceid    choice for the outcome of the task
	 * @param taskcomment comment saved in the workflow history
	 */
	public void processtask(DataObjectId<Task> taskid, DataObjectId<Appuser> userid, DataObjectId<Taskchoice> choiceid,
			String taskcomment);

	/**
	 * saves a task comment for the current task
	 * 
	 * @param taskid      id of the task
	 * @param userid      id of the user
	 * @param taskcomment comment of the task
	 */
	public void savetaskcomment(DataObjectId<Task> taskid, DataObjectId<Appuser> userid, String taskcomment);

	/**
	 * reassigns a task to a new user
	 * 
	 * @param taskid id of the task
	 * @param user id of the user
	 * @param newuser new id of the user
	 * @param taskcomment comment of the task
	 */
	public void reassigntask(DataObjectId<Task> taskid, DataObjectId<Appuser> user, DataObjectId<Appuser> newuser,
			String taskcomment);

}
