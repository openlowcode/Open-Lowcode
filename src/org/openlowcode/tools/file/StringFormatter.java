/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.file;

/**
 * An utility to format text. This is mostly used for code generation, but also
 * at other places on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringFormatter {
	/**
	 * trims the text, and put the first letter as upper case and subsequent letters
	 * as lower case
	 * 
	 * @param payload string to format
	 * @return the formatted string
	 */
	public static String formatForJavaClass(String payload) {
		String cleanpayload = payload.trim().toLowerCase();
		String firstletter = cleanpayload.substring(0, 1);
		String reminder = cleanpayload.substring(1);
		return firstletter.toUpperCase() + reminder.toLowerCase();
	}

	/**
	 * trims the text, and puts all letters as lower case
	 * 
	 * @param payload string to format
	 * @return the formatted string
	 */
	public static String formatForAttribute(String payload) {
		return payload.trim().toLowerCase();
	}

	/**
	 * escapes the string to be put in java code
	 * 
	 * @param payload a string
	 * @return the string with backslash doubled so that the string is a valid
	 *         string input inside java code
	 */
	public static String escapeforjavastring(String payload) {
		return payload.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n");

	}

}
