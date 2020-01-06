/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.TasklifecycleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.workflowhelper.ComplexWorkflowHelper;

/**
 * A complex workflow allows several tasks with routing options
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition used by the lifecycle
 */
public class Complexworkflow<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectProperty<E> {

	private ComplexworkflowDefinition<E, F> definition;
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/**
	 * creates a complex workflow property for this data object
	 * 
	 * @param definition    definition of the complex workflow property
	 * @param parentpayload payload of the parent data object
	 */
	public Complexworkflow(ComplexworkflowDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.definition = definition;
	}

	private static final Logger logger = Logger.getLogger(Complexworkflow.class.getName());

	@SuppressWarnings("rawtypes")
	private String generateObjectString(E object) {
		String objectid = object.getDefinitionFromObject().getLabel() + " ";
		if (object instanceof NumberedInterface)
			objectid += ((NumberedInterface) object).getNr() + " ";
		if (object instanceof NamedInterface)
			objectid += ((NamedInterface) object).getObjectname() + " ";
		return objectid;
	}

	private Workflow createWorkflow(E object) {
		Workflow workflow = new Workflow();
		String workflowname = "Complex Workflow";
		if (workflowname.length() > 64)
			workflowname = workflowname.substring(0, 60) + "...";
		workflow.setobjectname(workflowname);
		workflow.setlinkedobjectidforworkflowobject(
				DataObjectId.generateDataObjectId(object.getId().getId(), object.getId().getObjectId()));
		workflow.insert();
		return workflow;
	}

	/**
	 * post processing after object has been created. Starts the workflow if the
	 * workflow is defined to start at object creation
	 * 
	 * @param object data object
	 */
	public void postprocStoredobjectInsert(E object) {
		boolean startworkflowatinsert = definition.getWorkflowHelper().isStartAtInsert();
		if (startworkflowatinsert) {
			Workflow workflow = createWorkflow(object);
			definition.getWorkflowHelper().executenext(object, workflow.getId(), ComplexWorkflowHelper.START_CODE,
					null);
		}

	}

	/**
	 * massive post processing after object has been created. Not optimized for
	 * batch treatment
	 * 
	 * @param objectbatch          batch of objects
	 * @param complexworkflowbatch corresponding batch of complex workflow
	 *                             properties
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> void postprocStoredobjectInsert(
			E[] objectbatch, Complexworkflow<E, F>[] complexworkflowbatch) {
		logger.warning("This function is not optimized for batch treatment");
		for (int i = 0; i < complexworkflowbatch.length; i++)
			complexworkflowbatch[i].postprocStoredobjectInsert(objectbatch[i]);
	}

	/**
	 * post processing to the change state method. Starts the workflow if defined to
	 * start at transition to the new state (for example, workflow starts when
	 * object is put in 'Under Review'
	 * 
	 * @param object   data object
	 * @param newstate new state
	 */
	public void postprocLifecycleChangestate(E object, ChoiceValue<F> newstate) {
		boolean startworkflowatchangestate = definition.getWorkflowHelper().isStartAtChangeState(newstate);
		if (startworkflowatchangestate) {
			Workflow workflow = createWorkflow(object);
			definition.getWorkflowHelper().executenext(object, workflow.getId(), ComplexWorkflowHelper.START_CODE,
					null);
		}

	}

	/**
	 * massive post processing to the change state method
	 * 
	 * @param objectbatch          batch of data objects
	 * @param newstate             new state
	 * @param complexworkflowbatch corresponding batch of complex workflow
	 *                             properties
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> void postprocLifecycleChangestate(
			E[] objectbatch, ChoiceValue<F>[] newstate, Complexworkflow<E, F>[] complexworkflowbatch) {
		// ------------ object control
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (complexworkflowbatch == null)
			throw new RuntimeException("lifecycle batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");
		if (objectbatch.length != complexworkflowbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with complexworkflow batch length " + complexworkflowbatch.length);
		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new state batch length " + newstate.length);

		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				complexworkflowbatch[i].postprocLifecycleChangestate(objectbatch[i], newstate[i]);
			}
		}
	}

	/**
	 * returns true if the user can accept the task
	 * 
	 * @param object data object
	 * @param taskid id of the task
	 * @param userid id of the user
	 * @return true if the user can accept the task
	 */
	public boolean canaccepttask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid) {
		Task task = Task.readone(taskid);
		if (task.getState().compareTo("OPEN") == 0)
			return true;
		return false;
	}

	/**
	 * returns true if the user can reject the task (if this is a group task)
	 * 
	 * @param object data object
	 * @param taskid id of the task
	 * @param userid id of the user
	 * @return true if the user can accept the task
	 */
	public boolean canrejecttask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid) {
		Task task = Task.readone(taskid);
		ChoiceValue<BooleanChoiceDefinition> isgroup = task.getGrouptask();
		if (isgroup.equals(BooleanChoiceDefinition.get().YES))
			return true;
		return false;

	}

	/**
	 * saves task comment
	 * 
	 * @param object  data object
	 * @param taskid  id of the task
	 * @param userid  id of the user
	 * @param comment comment for the task
	 */
	public void savetaskcomment(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid, String comment) {
		logger.info("start save task comment task " + taskid + " for userid = " + userid);

		Task task = Task.readone(taskid);
		// check if task must be accepted
		if (task.getState().compareTo("OPEN") == 0)
			accepttask(object, taskid, userid);
		// update comment
		task = Task.readone(taskid);
		task.setComment(comment);
		task.update();
		// persist task
	}

	/**
	 * accepts the task
	 * 
	 * @param object data object
	 * @param taskid id of the task
	 * @param userid id of the user accepting the task
	 */
	public void accepttask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid) {
		logger.info("start accept task " + taskid + " for userid = " + userid);

		Task task = Task.readone(taskid);
		task.setobjectname("Complete and close");

		// remove all tasks except the one that is accepted
		Taskuser[] taskuser = Taskuser.getalllinksfromleftid(taskid, null);
		for (int i = 0; i < taskuser.length; i++) {
			Taskuser thistaskuser = taskuser[i];
			if (!(thistaskuser.getRgid().equals(userid)))
				thistaskuser.delete();
		}
		// changing state of task to in-work
		task.changestate(TasklifecycleChoiceDefinition.getChoiceInwork());

	}

	/**
	 * rejects the task
	 * 
	 * @param object data object
	 * @param taskid id of the task
	 * @param userid id of the user
	 */
	public void rejecttask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid) {
		// step 1: get task
		logger.info("start reject task " + taskid + " for userid = " + userid);
		Task task = Task.readone(taskid);
		task.setobjectname("Accept task for " + generateObjectString(object));
		// step 2 : suppress current task assignee
		Taskuser taskuser[] = Taskuser.getalllinksfromleftandrightid(taskid, userid, null);
		for (int i = 0; i < taskuser.length; i++)
			taskuser[i].delete();
		// step 3 : reassign to other

		definition.getWorkflowHelper().regenerateTaskUserAfterReject(object, task.getLinkedtoparentforworkflowid(),
				task, userid);

		task.changestate(TasklifecycleChoiceDefinition.getChoiceOpen());

	}

	/**
	 * processes the task (completion by user)
	 * 
	 * @param object   data object
	 * @param taskid   id of the task
	 * @param userid   id of the user
	 * @param choiceid id of the choice of outcome selected by the user (e.g.
	 *                 request approved or request rejected)
	 * @param comment  task comment
	 */
	public void processtask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid,
			DataObjectId<Taskchoice> choiceid, String comment) {
		logger.info("start process task " + taskid + " for user " + userid + " for choice " + choiceid + " for comment "
				+ comment);
		Task task = Task.readone(taskid);
		Taskchoice choice = Taskchoice.readone(choiceid);
		String choicecode = choice.getCode();
		String choicelabel = choice.getObjectname();
		Appuser userobject = Appuser.readone(userid);
		String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
		if (userstring.length() > 95)
			userstring = userstring.substring(0, 92) + "...";
		task.setCompletedby(userstring);
		task.setCompleteddate(new Date());
		task.setSelectedchoice(choicelabel);
		task.setComment(comment);

		definition.getWorkflowHelper().executenext(object, task.getLinkedtoparentforworkflowid(), task.getCode(),
				choicecode);

		logger.fine("changing task status to " + TasklifecycleChoiceDefinition.getChoiceCompleted().getStorageCode());
		task.changestate(TasklifecycleChoiceDefinition.getChoiceCompleted());

	}

	/**
	 * reassigns a task from a user to another one
	 * 
	 * @param object      data object
	 * @param taskid      id of the task
	 * @param user        user who reassigned the task
	 * @param newuser     new user the task is assigned to
	 * @param taskcomment comment of the task
	 */
	public void reassigntask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> user,
			DataObjectId<Appuser> newuser, String taskcomment) {
		logger.info("start process task " + taskid + " for user " + user + " for new user" + user + " for comment "
				+ taskcomment);
		// First do stuff
		Task task = Task.readone(taskid);
		Appuser userobject = Appuser.readone(user);
		String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
		if (userstring.length() > 95)
			userstring = userstring.substring(0, 92) + "...";
		String taskcommentfinal = "[]Reassign from " + userobject.getObjectname() + " (" + userobject.getNr() + ") at "
				+ dateformat.format(new Date()) + ", comments below:\n" + taskcomment;
		definition.getWorkflowHelper().reassigntask(object, task.getLinkedtoparentforworkflowid(), newuser,
				task.getCode(), taskcommentfinal);
		// then if everything was fine, reassign
		task.setComment(taskcomment);
		task.setCompletedby(userstring);
		task.setCompleteddate(new Date());
		task.changestate(TasklifecycleChoiceDefinition.getChoiceReassigned());

	}

}
