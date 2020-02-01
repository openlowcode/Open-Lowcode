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

import org.openlowcode.tools.misc.Named;

/**
 * a choice for a user task. Depending on the choice, the next step is indicated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UserTaskChoice
		extends
		Named {

	private String label;
	private String tooltip;
	private boolean commentcompulsory;
	private WorkflowStep nextstepforchoice;

	/**
	 * creates a choice
	 * 
	 * @param name              unique name of the choice for the task (valid java
	 *                          field name)
	 * @param label             plain default language description
	 * @param tooltip           multi-line tooltip
	 * @param commentcompulsory if true, a comment will be requested from the user.
	 *                          This is typically required when rejecting a task in
	 *                          a workflow to justify the rejection
	 */
	public UserTaskChoice(String name, String label, String tooltip, boolean commentcompulsory) {
		super(name);
		this.label = label;
		this.tooltip = tooltip;
		this.commentcompulsory = commentcompulsory;
	}

	/**
	 * sets the next step for choice
	 * 
	 * @param nextstepforchoice next workflow step if choice is selected by the user
	 *                          running the task
	 */
	public void addnextStep(WorkflowStep nextstepforchoice) {
		this.nextstepforchoice = nextstepforchoice;
	}

	/**
	 * @return get the workflow next step if the choice is chosen
	 */
	public WorkflowStep getNextStepForChoice() {
		return this.nextstepforchoice;
	}

	/**
	 * @return get the label (plain default language description of the choice)
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the multi-line tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @return true if comment is compulsory when choosing this task
	 */
	public boolean isCommentcompulsory() {
		return commentcompulsory;
	}

}
