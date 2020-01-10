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
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;
import org.openlowcode.server.data.properties.NamedInterface;
import org.openlowcode.server.data.properties.NumberedInterface;

/**
 * A workflow task step
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition of the lifecycle of the object
 */
public class WorkflowTaskStep<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends ComplexWorkflowStep<E, F> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WorkflowTaskStep.class.getName());
	private UserSelectionForTask<E, F> userselection;
	private TaskChoiceTemplate[] choices;
	private String taskcode;
	private String taskdescription;
	private String tasktitle;
	private int delay;
	private boolean delayindays;

	public static final String EMAIL_NONE = null;
	public static final String EMAIL_NOW = "NOW";
	public static final String EMAIL_DELAY15MIN = "D15M";
	public static final String EMAIL_DELAY2H = "D2H";
	public static final String EMAIL_DAILY = "DAY";
	public static final String EMAIL_WEEKLY = "WEEKLY";
	public static final String EMAIL_WFSETTING = "WFSETTING";
	private String emailtype;

	/**
	 * @return get e-mail type
	 */
	public String getEmailtype() {
		return this.emailtype;
	}

	/**
	 * creates a workflow task step with default e-mail parameter
	 * 
	 * @param userselection   user selection
	 * @param choices         possible choices
	 * @param taskcode        task code ()
	 * @param taskdescription description of the task
	 * @param tasktitle       title of the task
	 * @param delay           delay for the task (in minutes or days)
	 * @param delayindays     true if delay is in day, false if delay is in minute
	 */
	public WorkflowTaskStep(UserSelectionForTask<E, F> userselection, TaskChoiceTemplate[] choices, String taskcode,
			String taskdescription, String tasktitle, int delay, boolean delayindays) {
		this(userselection, choices, taskcode, taskdescription, tasktitle, delay, delayindays,
				WorkflowTaskStep.EMAIL_WFSETTING);
	}

	/**
	 * creates a workflow task step with specific e-mail delay parameter
	 * 
	 * @param userselection   user selection
	 * @param choices         possible choices
	 * @param taskcode        task code ()
	 * @param taskdescription description of the task
	 * @param tasktitle       title of the task
	 * @param delay           delay for the task (in minutes or days)
	 * @param delayindays     true if delay is in day, false if delay is in minute
	 * @param emailtype       type of delay for e-mail regrouping and sending
	 */
	public WorkflowTaskStep(UserSelectionForTask<E, F> userselection, TaskChoiceTemplate[] choices, String taskcode,
			String taskdescription, String tasktitle, int delay, boolean delayindays, String emailtype) {
		super();
		this.userselection = userselection;
		this.choices = choices;
		this.taskcode = taskcode;
		this.taskdescription = taskdescription;
		this.tasktitle = tasktitle;
		this.delay = delay;
		this.delayindays = delayindays;

		boolean emailtypevalue = false;
		if (emailtype == null)
			emailtypevalue = true;
		if (emailtype != null) {
			if (emailtype.equals(EMAIL_NOW))
				emailtypevalue = true;
			if (emailtype.equals(EMAIL_DELAY15MIN))
				emailtypevalue = true;
			if (emailtype.equals(EMAIL_DELAY2H))
				emailtypevalue = true;
			if (emailtype.equals(EMAIL_DAILY))
				emailtypevalue = true;
			if (emailtype.equals(EMAIL_WEEKLY))
				emailtypevalue = true;
			if (emailtype.equals(EMAIL_WFSETTING))
				emailtypevalue = true;

		}
		if (!emailtypevalue)
			throw new RuntimeException(
					"email type is not valid, please refer to the class static string, given type =  '" + emailtype
							+ "'");

		this.emailtype = emailtype;
	}

	/**
	 * generates the object identification string
	 * 
	 * @param object data object
	 * @return a short string to describe the object
	 */
	@SuppressWarnings("rawtypes")
	private String generateObjectString(E object) {
		String objectid = object.getDefinitionFromObject().getLabel() + " ";
		if (object instanceof NumberedInterface)
			objectid += ((NumberedInterface) object).getNr() + " ";
		if (object instanceof NamedInterface)
			objectid += ((NamedInterface) object).getObjectname() + " ";
		return objectid;
	}

	/**
	 * adds user to the task
	 * 
	 * @param workflowid      id of the workflows
	 * @param object          data object that is subject to the workflow
	 * @param task            task object
	 * @param useridtoexclude user to exclude (typically, the user who just rejected
	 *                        the task)
	 * @param workflowhelper  workflow helper
	 */
	private void addUsersToTask(DataObjectId<Workflow> workflowid, E object, Task task,
			DataObjectId<Appuser> useridtoexclude, ComplexWorkflowHelper<E, F> workflowhelper) {
		ArrayList<DataObjectId<Appuser>> usersfortask = userselection.select(workflowid, object);
		boolean treated = false;

		ArrayList<DataObjectId<Appuser>> recipients = new ArrayList<DataObjectId<Appuser>>();
		if (usersfortask != null)
			if (usersfortask.size() == 1) {
				Taskuser taskuser = new Taskuser();
				taskuser.setleftobject(task.getId());
				taskuser.setrightobject(usersfortask.get(0));
				recipients.add(usersfortask.get(0));
				taskuser.insert();
				treated = true;
				task.setGrouptask(BooleanChoiceDefinition.get().NO);
				task.update();
				String specificemailtype = this.emailtype;
				if (EMAIL_WFSETTING.equals(this.emailtype))
					specificemailtype = workflowhelper.getDefaultTaskEmailType();

				if (specificemailtype != null) {
					Workflow workflow = Workflow.readone(workflowid);
					WorkflowCommons.sendNotification("You received a task " + tasktitle, taskdescription,
							WorkflowCommons.generateObjectId(object), WorkflowCommons.generateObjectString(object),
							workflow.getCreateuserid(), recipients, specificemailtype,
							"CPLX:" + object.getName() + ":" + task.getCode(),
							object.getDefinitionFromObject().getModuleName(), true);
				}
				return;
			}
		if (usersfortask != null)
			if (usersfortask.size() > 1) {
				for (int i = 0; i < usersfortask.size(); i++) {
					DataObjectId<Appuser> thisappuser = usersfortask.get(i);
					boolean process = false;
					if (useridtoexclude == null)
						process = true;
					if (useridtoexclude != null)
						if (!(thisappuser.getId().equals(useridtoexclude.getId())))
							process = true;

					if (process) {
						Taskuser taskuser = new Taskuser();
						taskuser.setleftobject(task.getId());
						taskuser.setrightobject(thisappuser);
						taskuser.insert();
						recipients.add(thisappuser);
					}
				}
				task.setGrouptask(BooleanChoiceDefinition.get().YES);
				task.update();
				treated = true;
			}
		if (!treated) {
			Appuser admin = Appuser.getobjectbynumber("admin")[0];
			Taskuser taskuser = new Taskuser();
			taskuser.setleftobject(task.getId());
			taskuser.setrightobject(admin.getId());
			taskuser.insert();
			task.setGrouptask(BooleanChoiceDefinition.get().NO);
			task.update();
			recipients.add(admin.getId());
		}
		String specificemailtype = this.emailtype;
		if (EMAIL_WFSETTING.equals(this.emailtype))
			specificemailtype = workflowhelper.getDefaultTaskEmailType();

		if (specificemailtype != null) {
			Workflow workflow = Workflow.readone(workflowid);
			WorkflowCommons.sendNotification("You received a task " + tasktitle, taskdescription,
					WorkflowCommons.generateObjectId(object), WorkflowCommons.generateObjectString(object),
					workflow.getCreateuserid(), recipients, specificemailtype,
					"CPLX:" + object.getName() + ":" + task.getCode(), object.getDefinitionFromObject().getModuleName(),
					true);
		}
	}

	/**
	 * generates a new task after a reassignment
	 * 
	 * @param workflowid      id of the workflows
	 * @param object          data object that is subject to the workflow
	 * @param forceuser       the user to reassign the task to
	 * @param workflowhelper  workflow helper (holding the workflow logic)
	 * @param reassigncomment comment after the reassignment
	 */
	private void generateNewTask(DataObjectId<Workflow> workflowid, E object, DataObjectId<Appuser> forceuser,
			ComplexWorkflowHelper<E, F> workflowhelper, String reassigncomment) {
		Task task = new Task();
		String objectstring = generateObjectString(object);
		if (objectstring.length() > 200)
			objectstring = objectstring.substring(0, 196) + "...";
		task.setobjectname(this.tasktitle);
		task.setSubject(objectstring);
		task.setDescription(this.taskdescription);
		task.setCode(this.taskcode);
		task.setGrouptask(BooleanChoiceDefinition.get().UNKN);
		task.setparentwithoutupdateforworkflow(workflowid);
		if (reassigncomment != null) {
			String reassigncommentcleaned = reassigncomment;
			if (reassigncommentcleaned.length() > 1995)
				reassigncommentcleaned = reassigncommentcleaned.substring(0, 1995) + "...";
			task.setComment(reassigncommentcleaned);
		}
		task.setlinkedobjectidfortaskobject(
				DataObjectId.generateDataObjectId(object.getId().getId(), object.getId().getObjectId()));
		task.insert();

		Date targetdate = null;
		if (this.delay > 0) {
			if (delayindays)
				targetdate = new Date(System.currentTimeMillis() + 86400 * 1000 * this.delay);
			if (!delayindays)
				targetdate = new Date(System.currentTimeMillis() + 60 * 1000 * this.delay);

		}
		if (targetdate != null)
			task.settargetdate(targetdate);
		if (forceuser == null)
			addUsersToTask(workflowid, object, task, null, workflowhelper);
		if (forceuser != null) {
			ArrayList<DataObjectId<Appuser>> recipients = new ArrayList<DataObjectId<Appuser>>();
			Taskuser taskuser = new Taskuser();
			taskuser.setleftobject(task.getId());
			taskuser.setrightobject(forceuser);
			recipients.add(forceuser);
			taskuser.insert();
			task.setGrouptask(BooleanChoiceDefinition.get().NO);
			task.update();
			String specificemailtype = this.emailtype;
			if (EMAIL_WFSETTING.equals(this.emailtype))
				specificemailtype = workflowhelper.getDefaultTaskEmailType();
			if (specificemailtype != null) {
				Workflow workflow = Workflow.readone(workflowid);
				WorkflowCommons.sendNotification("You received a task " + tasktitle,
						taskdescription + "\n\n" + reassigncomment, WorkflowCommons.generateObjectId(object),
						WorkflowCommons.generateObjectString(object), workflow.getCreateuserid(), recipients, emailtype,
						"CPLX:" + object.getName() + ":" + task.getCode(),
						object.getDefinitionFromObject().getModuleName(), true);
			}
		}
		for (int i = 0; i < choices.length; i++) {
			TaskChoiceTemplate thischoice = choices[i];
			Taskchoice thistaskchoice = new Taskchoice();
			thistaskchoice.setCode(thischoice.getCode());
			thistaskchoice.setobjectname(thischoice.getName());
			thistaskchoice.setparentwithoutupdatefortask(task.getId());
			thistaskchoice.setSelected(BooleanChoiceDefinition.get().UNKN);
			thistaskchoice.insert();
		}

	}

	@Override
	public void execute(DataObjectId<Workflow> workflowid, E object, ComplexWorkflowHelper<E, F> workflowhelper) {
		generateNewTask(workflowid, object, null, workflowhelper, null);
	}

	/**
	 * reassigns a task after workflow
	 * 
	 * @param object          data object that is the subject of the workflow
	 * @param workflowid      id of the workflows
	 * @param task            task to reassign users to
	 * @param useridtoexclude user to exclude (the user that rejected the tasks)
	 * @param workflowhelper  the workflow helper holding the workflow
	 */
	public void generateAfterReject(E object, DataObjectId<Workflow> workflowid, Task task,
			DataObjectId<Appuser> useridtoexclude, ComplexWorkflowHelper<E, F> workflowhelper) {
		addUsersToTask(workflowid, object, task, useridtoexclude, workflowhelper);
	}

	/**
	 * generates a new task after reassign
	 * 
	 * @param object          data object that is the subject of the workflow
	 * @param workflowid      id of the workflows
	 * @param userforreassign user to reassign the task to
	 * @param workflowhelper  helper of the workflow
	 * @param reassigncomment reassign comment
	 */
	public void generateAfterReassign(E object, DataObjectId<Workflow> workflowid,
			DataObjectId<Appuser> userforreassign, ComplexWorkflowHelper<E, F> workflowhelper, String reassigncomment) {
		generateNewTask(workflowid, object, userforreassign, workflowhelper, reassigncomment);
	}

}
