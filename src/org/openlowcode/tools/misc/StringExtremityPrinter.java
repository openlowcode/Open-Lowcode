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
 * A utility class that prints the extremity of a string
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class StringExtremityPrinter {
	
	/**
	 * Generates a string providing the 'charnumber' elements
	 * at each side of the string
	 * @param payload the string to audit. It can be null
	 * or smaller than 2x charnumber
	 * @param charnumber number of chars to audit at each end
	 * @return
	 */
	public static String printextremity(String payload,int charnumber) {
		StringBuffer extremities = new StringBuffer();
		if (payload==null) return "NULL STRING";
		int startextremityindex = charnumber;
		if (startextremityindex>payload.length()) startextremityindex=payload.length();
		int endextremity = payload.length()-charnumber;
		if (endextremity<0) endextremity=0;
		
		for (int i=0;i<startextremityindex;i++) extremities.append(auditCharAt(payload,i));
		extremities.append("--");
		for (int i=endextremity;i<payload.length();i++) extremities.append(auditCharAt(payload,i));
		return extremities.toString();
	}
	private static String auditCharAt(String payload,int charindex) {
		int thischar = payload.charAt(charindex);
		return "("+thischar+")";
	}
}
