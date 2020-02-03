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

import org.openlowcode.module.system.page.generated.AbsFaultymessagePage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * A page that should never be dislayed to the user as it is called by an action
 * generating a low-level error
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FaultymessagePage
		extends
		AbsFaultymessagePage {

	/**
	 * @param faultyargument
	 */
	public FaultymessagePage(String faultyargument) {
		super(faultyargument);
	}

	@Override
	public String generateTitle(String faultyargument) {
		return "You should never see this";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("You should never see this", SPageText.TYPE_WARNING, this));
		return mainband;
	}

}
