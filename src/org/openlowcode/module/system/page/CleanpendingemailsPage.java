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

import org.openlowcode.module.system.action.CleanpendingemailsAction;
import org.openlowcode.module.system.action.generated.AtgLaunchsearchemailAction;
import org.openlowcode.module.system.data.Email;
import org.openlowcode.module.system.data.choice.CleanemailprocessChoiceDefinition;
import org.openlowcode.module.system.page.generated.AbsCleanpendingemailsPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SChoiceTextField;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SPageText;


/**
 * The page showing all pending e-mails to propose to clean them
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CleanpendingemailsPage extends AbsCleanpendingemailsPage {

	/**
	 * creates the page
	 * 
	 * @param pendingemails all pending e-mails
	 */
	public CleanpendingemailsPage(Email[] pendingemails)  {
		super(pendingemails);
		
	}

	@Override
	public String generateTitle(Email[] pendingemails)  {
		return "Clean Pending E-mails ("+
	(pendingemails!=null?pendingemails.length:0)+" elements)";
	}

	@Override
	protected SPageNode getContent()  {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);
		mainband.addElement(new SPageText("Clean pending e-mails", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText("Select specific e-mails below, or just click 'Clean' to clean all e-mails. By default, mails will be discarded, but they can also be relaunched.", SPageText.TYPE_NORMAL, this));
		AtgLaunchsearchemailAction.ActionRef back = AtgLaunchsearchemailAction.get().getActionRef();
		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);
		buttonband.addElement(new SActionButton("Back", back,this));
		CleanpendingemailsAction.ActionRef cleanemails = CleanpendingemailsAction.get().getActionRef();
		SChoiceTextField<CleanemailprocessChoiceDefinition> typeofrelaunch 
			= new SChoiceTextField<CleanemailprocessChoiceDefinition>("Type of Relaunch", 
					"RELAUNCHTYPE", 
					"", 
					CleanemailprocessChoiceDefinition.get(), 
					CleanemailprocessChoiceDefinition.get().DISCARD, 
					this, true, false, false, false, null);
		typeofrelaunch.setCompactShow();
		buttonband.addElement(typeofrelaunch);
		buttonband.addElement(new SActionButton("Clean",cleanemails,this));
		mainband.addElement(buttonband);
		SObjectArray<Email> emailstocleanarray = new SObjectArray<Email>("EMAILSTOCLEANARRAY", this.getPendingemails(), Email.getDefinition(),this);
		mainband.addElement(emailstocleanarray);
		emailstocleanarray.setAllowMultiSelect();
		cleanemails.setSelectedpendingemails(emailstocleanarray.getActiveObjectArray()); 
		cleanemails.setNewstatus(typeofrelaunch.getChoiceInput()); 
		return mainband;
	}

}
