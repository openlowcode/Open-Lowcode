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

import org.openlowcode.module.system.action.ShowactivetaskAction;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.page.generated.AbsFrontpagePage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * Front page of the application, shows the pending tasks for the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Frontpage
		extends
		AbsFrontpagePage {

	/**
	 * creates a front page with the following attributes
	 * 
	 * @param specificmessage specific message
	 * @param activetasks     active tasks for the user
	 */
	public Frontpage(String specificmessage, Task[] activetasks) {
		super(specificmessage, activetasks);

	}

	@Override
	public String generateTitle(String specificmessage, Task[] activetasks) {
		int tasknumber = 0;
		if (activetasks != null)
			tasknumber = activetasks.length;
		if (tasknumber == 0)
			return "Welcome";
		if (tasknumber == 1)
			return "Welcome, you have 1 task pending.";
		return "Welcome, you have " + tasknumber + " tasks pending.";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		// specific module message
		SPageText specificmessagetext = new SPageText(this.getSpecificmessage(), SPageText.TYPE_NORMAL, this);
		mainband.addElement(specificmessagetext);
		// pending tasks
		SPageText titletask = new SPageText("Pending tasks", SPageText.TYPE_TITLE, this);
		mainband.addElement(titletask);

		SObjectArray<Task> pendingtaskarray = new SObjectArray<Task>("PENDINGTASKARRAY", this.getActivetasks(),
				Task.getDefinition(), this);
		pendingtaskarray.setMinFieldPriority(5);
		pendingtaskarray.hideAttribute(Task.getCompletedbyFieldMarker());
		pendingtaskarray.hideAttribute(Task.getCompleteddateFieldMarker());
		pendingtaskarray.hideAttribute(Task.getSelectedchoiceFieldMarker());
		pendingtaskarray.hideAttribute(Task.getStateFieldMarker());

		mainband.addElement(pendingtaskarray);
		ShowactivetaskAction.ActionRef showtask = ShowactivetaskAction.get().getActionRef();
		showtask.setTaskid(pendingtaskarray.getAttributeInput(Task.getIdMarker()));
		pendingtaskarray.addDefaultAction(showtask);
		return mainband;
	}

}
