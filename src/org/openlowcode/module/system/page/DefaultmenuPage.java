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

import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.module.system.page.generated.AbsDefaultmenuPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SMenu;
import org.openlowcode.server.graphic.widget.SMenuBar;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * The default menu page add-on of the application. Will get a menu per module
 * and all key actions of the module shown as menu item
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DefaultmenuPage
		extends
		AbsDefaultmenuPage {

	/**
	 * creates the default menu page
	 */
	public DefaultmenuPage() {
		super();
	}

	@Override
	public String generateTitle() {
		return null;
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand pagemainframe = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SComponentBand upperband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);

		SMenuBar menubar = new SMenuBar(this);

		for (int i = 0; i < OLcServer.getServer().getModuleNumber(); i++) {
			SMenu modulemenu = OLcServer.getServer().getModule(i).getModuleMenu(this);
			menubar.addSMenu(modulemenu);
		}

		upperband.addElement(menubar);
		pagemainframe.addElement(upperband);
		Systemattribute serverlabel = ServerSecurityBuffer.getUniqueInstance().getSystemattribute("S0.SERVERLABEL");
		if (serverlabel != null)
			if (serverlabel.getValue() != null)
				if (serverlabel.getValue().length() > 0) {
					String label = serverlabel.getValue();
					pagemainframe.addElement(new SPageText(label, SPageText.TYPE_WARNING, this));
				}

		pagemainframe.addElement(this.insertInternalContent());
		return pagemainframe;
	}

}
