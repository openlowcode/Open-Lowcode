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

import org.openlowcode.module.system.action.SessioncleaningAction;
import org.openlowcode.module.system.action.generated.AtgLaunchsearchusersessionAction;
import org.openlowcode.module.system.page.generated.AbsSessioncleaningPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SIntegerField;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * Page to perform session cleaning
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SessioncleaningPage extends AbsSessioncleaningPage {

	/**
	 * create the page 
	 * 
	 * @param numberofitems
	 * @param oldestlog
	 */
	public SessioncleaningPage(Integer numberofitems,Integer oldestlog)  {
		super(numberofitems,oldestlog);
		
	}

	@Override
	public String generateTitle(Integer numberofitems,Integer oldestlog)  {
		return "Clean Sessions";
	}

	@Override
	protected SPageNode getContent()  {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);
		mainband.addElement(new SPageText("Session Log cleaning",SPageText.TYPE_TITLE,this));
		SIntegerField numberofrecords = new SIntegerField("Number of records", "RECORDS", "", this.getNumberofitems(), false, this,true, false, false, null);
		mainband.addElement(numberofrecords);
		SIntegerField oldestlog = new SIntegerField("Oldest log (days)", "OLDESTLOGS", "", this.getOldestlog(), false, this,true, false, false, null);
		mainband.addElement(oldestlog);
		SessioncleaningAction.ActionRef sessioncleaningaction = SessioncleaningAction.get().getActionRef();
		SIntegerField keepdays = new SIntegerField("Keep days of history", "KEEPDAYS", "", new Integer(31), false, this,false, false, false, sessioncleaningaction);
		mainband.addElement(keepdays);
		sessioncleaningaction.setCleansessionsolderthandays(keepdays.getIntegerInput()); 
		SActionButton clean = new SActionButton("Clean older logs", sessioncleaningaction, this);
		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);
		buttonband.addElement(clean);
		AtgLaunchsearchusersessionAction.ActionRef searchsessionlogs = AtgLaunchsearchusersessionAction.get().getActionRef();
		buttonband.addElement(new SActionButton("Search Session Logs",searchsessionlogs,this));
		mainband.addElement(buttonband);
		return mainband;
	}

}
