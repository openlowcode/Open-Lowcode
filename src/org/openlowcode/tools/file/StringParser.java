/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.file;

import java.util.ArrayList;

/**
 * A utility class to parse elements with a separator that can be escaped by
 * doubling it. As an example, if separator is ':', splitting AA::BB:CC will
 * give back 'AA:BB' and CC.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringParser {
	/**
	 * @param origin         the origin string
	 * @param splitseparator the split sparator
	 * @return the array of splitted strings
	 */
	public static String[] splitwithdoubleescape(String origin, int splitseparator) {
		ArrayList<String> splitvalues = new ArrayList<String>();
		String remaining = origin;
		if (remaining == null)
			remaining = "";
		String currentcolumncollector = "";
		while (remaining.length() > 0) {

			int nextsplit = remaining.indexOf(splitseparator);
			if (nextsplit == -1) {
				splitvalues.add(currentcolumncollector + remaining.trim());
				remaining = "";
			}
			if (nextsplit >= 0) {
				// check if fake split (double separator)
				boolean fakesplit = false;
				if (remaining.length() > nextsplit + 1) {
					if (remaining.charAt(nextsplit + 1) == splitseparator)
						fakesplit = true;
				}
				if (!fakesplit) {
					splitvalues.add(currentcolumncollector + remaining.substring(0, nextsplit));
					currentcolumncollector = "";
					if (remaining.length() > nextsplit + 1) {
						remaining = remaining.substring(nextsplit + 1);
					} else {
						remaining = "";
					}
				}
				if (fakesplit) {
					currentcolumncollector += remaining.substring(0, nextsplit)
							+ remaining.substring(nextsplit, nextsplit + 1);
					if (remaining.length() > nextsplit + 2) {
						remaining = remaining.substring(nextsplit + 2);
					} else {
						splitvalues.add(currentcolumncollector);
						remaining = "";
					}
				}
			}
		}
		return splitvalues.toArray(new String[0]);
	}

	public static void main(String[] args) {
		String[] stringstoparse = { "TOTO", "TOTO:TATA:TITI:", "TOTO:TATA::TATA:TITI::" };
		for (int a = 0; a < stringstoparse.length; a++) {
			System.err.println("-------- parse string " + stringstoparse[a]);
			String[] splitdata = splitwithdoubleescape(stringstoparse[a], ':');
			for (int i = 0; i < splitdata.length; i++)
				System.err.println(splitdata[i]);
		}
	}
}
