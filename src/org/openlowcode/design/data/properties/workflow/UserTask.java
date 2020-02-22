/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.data.properties.basic.ComplexWorkflow;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * A user task is a workflow step where the user will make a choice on the
 * workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UserTask
		extends
		WorkflowStep {
	private String taskmessage;
	private int delay;
	private boolean delayindays;
	private TaskUserSelector selector;
	private String code;
	private String title;
	private NamedList<UserTaskChoice> alltaskchoices;

	/**
	 * creates a user task
	 * 
	 * @param parent                the parent workflow
	 * @param code                  code of the user task (should be unique)
	 * @param title                 title of the task
	 * @param taskmessage           the task message that will be displayed. It can
	 *                              potentially be rich-text using Open Lowcode rich-text
	 *                              language
	 * @param delay                 number of units (see below delayindays) for
	 *                              delay before task is considered late
	 * @param delayindays           if true, delay is expressed in days, else in
	 *                              minutes
	 * @param notifyrecipientbymail if true, recipients will receive an e-mail (not:
	 *                              probably not used)
	 * @param selector              a selector of the users the task should be sent
	 *                              to
	 */
	public UserTask(
			ComplexWorkflow parent,
			String code,
			String title,
			String taskmessage,
			int delay,
			boolean delayindays,
			boolean notifyrecipientbymail,
			TaskUserSelector selector) {
		super(parent);
		this.code = code;
		this.title = title;
		this.taskmessage = taskmessage;
		this.delay = delay;
		this.delayindays = delayindays;
		this.selector = selector;
		alltaskchoices = new NamedList<UserTaskChoice>();

		this.selector = selector;
		this.getParent().registerUserTask(this);
	}

	@Override
	public String gettaskid() {
		return code;
	}

	/**
	 * @return the task selector (determines next steps depending on user choice)
	 */
	public TaskUserSelector getUserSelector() {
		return this.selector;
	}

	/**
	 * @return delay for the task
	 */
	public int getDelay() {
		return this.delay;
	}

	/**
	 * @return true if delay is in days, false if delay is in minutes
	 */
	public boolean isDelayInDays() {
		return this.delayindays;
	}

	/**
	 * @return get the task message (can be several lines)
	 */
	public String getTaskmessage() {
		return taskmessage;
	}

	/**
	 * @return get the task title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the number of task choices
	 */
	public int getTaskChoiceNumber() {
		return alltaskchoices.getSize();
	}

	/**
	 * the task choice at the given index
	 * 
	 * @param index a number between 0 (included) and getTaskChoiceNumber (excluded)
	 * @return the task choice at the given index
	 */
	public UserTaskChoice getChoiceAtIndex(int index) {
		return alltaskchoices.get(index);
	}

	/**
	 * sets a possible choice and associated next steps
	 * 
	 * @param taskchoice task choice presented  to the user
	 * @param nextstep next step for this choice
	 */
	public void setUserTaskChoice(UserTaskChoice taskchoice, WorkflowStep nextstep) {
		if (alltaskchoices.lookupOnName(taskchoice.getName()) != null)
			throw new RuntimeException("Duplicate task choice name " + taskchoice.getName());
		if (nextstep.getParent() != this.getParent())
			throw new RuntimeException("Can only link user task (wf:" + this.getParent().getInstancename()
					+ ") for nextstep " + taskchoice.getName() + " (wf: " + nextstep.getParent()
					+ ") with a task of same workflow ");
		alltaskchoices.add(taskchoice);
		taskchoice.addnextStep(nextstep);
	}

	/**
	 * get the  task code
	 * 
	 * @return the task code
	 */
	public String getCode() {
		return this.code;

	}

	@Override
	public void registerToParentWorkflow()  {
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {
		// nothing to do

	}

	@Override
	public void writedeclaration(SourceGenerator sg, Module module, String parentclass, String lifecycleclass)
			throws  IOException {

	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module) throws IOException {
		for (int i = 0; i < this.getTaskChoiceNumber(); i++) {
			UserTaskChoice choice = this.getChoiceAtIndex(i);
			sg.wl("		this.addnextstepaftertask(\"" + this.code.toUpperCase() + "\",\""
					+ choice.getName().toUpperCase() + "\"," + choice.getNextStepForChoice().gettaskid().toLowerCase()
					+ ");");
		}
	}
}
