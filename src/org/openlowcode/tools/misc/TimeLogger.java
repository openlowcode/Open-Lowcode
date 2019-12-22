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
 * @author demau
 *
 */
public class TimeLogger {
	long lastlogtime;
	public TimeLogger() {
		lastlogtime = System.currentTimeMillis();
	}
	public String logTimer(String message) {
		long currentlogtime = System.currentTimeMillis();
		StringBuffer logmessage = new StringBuffer(" --- TIMELOG --- ");
		logmessage.append(message);
		logmessage.append(" [");
		long executiontime = currentlogtime-lastlogtime;
		logmessage.append(executiontime);
		logmessage.append("ms]");
		lastlogtime = currentlogtime;
		return logmessage.toString();
	}
	
	
}
