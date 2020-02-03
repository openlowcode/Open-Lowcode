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

import org.openlowcode.module.system.action.AddlogsAction;
import org.openlowcode.module.system.action.ResetextralogsAction;
import org.openlowcode.module.system.data.choice.LoglevelChoiceDefinition;
import org.openlowcode.module.system.page.generated.AbsAddlogsPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SChoiceTextField;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;


public class AddlogsPage extends AbsAddlogsPage {

	public AddlogsPage()  {
		super();
	}

	@Override
	public String generateTitle()  {
		return "Add logs to server";
	}

	@Override
	protected SPageNode getContent()  {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);
		mainband.addElement(new SPageText("Add logs to Server",SPageText.TYPE_TITLE,this));
		mainband.addElement(new SPageText("You can immediately add logs to the server by entering a class full name (e.g. gallium.module.system.page.AddlogsAction) or an alias.",SPageText.TYPE_NORMAL,this));
		mainband.addElement(new SPageText("The following aliases are available: @PERSISTENCE, @SECURITY.",SPageText.TYPE_NORMAL,this));
		
		STextField path = new STextField("Log path","LOGPATH","a java log path or an alias",150,"",false,this,false,false,false,null);
		mainband.addElement(path);
		SChoiceTextField<LoglevelChoiceDefinition> filelevel = new  SChoiceTextField<LoglevelChoiceDefinition>
		("File level","FILELOG","File Log", LoglevelChoiceDefinition.get(), LoglevelChoiceDefinition.get().INFO, this, true, false, false, false, null);
		mainband.addElement(filelevel);
		
		SChoiceTextField<LoglevelChoiceDefinition> consolelevel = new  SChoiceTextField<LoglevelChoiceDefinition>
		("Console level","CONSOLELOG","Console Log", LoglevelChoiceDefinition.get(), LoglevelChoiceDefinition.get().INFO, this, true, false, false, false, null);
		mainband.addElement(consolelevel);
		
		AddlogsAction.ActionRef addlogaction = AddlogsAction.get().getActionRef();
		addlogaction.setPath(path.getTextInput()); 
		addlogaction.setTextlog(filelevel.getChoiceInput());
		addlogaction.setConsolelog(consolelevel.getChoiceInput());
		SActionButton addlogbutton = new SActionButton("Add", addlogaction, false, this);
		
		mainband.addElement(addlogbutton);
		ResetextralogsAction.ActionRef resetlogaction = ResetextralogsAction.get().getActionRef();
		mainband.addElement(new SActionButton("Reset special logs", resetlogaction, this));
		return mainband;
	}

}
