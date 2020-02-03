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

import org.openlowcode.module.system.action.AudittextAction;
import org.openlowcode.module.system.action.GeneratefaultymessageAction;
import org.openlowcode.module.system.page.generated.AbsTechnicaltoolsPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;
/**
 * A page proposing various technical tools
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TechnicaltoolsPage extends AbsTechnicaltoolsPage {

	/**
	 * creates the technical tools page
	 */
	public TechnicaltoolsPage()  {
		super();
		
	}

	@Override
	public String generateTitle() {
		
		return "Technical Tools";
	}

	@Override
	protected SPageNode getContent()  {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);
		mainband.addElement(new SPageText("Technical Tools",SPageText.TYPE_TITLE, this));
		SComponentBand faultymessageband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);
		faultymessageband.addElement(new SPageText("Send faulty message",SPageText.TYPE_NORMAL,this));
		SActionButton faultymessagebutton = new SActionButton("Send", GeneratefaultymessageAction.get().getActionRef(), false, this);
		faultymessageband.addElement(faultymessagebutton);
		mainband.addElement(faultymessageband);
		mainband.addElement(new SPageText("You can perform a text audit below.",SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SPageText("The text you send will perform a round trip to the database and server, highlighting any encoding problem.",SPageText.TYPE_NORMAL, this));
		AudittextAction.ActionRef audittextaction = AudittextAction.get().getActionRef();
		STextField bigtext = new STextField("Text - part 1", "AUDITBIGTEXT","a text with specific characters you wish to test", 300, 
				"",false,this,false,false,false ,audittextaction);
		STextField smalltext = new STextField("Text - part 2", "AUDITSMALLTEXT","a text with specific characters you wish to test", 30, 
				"",false,this,false,false,false ,audittextaction);
		
		audittextaction.setTextfromclientbigfield(bigtext.getTextInput());
		audittextaction.setTextfromclientsmallfield(smalltext.getTextInput());
		mainband.addElement(bigtext);
		
		mainband.addElement(smalltext);
		
		SActionButton audittextbutton = new SActionButton("Audit", audittextaction,this);
		mainband.addElement(audittextbutton);
		return mainband;
	}

}
