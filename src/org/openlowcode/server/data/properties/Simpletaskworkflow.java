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
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.TasklifecycleChoiceDefinition;
import org.openlowcode.module.system.data.choice.WorkflowlifecycleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.workflowhelper.WorkflowCommons;
import org.openlowcode.tools.richtext.RichText;

/**
 * A simple workflow around the lifecycle of the objects. It allows to dispatch
 * a unique task to a preset group of users. Users can then start working on a
 * task, and when finished, choose any of the final states of the lifecycle. If
 * you need a workflow with several tasks, you should use complex workflows.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> the transition choice for the lifecycle
 */
public class Simpletaskworkflow<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectProperty<E> {
	@SuppressWarnings("unused")
	private Lifecycle<E, F> lifecycle;
	private F lifecyclehelper;
	private SimpletaskworkflowDefinition<E, F> definition;
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static Logger logger = Logger.getLogger(Simpletaskworkflow.class.getName());

	/**
	 * creates the property simple task workflow for a data object
	 * 
	 * @param definition      definition of the workflow object
	 * @param parentpayload   parent payload of the data object
	 * @param lifecyclehelper lifecycle transition choice
	 */
	public Simpletaskworkflow(SimpletaskworkflowDefinition<E, F> definition, DataObjectPayload parentpayload,
			F lifecyclehelper) {
		super(definition, parentpayload);
		this.lifecyclehelper = lifecyclehelper;
		this.definition = definition;
	}

	/**
	 * sets the dependent property lifecycle
	 * 
	 * @param lifecycle dependent property lifecycle
	 */
	public void setDependentPropertyLifecycle(Lifecycle<E, F> lifecycle) {
		this.lifecycle = lifecycle;
	}

	/**
	 * performs post processing after the insertion of the object (creates a
	 * workflow task)
	 * 
	 * @param object data object
	 */
	public void postprocStoredobjectInsert(E object) {
		createTaskForObject(object, null, null, false, null);
	}

	/**
	 * performs post processing after massive insertion of the object (not optimized
	 * for performance)
	 * 
	 * @param objectbatch        a batch of data objects
	 * @param simpletaskworkflow a batch of corresponding simple task workflow
	 *                           properties
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> void postprocStoredobjectInsert(
			E[] objectbatch, Simpletaskworkflow<E, F>[] simpletaskworkflow) {
		logger.warning("Using massive preproc procedure that is not optimized for mass insert");
		for (int i = 0; i < simpletaskworkflow.length; i++) {
			simpletaskworkflow[i].createTaskForObject(objectbatch[i], null, null, false, null);
		}
	}

	private void createTaskForObject(E object, DataObjectId<Appuser> uniqueuser,
			DataObjectId<Workflow> existingworkflowid, boolean donotsendmail, String reassigncomment) {
		// creates workflow task sent to the group of the authority (limitation today:
		// does NOT manage several groups: will take the first one)
		Date targetdate = null;
		if (definition.getWorkflowHelper().getDefaultDelay() > 0) {
			targetdate = new Date(
					System.currentTimeMillis() + 86400 * 1000 * definition.getWorkflowHelper().getDefaultDelay());
		}
		if (object instanceof TargetdateInterface) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			TargetdateInterface<E, F> objectcasted = (TargetdateInterface) object;
			targetdate = objectcasted.getTargetdate();
		}

		DataObjectId<Workflow> workflowid = existingworkflowid;
		if (workflowid == null) {
			Workflow workflow = new Workflow();
			String workflowname = "Assignment for " + WorkflowCommons.generateObjectString(object);
			if (workflowname.length() > 64)
				workflowname = workflowname.substring(0, 60) + "...";
			workflow.setobjectname(workflowname);
			workflow.setlinkedobjectidforworkflowobject(
					DataObjectId.generateDataObjectId(object.getId().getId(), object.getId().getObjectId()));
			workflow.insert();
			workflowid = workflow.getId();
			if (targetdate != null)
				workflow.settargetdate(targetdate);

		}

		Task task = new Task();

		if (uniqueuser == null) {
			String name = "Accept assignment";
			if (name.length() > 64)
				name = name.substring(0, 60) + "...";
			task.setobjectname(name);

		}
		if (uniqueuser != null) {
			String name = "Complete and close";
			if (name.length() > 64)
				name = name.substring(0, 60) + "...";
			task.setobjectname(name);
			Appuser userobject = Appuser.readone(uniqueuser);
			String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
			if (userstring.length() > 95)
				userstring = userstring.substring(0, 92) + "...";
			task.setCompletedby(userstring);

		}

		String objectstring = WorkflowCommons.generateObjectString(object);
		if (objectstring.length() > 200)
			objectstring = objectstring.substring(0, 196) + "...";
		task.setSubject(objectstring);

		task.setDescription(definition.getWorkflowHelper().getTaskMessage());
		task.setparentwithoutupdateforworkflow(workflowid);
		task.setlinkedobjectidfortaskobject(
				DataObjectId.generateDataObjectId(object.getId().getId(), object.getId().getObjectId()));
		if (reassigncomment != null) {
			String reassigncommentcleaned = reassigncomment;
			if (reassigncommentcleaned.length() > 1995)
				reassigncommentcleaned = reassigncommentcleaned.substring(0, 1995) + "...";
			task.setComment(reassigncommentcleaned);
		}

		task.insert();

		if (targetdate != null) {
			task.settargetdate(targetdate);
		}

		String emailtitle = "Notification: Access and Complete Assignment for "
				+ definition.getParentObject().getLabel();
		if (uniqueuser != null)
			emailtitle = "Notification: Complete Assignment for " + definition.getParentObject().getLabel();
		String emailbody = "You have received a notification to close and complete the elements below. ";
		if (targetdate != null) {
			emailbody += " Target date for this action is " + dateformat.format(targetdate);
			long days = (new Date().getTime() - targetdate.getTime()) / (24 * 3600 * 1000);
			if (days > 1)
				emailbody += " (" + days + " days ago)";
			if (days == 1)
				emailbody += " (yesterday)";
			if (days == 0)
				emailbody += " (today)";
			if (days == -1)
				emailbody += "(tomorrow)";
			if (days < -1)
				emailbody += " (in " + (-days) + " days)";
			emailbody += ".";
		}
		if (uniqueuser != null) {
			emailbody += " As the only responsible for the task, it is your responsibility to complete it in time or to reassign it to a more appropriate person.";
		}
		if (reassigncomment != null) {
			RichText richtext = new RichText(reassigncomment);

			emailbody += "\n\n" + richtext.generatePlainString();
		}
		String objectid = "";
		if (object instanceof NumberedInterface) {
			@SuppressWarnings("unchecked")
			NumberedInterface<E> numberobject = (NumberedInterface<E>) object;
			objectid = numberobject.getNr();
		}
		String objectlabel = WorkflowCommons.generateObjectString(object);

		DataObjectId<Authority> authorityid = definition.getWorkflowHelper().getSingleAuthorityMapper()
				.getAuthority(object);
		DataObjectId<Appuser> workflowcreatorid = Workflow.readone(workflowid).getCreateuserid();
		if (uniqueuser == null) {
			WorkflowCommons.assignTaskToAuthority(task, authorityid, null,
					(donotsendmail == true ? null : definition.getWorkflowHelper().getEmailtype()),
					"SIMPLEWORKFLOW/" + definition.getParentObject().getName(), emailtitle, emailbody, objectid,
					objectlabel, workflowcreatorid, object);
		} else {
			WorkflowCommons.assignTaskToUser(task, uniqueuser,
					(donotsendmail == true ? null : definition.getWorkflowHelper().getEmailtype()), emailtitle,
					emailbody, objectid, objectlabel, workflowcreatorid,
					"SIMPLEWORKFLOW/" + definition.getParentObject().getName(), object);
			task.changestate(TasklifecycleChoiceDefinition.getChoiceInwork());
		}

		ChoiceValue<F> workingchoice = lifecyclehelper.parseValueFromStorageCode(object.getState());
		if (lifecyclehelper.getDefaultChoice().equals(workingchoice)) {
			workingchoice = lifecyclehelper.getDefaultWorkingChoice();
		}

		ChoiceValue<F>[] transitionstates = workingchoice.getAuthorizedTransitions();

		for (int i = 0; i < transitionstates.length; i++) {
			ChoiceValue<F> thisstate = transitionstates[i];
			Taskchoice choice = new Taskchoice();
			choice.setparentfortask(task.getId());
			choice.setobjectname(thisstate.getDisplayValue());
			choice.setCode(thisstate.getStorageCode());
			choice.setSelected(BooleanChoiceDefinition.get().UNKN);
			choice.insert();

		}
	}

	/**
	 * post processing after change state. Currently does nothing
	 * 
	 * @param object   data object
	 * @param newstate new state for change state
	 */
	public void postprocLifecycleChangestate(E object, ChoiceValue<F> newstate) {
		// do nothing for now, maybe in the future, reset the workflow if state is back
		// to Open

	}

	/**
	 * massive post-proccessing after changee state. Currently does nothing
	 * 
	 * @param objectbatch              object batch
	 * @param newstate                 new state
	 * @param simpletaskfworkflowbatch simple task workflow batch corresponding to
	 *                                 the object batch
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> void postprocLifecycleChangestate(
			E[] objectbatch, ChoiceValue<F>[] newstate, Simpletaskworkflow<E, F>[] simpletaskfworkflowbatch) {
		// ------------ object control

		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (simpletaskfworkflowbatch == null)
			throw new RuntimeException("lifecycle batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");
		if (objectbatch.length != simpletaskfworkflowbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with simpletaskworkflow batch length " + simpletaskfworkflowbatch.length);
		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new state batch length " + newstate.length);

		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				simpletaskfworkflowbatch[i].postprocLifecycleChangestate(objectbatch[i], newstate[i]);
			}
		}
	}

	/**
	 * returns true if the user can accept the task (task in status Open)
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
	 * returns true if the user can reject the task (possible in any state that
	 * allows going back to default choice)
	 * 
	 * @param object data object
	 * @param taskid id of the task
	 * @param userid id of the user
	 * @return true if the user can accept the task
	 */
	public boolean canrejecttask(E object, DataObjectId<Task> taskid, DataObjectId<Appuser> userid) {
		ChoiceValue<F> currentstate = lifecyclehelper.parseValueFromStorageCode(object.getState());
		ChoiceValue<F>[] transitionstates = currentstate.getAuthorizedTransitions();
		for (int i = 0; i < transitionstates.length; i++) {
			ChoiceValue<F> thisstate = transitionstates[i];
			if (lifecyclehelper.getDefaultChoice().getStorageCode().compareTo(thisstate.getStorageCode()) == 0)
				return true;
		}
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
		Appuser userobject = Appuser.readone(userid);
		String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
		if (userstring.length() > 95)
			userstring = userstring.substring(0, 92) + "...";
		task.setCompletedby(userstring);
		task.update();
		// remove all tasks except the one that is accepted
		Taskuser[] taskuser = Taskuser.getalllinksfromleftid(taskid, null);
		for (int i = 0; i < taskuser.length; i++) {
			Taskuser thistaskuser = taskuser[i];
			if (!(thistaskuser.getRgid().equals(userid)))
				thistaskuser.delete();
		}
		// changing state of task to in-work
		task.changestate(TasklifecycleChoiceDefinition.getChoiceInwork());
		// changing state of object to first working state.
		object.changestate(lifecyclehelper.getDefaultWorkingChoice());
		// object.setState(state);

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
		task.setobjectname("Accept task for " + WorkflowCommons.generateObjectString(object));
		task.setCompletedby("");
		task.update();
		// step 2 : suppress current task assignee
		Taskuser taskuser[] = Taskuser.getalllinksfromleftandrightid(taskid, userid, null);
		for (int i = 0; i < taskuser.length; i++)
			taskuser[i].delete();
		// step 3 : reassign to other
		object.changestate(lifecyclehelper.getDefaultChoice());
		DataObjectId<Authority> authorityid = definition.getWorkflowHelper().getSingleAuthorityMapper()
				.getAuthority(object);

		WorkflowCommons.assignTaskToAuthority(task, authorityid, userid, null, null, null, null, null, null, null,
				object);
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
		task.setSelectedchoice(choicelabel);
		task.setComment(comment);
		Appuser userobject = Appuser.readone(userid);
		String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
		if (userstring.length() > 95)
			userstring = userstring.substring(0, 92) + "...";
		task.setCompletedby(userstring);
		task.setCompleteddate(new Date());
		ChoiceValue<F> lifecyclevalue = lifecyclehelper.parseValueFromStorageCode(choicecode);
		logger.fine("start process task choicecode " + choice.getCode() + ", lifecyclevalue = "
				+ lifecyclevalue.getStorageCode());

		if (object.getState().equals(lifecyclehelper.getDefaultChoice().getStorageCode())) {
			logger.severe("prepare to change task to " + lifecyclehelper.getDefaultWorkingChoice().getStorageCode());
			object.changestate(lifecyclehelper.getDefaultWorkingChoice());
		}
		logger.fine("prepare to change object status to " + lifecyclevalue);
		object.changestate(lifecyclevalue);

		if (!lifecyclehelper.isChoiceFinal(lifecyclevalue)) {
			// create new workflow task if state is not final
			createTaskForObject(object, userid, task.getLinkedtoparentforworkflowid(), false, comment);
		} else {
			// finished workflow
			Workflow workflow = Workflow.readone(task.getLinkedtoparentforworkflowid());
			workflow.changestate(WorkflowlifecycleChoiceDefinition.getChoiceFinished());
		}
		// close task in the end, when everything is done correctly.
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
		Appuser userobject = Appuser.readone(user);
		String userstring = userobject.getObjectname() + " (" + userobject.getNr() + ")";
		if (userstring.length() > 95)
			userstring = userstring.substring(0, 92) + "...";
		Task task = Task.readone(taskid);
		// First create reassignable task
		String taskcommentfinal = "[]Reassign from " + userobject.getObjectname() + " (" + userobject.getNr() + ") at "
				+ dateformat.format(new Date()) + ", comments below:\n" + taskcomment;
		createTaskForObject(object, newuser, task.getLinkedtoparentforworkflowid(), false, taskcommentfinal);
		// if everything goes fine, close current task as reassigned.
		task.setComment(taskcomment);
		task.setCompletedby(userstring);
		task.setCompleteddate(new Date());
		task.changestate(TasklifecycleChoiceDefinition.getChoiceReassigned());

	}
}
