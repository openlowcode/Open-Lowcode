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

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * the helper for a complex workflow defining the workflow logic
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition of the lifecycle of the object
 */
public abstract class ComplexWorkflowHelper<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> {

	public static final String EMAIL_NONE = null;
	public static final String EMAIL_NOW = "NOW";
	public static final String EMAIL_DELAY15MIN = "D15M";
	public static final String EMAIL_DELAY2H = "D2H";
	public static final String EMAIL_DAILY = "DAY";
	public static final String EMAIL_WEEKLY = "WEEKLY";
	public final static String START_CODE = "START";
	private HashMap<String, ComplexWorkflowStep<E, F>> steps;
	private HashMap<String, WorkflowTaskStep<E, F>> tasksteps;
	private String defaulttaskemailtype;

	/**
	 * @return the default delay and e-mail grouping
	 */
	public String getDefaultTaskEmailType() {
		return this.defaulttaskemailtype;
	}

	/**
	 * @return true if the workflow starts at insert
	 */
	public abstract boolean isStartAtInsert();

	/**
	 * checks if the workflow will start at change state for the specified new state
	 * 
	 * @param newstate new state
	 * @return true if the workflow has to start at change state
	 */
	public abstract boolean isStartAtChangeState(ChoiceValue<F> newstate);

	/**
	 * initializes task and steps
	 */
	public abstract void initializetaskandsteps();

	/**
	 * adds a task step
	 * 
	 * @param taskcode code of the task
	 * @param taskstep step of the task
	 */
	protected void addtaskstep(String taskcode, WorkflowTaskStep<E, F> taskstep) {
		this.tasksteps.put(taskcode, taskstep);
	}

	/**
	 * specifies what is the next step after a task
	 * 
	 * @param taskcode   code of the previous task
	 * @param choicecode choice if there is a condition
	 * @param step       step after the task
	 */
	protected void addnextstepaftertask(String taskcode, String choicecode, ComplexWorkflowStep<E, F> step) {
		this.steps.put(generatekeyfortaskresult(taskcode, choicecode), step);
	}

	/**
	 * creates a complex workflow helper
	 * 
	 * @param defaulttaskemailtype default task e-mail notification
	 */
	public ComplexWorkflowHelper(String defaulttaskemailtype) {
		boolean emailtypevalue = false;
		if (defaulttaskemailtype == null)
			emailtypevalue = true;
		if (defaulttaskemailtype.equals(EMAIL_NOW))
			emailtypevalue = true;
		if (defaulttaskemailtype.equals(EMAIL_DELAY15MIN))
			emailtypevalue = true;
		if (defaulttaskemailtype.equals(EMAIL_DELAY2H))
			emailtypevalue = true;
		if (defaulttaskemailtype.equals(EMAIL_DAILY))
			emailtypevalue = true;
		if (defaulttaskemailtype.equals(EMAIL_WEEKLY))
			emailtypevalue = true;

		if (!emailtypevalue)
			throw new RuntimeException(
					"email type is not valid, please refer to the class static string, given type =  '"
							+ defaulttaskemailtype + "'");

		this.defaulttaskemailtype = defaulttaskemailtype;
		steps = new HashMap<String, ComplexWorkflowStep<E, F>>();
		tasksteps = new HashMap<String, WorkflowTaskStep<E, F>>();
		initializetaskandsteps();
	}

	/**
	 * generates the key for a task output
	 * 
	 * @param taskcode   code of the task
	 * @param choicecode code of the outcome of the task (choice made by the user)
	 * @return a unique key
	 */
	private String generatekeyfortaskresult(String taskcode, String choicecode) {
		return taskcode + "/" + choicecode;
	}

	/**
	 * executes the next step
	 * 
	 * @param object          subject data object
	 * @param workflowid      id of the workflow
	 * @param currenttaskcode current task code
	 * @param choice          outcome of the task (choice of the user)
	 */
	public void executenext(E object, DataObjectId<Workflow> workflowid, String currenttaskcode, String choice) {
		ComplexWorkflowStep<E, F> step = steps.get(generatekeyfortaskresult(currenttaskcode, choice));
		if (step == null)
			throw new RuntimeException("task code '" + currenttaskcode + "' and choice '" + choice
					+ "' is not valid for workflow for object " + object.getName() + "/" + object.getId());
		step.execute(workflowid, object, this);
	}

	/**
	 * recreates the task after rejection by one of hte user
	 * 
	 * @param object          subject data object
	 * @param workflowid      id of the workflow
	 * @param task            current task
	 * @param useridtoexclude user id to exclude (because he rejected the task)
	 */
	public void regenerateTaskUserAfterReject(E object, DataObjectId<Workflow> workflowid, Task task,
			DataObjectId<Appuser> useridtoexclude) {
		WorkflowTaskStep<E, F> taskstep = tasksteps.get(task.getCode());
		if (taskstep == null)
			throw new RuntimeException("task code '" + task.getCode() + "'is not valid for workflow for object "
					+ object.getName() + "/" + object.getId());
		taskstep.generateAfterReject(object, workflowid, task, useridtoexclude, this);

	}

	/**
	 * reassigns the task
	 * 
	 * @param object          subject data object
	 * @param workflowid      if of the workflow
	 * @param newuser         new user
	 * @param code            code of the task (current choice)
	 * @param reassigncomment reassign comment of the original assignee
	 */
	public void reassigntask(E object, DataObjectId<Workflow> workflowid, DataObjectId<Appuser> newuser, String code,
			String reassigncomment) {
		WorkflowTaskStep<E, F> taskstep = tasksteps.get(code);
		if (taskstep == null)
			throw new RuntimeException("task code '" + code + "'is not valid for workflow for object "
					+ object.getName() + "/" + object.getId());
		taskstep.generateAfterReassign(object, workflowid, newuser, this, reassigncomment);

	}

}
