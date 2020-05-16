/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.generation;

import java.util.logging.Logger;

/**
 * Utilities used for formatting for generating java code
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringFormatter {
	
	private static Logger logger = Logger.getLogger(StringFormatter.class.getName());
	
	/**
	 * format a text for a java class
	 * 
	 * @param payload a string text
	 * @return the text with first letter as upper case and the rest as lower case
	 */
	public static String formatForJavaClass(String payload) {
		String cleanpayload = payload.trim().toLowerCase();
		String firstletter = cleanpayload.substring(0, 1);
		String reminder = cleanpayload.substring(1);
		return firstletter.toUpperCase() + reminder.toLowerCase();
	}

	/**
	 * format a text for java attribute (puts it in lower case)
	 * 
	 * @param payload a string text
	 * @return the text with all letters as lower case
	 */
	public static String formatForAttribute(String payload) {
		return payload.trim().toLowerCase();
	}

	/**
	 * escape a string so that it is a valid java string inside double quotes
	 * 
	 * @param payload a string
	 * @return the string with backslash doubled so that the string is a valid
	 *         string input inside java code
	 */
	public static String escapeforjavastring(String payload) {
		return payload.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");

	}
	
	public static void checkNoJavaReservedName(String name) {
		String namecleaned = name.trim().toUpperCase();
		if (namecleaned.equals("OBJECT")) throw new RuntimeException("OBJECT not authorized as a data object name");

	}

}
