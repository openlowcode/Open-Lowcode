/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

/**
 * a simple class that returns a log precising the time elapsed since the object was created
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimeLogger {
	long lastlogtime;

	/**
	 * Creates the object and records the time
	 */
	public TimeLogger() {
		lastlogtime = System.currentTimeMillis();
	}

	/**
	 * returns a log with the duration since last call
	 * @param message message to put in log
	 * @return a log with the time elapsed since previous action on this TimeLogger and the messag specified
	 */
	public String logTimer(String message) {
		long currentlogtime = System.currentTimeMillis();
		StringBuffer logmessage = new StringBuffer(" --- TIMELOG --- ");
		logmessage.append(message);
		logmessage.append(" [");
		long executiontime = currentlogtime - lastlogtime;
		logmessage.append(executiontime);
		logmessage.append("ms]");
		lastlogtime = currentlogtime;
		return logmessage.toString();
	}

}
