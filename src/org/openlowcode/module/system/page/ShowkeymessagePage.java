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

import org.openlowcode.module.system.page.generated.AbsShowkeymessagePage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;


/**
 * A page to show a key text message to the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowkeymessagePage extends AbsShowkeymessagePage {

	/**
	 * creates the page
	 * 
	 * @param message key message to display
	 */
	public ShowkeymessagePage(String message)  {
		super(message);
	}

	@Override
	public String generateTitle(String message){
		return "Message";
	}

	@Override
	protected SPageNode getContent()  {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);
		mainband.addElement(new SPageText(this.getMessage(),SPageText.TYPE_NORMAL,this));
		return mainband;
	}

}
