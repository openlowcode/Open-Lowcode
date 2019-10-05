/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.trace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * a formatter for console display, focusing on being concise
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class ConsoleFormatter extends Formatter {
	static SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
	@Override
	public String format(LogRecord record) {
		StringBuffer output = new StringBuffer();
		output.append(String.format("%-8s",record.getLevel().toString()));
		output.append(' ');
		output.append(sdf.format(new Date()));
		output.append(' ');
		output.append(record.getMessage());
		output.append('\n');
		return output.toString();
	}

}
