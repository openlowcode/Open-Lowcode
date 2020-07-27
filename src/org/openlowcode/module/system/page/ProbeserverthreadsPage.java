/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.data.Serverthread;
import org.openlowcode.module.system.page.generated.AbsProbeserverthreadsPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * Shows the server threads that were just probed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.11
 *
 */

public class ProbeserverthreadsPage
		extends
		AbsProbeserverthreadsPage {

	/**
	 * creates the page to display the server threads
	 * 
	 * @param threads server threads
	 */
	public ProbeserverthreadsPage(Serverthread[] threads) {
		super(threads);

	}

	@Override
	public String generateTitle(Serverthread[] threads) {
		return "Server Threads";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Current Server Threads", SPageText.TYPE_TITLE, this));
		SObjectArray<Serverthread> threadarray = new SObjectArray<Serverthread>("SERVERTHREAD", this.getThreads(),
				Serverthread.getDefinition(), this);
		threadarray.forceRowHeight(4);
		threadarray.setRowsToDisplay(10);
		mainband.addElement(threadarray);
		return mainband;
	}

}
