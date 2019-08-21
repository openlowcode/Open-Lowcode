/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.desktop;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Desktop Services gathers utilities to interact with the desktop
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class DesktopServices {
	private static Logger logger = Logger.getLogger(DesktopServices.class.getName());

	/**
	 * Tries to open a web page in the desktop default browser. In its current
	 * version, it will likely only work correctly on windows.
	 * 
	 * @param uri a valid URI / URL for the browser (e.g. https://openlowcode.com/
	 *            ).
	 */
	public static void launchBrowser(String uri) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (Exception e) {
				logger.severe("Exception while trying to open site " + uri + " " + e.getClass().toString() + " "
						+ e.getMessage());
			}
		} else {
			logger.severe("Java Desktop services not supported on this platform");
		}
	}
}
