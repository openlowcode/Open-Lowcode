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
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A formatter for logs, to be used for file, so with all information necessary
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class FileFormatter extends Formatter {

		private SimpleDateFormat format;
		private String lineseparator;
		private boolean showlinenumber;
		public FileFormatter(boolean showlinenumber) {
			format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			lineseparator=System.getProperty("line.separator");
			this.showlinenumber = showlinenumber;
		}
		@Override
		public String format(LogRecord log) {
			// TODO Auto-generated method stub
			StringBuffer sb=new StringBuffer();
			sb.append(log.getLevel().getName());
			
			completeToLength(sb,8);
			
			sb.append(log.getThreadID());
			completeToLength(sb,13);
			sb.append(format.format(new java.util.Date(log.getMillis())));
			completeToLength(sb,38);
			sb.append(log.getSourceClassName());
			sb.append(".");
			sb.append(log.getSourceMethodName());
			if (this.showlinenumber) {
				sb.append("(");
				StackTraceElement callerFrame = null;

			    StackTraceElement stack[] = new Throwable().getStackTrace();
			    // Search the stack trace to find the calling class
			    for (int i = 0; i < stack.length; i++) {
			        final StackTraceElement frame = stack[i];
			        if (log.getSourceClassName().equals(frame.getClassName())) {
			            callerFrame = frame;
			            break;
			        }
			    }
			    sb.append(callerFrame.getLineNumber());
				sb.append(")");
			}
			completeToLength(sb,115);

			sb.append(log.getMessage());
			sb.append(lineseparator);
			
			return sb.toString();
		}
		public static void completeToLength(StringBuffer sb,int length) {
			char space=' ';
			while (sb.length()<length) sb.append(space);
		}

}

