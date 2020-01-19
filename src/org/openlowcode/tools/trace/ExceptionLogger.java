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

import java.util.logging.Logger;

/**
 * An utility class to print logger in logs
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ExceptionLogger {
	/**
	 * sets the exception in logs
	 * 
	 * @param e           exception
	 * @param classlogger class logger
	 */
	public static void setInLogs(Throwable e, Logger classlogger) {
		classlogger.warning("------------------------------------------------------------------------------");
		classlogger.warning(e.getClass().toString() + " during processing " + e.getMessage());
		for (int i = 0; i < e.getStackTrace().length; i++) {
			classlogger.warning("   - " + e.getStackTrace()[i].toString());
		}
	}
}
