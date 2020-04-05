/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.trace;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides standard settings for loggers for unitary test benches.
 * Those are typically classes that test complex standalone components of the
 * framework, such as PDF printing or RichText edition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.5
 */
public class DebugLoggerSetup {
	/**
	 * setup logs that display in a compact way on console
	 */
	public static void setUpLogsForConsoleDebugging() {
		
		ConsoleHandler consolehandler = new ConsoleHandler();
		consolehandler.setFormatter(new ConsoleFormatter());
		
		Logger anonymouslogger = Logger.getLogger("");
		for (int i = 0; i < anonymouslogger.getHandlers().length; i++) {
			anonymouslogger.removeHandler(anonymouslogger.getHandlers()[i]);
		}
		anonymouslogger.addHandler(consolehandler);
		anonymouslogger.setUseParentHandlers(false);
		anonymouslogger.setLevel(Level.ALL);


		// --------------------------------------------------------------
		Logger rootlogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		for (int i = 0; i < rootlogger.getHandlers().length; i++) {
			rootlogger.removeHandler(rootlogger.getHandlers()[i]);
		}
		rootlogger.addHandler(consolehandler);
		rootlogger.setUseParentHandlers(false);
		rootlogger.setLevel(Level.ALL);
	}
}
