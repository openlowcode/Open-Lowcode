/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.action.AccepttaskAction;
import org.openlowcode.module.system.action.CompletetaskAction;
import org.openlowcode.module.system.action.DisplaygenericobjectAction;
import org.openlowcode.module.system.action.ReassigntaskAction;
import org.openlowcode.module.system.action.RejecttaskAction;
import org.openlowcode.module.system.action.SavetaskcommentAction;
import org.openlowcode.module.system.action.generated.AtgShowworkflowAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskchoice;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.TaskuserDefinition;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.generated.AbsShowactivetaskPage;
import org.openlowcode.module.system.page.generated.AtgSearchappuserPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SObjectDisplay;
import org.openlowcode.server.graphic.widget.SObjectIdStorage;
import org.openlowcode.server.graphic.widget.SObjectSearcher;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.SPopupButton;
import org.openlowcode.server.graphic.widget.STabPane;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * the page to show an active to the user wishing to validate it
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowactivetaskPage
		extends
		AbsShowactivetaskPage {

	/**
	 * shows an active task to a user being assigned to it
	 * 
	 * @param task       task object
	 * @param objectid   id of the subject object (object the workflow is running
	 *                   on)
	 * @param taskchoice list of available choices
	 * @param taskusers  other users
	 * @param canaccept  true if the user can accept the task
	 * @param canreject  true if the user can reject the task
	 * @param comment    current comment for the task
	 */
	public ShowactivetaskPage(
			Task task,
			DataObjectId<?> objectid,
			Taskchoice[] taskchoice,
			Taskuser[] taskusers,
			ChoiceValue<BooleanChoiceDefinition> canaccept,
			ChoiceValue<BooleanChoiceDefinition> canreject,
			String comment) {
		super(task, objectid, taskchoice, taskusers, canaccept, canreject, comment);

	}

	@Override
	public String generateTitle(
			Task task,
			@SuppressWarnings("rawtypes") DataObjectId objectid,
			Taskchoice[] taskchoice,
			Taskuser[] taskusers,
			ChoiceValue<BooleanChoiceDefinition> canaccept,
			ChoiceValue<BooleanChoiceDefinition> canreject,
			String comment) {
		return " Process the task " + task.getName() + ".";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SPageNode getContent() {

		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SObjectDisplay<
				Task> taskdisplay = new SObjectDisplay<Task>("TASK", this.getTask(), Task.getDefinition(), this, true);
		taskdisplay.setMinFieldPriority(5);
		mainband.addElement(taskdisplay);
		taskdisplay.hideAttribute(Task.getStateFieldMarker());
		taskdisplay.hideAttribute(Task.getCompletedbyFieldMarker());
		taskdisplay.hideAttribute(Task.getCompleteddateFieldMarker());
		taskdisplay.hideAttribute(Task.getGrouptaskFieldMarker());
		taskdisplay.hideAttribute(Task.getSelectedchoiceFieldMarker());

		AccepttaskAction.ActionRef accepttask = AccepttaskAction.get().getActionRef();
		accepttask.setTaskid(taskdisplay.getAttributeInput(Task.getIdMarker()));
		SActionButton accept = new SActionButton("Accept assignment", accepttask, this);
		accept.setConditionalShow(this.getCanaccept());

		RejecttaskAction.ActionRef rejecttask = RejecttaskAction.get().getActionRef();
		rejecttask.setTaskid(taskdisplay.getAttributeInput(Task.getIdMarker()));
		SActionButton reject = new SActionButton("Reject assignment", rejecttask, this);
		reject.setConditionalShow(this.getCanreject());

		DisplaygenericobjectAction.ActionRef displayobject = DisplaygenericobjectAction.get().getActionRef();
		@SuppressWarnings("rawtypes")
		SObjectIdStorage subjectid = new SObjectIdStorage("SUBJECTIDSTORAGE", this, this.getObjectid());
		mainband.addElement(subjectid);

		SComponentBand taskacceptance = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SComponentBand otherassignees = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		STabPane bottompane = new STabPane(this);
		mainband.addElement(bottompane);
		bottompane.addElement(taskacceptance, "Task Management");
		bottompane.addElement(otherassignees, "Other assignees");

		displayobject.setGenericid(subjectid.getObjectIdInput());
		SActionButton displayobjectbutton = new SActionButton("Go to Subject", displayobject, this);
		taskdisplay.addPageNodeRightOfTitle(displayobjectbutton);
		AtgShowworkflowAction.ActionRef showparentworkflow = AtgShowworkflowAction.get().getActionRef();
		showparentworkflow.setId(taskdisplay.getAttributeInput(Task.getLinkedtoparentforworkflowidMarker()));
		// unused button, to be cleaned, see GitHub issue #25
		@SuppressWarnings("unused")
		SActionButton showparentworkflowbutton = new SActionButton("Workflow overview", showparentworkflow, this);
		taskacceptance.addElement(new SPageText("You can enter comment on a task before complete or reassign..",
				SPageText.TYPE_NORMAL, this));
		CompletetaskAction.ActionRef completetask = CompletetaskAction.get().getActionRef();

		STextField comment = new STextField("Comment", "COMMENT",
				"validation comment, typically compulsory for  rejecting a task", 1000, "", false, this, false, false,
				false, completetask, true);
		comment.setTextBusinessData(this.getComment());
		taskacceptance.addElement(comment);

		taskacceptance.addElement(new SPageText(
				"To validate the task, please select a choice below and press complete.", SPageText.TYPE_NORMAL, this));
		SObjectArray<Taskchoice> choice = new SObjectArray<Taskchoice>("CHOICES", this.getTaskchoice(),
				Taskchoice.getDefinition(), this);
		choice.hideAttribute(Taskchoice.getSelectedFieldMarker());
		choice.setMinFieldPriority(700);
		taskacceptance.addElement(choice);

		completetask.setTaskid(taskdisplay.getAttributeInput(Task.getIdMarker()));
		completetask.setTaskchoiceid(choice.getAttributeInput(Taskchoice.getIdMarker()));
		completetask.setComment(comment.getTextInput());

		SavetaskcommentAction.ActionRef savecomment = SavetaskcommentAction.get().getActionRef();
		savecomment.setTaskid(taskdisplay.getAttributeInput(Task.getIdMarker()));
		savecomment.setComment(comment.getTextInput());

		SActionButton complete = new SActionButton("Complete", completetask, this);
		SActionButton savecommentbutton = new SActionButton("Save comment", savecomment, this);
		ReassigntaskAction.ActionRef reassigntaskaction = ReassigntaskAction.get().getActionRef();

		SComponentBand reassigntaskpopup = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SPageText reassigntasklabel = new SPageText("Select a user for reassign", SPageText.TYPE_TITLE, this);
		reassigntaskpopup.addElement(reassigntasklabel);
		SObjectSearcher<
				Appuser> reassigntaskusersearch = AtgSearchappuserPage.getsearchpanel(this, "REASSIGNUSERSEARCH");
		reassigntaskpopup.addElement(reassigntaskusersearch);

		reassigntaskaction.setTaskid(taskdisplay.getAttributeInput(Task.getIdMarker()));
		reassigntaskaction.setComment(comment.getTextInput());
		reassigntaskaction
				.setNewuserid(reassigntaskusersearch.getresultarray().getAttributeInput(Appuser.getIdMarker()));

		SActionButton reassigntaskexecutebutton = new SActionButton("Execute reassign", reassigntaskaction, this);
		reassigntaskpopup.addElement(reassigntaskexecutebutton);
		SPopupButton reassigntaskopenbutton = new SPopupButton(this, reassigntaskpopup, "Reassign",
				"transfer the task to another person. You take the responsibility that the new person is legitimate.");

		SComponentBand bottombuttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);
		bottombuttonband.addElement(savecommentbutton);
		bottombuttonband.addElement(accept);
		bottombuttonband.addElement(reject);
		bottombuttonband.addElement(complete);
		bottombuttonband.addElement(reassigntaskopenbutton);
		taskacceptance.addElement(bottombuttonband);
		otherassignees.addElement(new SPageText("Other assignees for the task", SPageText.TYPE_TITLE, this));
		otherassignees.addElement(new SPageText("The table below shows other users who have received the task.",
				SPageText.TYPE_NORMAL, this));
		SObjectArray<Taskuser> otherusers = new SObjectArray<Taskuser>("OTHERUSERS", this.getTaskusers(),
				Taskuser.getDefinition(), this);
		otherusers
				.addDisplayProfile(TaskuserDefinition.getTaskuserDefinition().getDisplayProfileHideleftobjectfields());
		choice.setMinFieldPriority(0);
		otherassignees.addElement(otherusers);
		return mainband;
	}

}
