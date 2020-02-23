/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.pages;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.widget.CClientUpdate;
import org.openlowcode.client.graphic.widget.CComponentBand;
import org.openlowcode.client.graphic.widget.CPageText;

/**
 * An hardcoded page for client upgrade. This limits the dependencies between
 * clients and servers.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientUpgradePage
		extends
		CPage {
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MMM.YYYY");

	/**
	 * Creates a hardcoded page allowing client upgrade
	 * 
	 * @param clientversion       client version currently installed
	 * @param serverclientversion client version requested by the server
	 * @param serverclientdate    date of the new build of the client
	 * @param updaterjar          name of the updater jar (e.g. OLcUpdater.jar ).
	 *                            Example given is default
	 * @param updaterclass        name of the updater class (e.g.
	 *                            org.openlowcode.updater.ClientUpdater ). Example
	 *                            given is default
	 */
	public ClientUpgradePage(
			String clientversion,
			String serverclientversion,
			Date serverclientdate,
			String updaterjar,
			String updaterclass) {
		super("CLIENTUPGRADEPAGE");
		CComponentBand mainband = new CComponentBand(CComponentBand.DIRECTION_DOWN, this.getRootPath());
		mainband.addNode(new CPageText("Client Upgrade Required", true, this.getRootPath()));
		mainband.addNode(
				new CPageText("You are using client version " + clientversion + ".", false, this.getRootPath()));
		mainband.addNode(new CPageText("Server now requires client version " + serverclientversion + " from "
				+ sdf.format(serverclientdate) + ".", false, this.getRootPath()));
		mainband.addNode(new CPageText("To start client update, you may wait 10 seconds or click on below button.",
				false, this.getRootPath()));
		mainband.addNode(new CClientUpdate(this.getRootPath(), updaterjar, updaterclass));
		this.setPageNode(mainband);
	}

}
