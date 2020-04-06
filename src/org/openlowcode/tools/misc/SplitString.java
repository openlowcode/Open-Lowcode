/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.util.ArrayList;

/**
 * A class to split a string before printing in pdf by lines. It makes the
 * different between lines of the same paragraph (separator \r alone) and lines
 * of different paragraphes (both \r\n and \n, depending on Windows and Unix
 * string)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SplitString {
	private ArrayList<String> stringsplit;
	private ArrayList<Boolean> transitions;

	/**
	 * @return the number of sections of this string
	 */
	public int getNumberOfSections() {
		return stringsplit.size();
	}

	/**
	 * @param index a number between 0 (included) and getNumberOfSections()
	 *              (excluded)
	 * @return the line at the given index
	 */
	public String getSplitStringAt(int index) {
		return stringsplit.get(index);
	}

	/**
	 * @param index a number between 0 (included) and getNumberOfSections()
	 *              (excluded)
	 * @return the type of transition before the line at this index: true = new
	 *         paragraph, false = new line in paragraph<br>
	 *         the value is always true for the first line
	 */
	public boolean getTransitionAt(int index) {
		return transitions.get(index).booleanValue();
	}

	/**
	 * create a Split chain, parsing the string for new lines only (\n or \r\n)
	 * (carriage returns are kept in sections), or for all
	 * 
	 * @param chaintosplit the string to split
	 * @param onlymajor    if true, only new lines are considered, if false, new
	 *                     lines and carriage returns are considered
	 *                     @since 1.5
	 */
	public SplitString(String chaintosplit, boolean onlymajor) {
		stringsplit = new ArrayList<String>();
		transitions = new ArrayList<Boolean>();

		boolean lastsplitismajor = true;
		StringBuffer currentstring = new StringBuffer();
		int parseindex = 0;
		boolean lastisbackslashr = false;
		while (parseindex < chaintosplit.length()) {
			int currentchar = chaintosplit.charAt(parseindex);
			boolean specialcharacter = false;
			if (currentchar == 13) { // \r
				specialcharacter = true;
			}
			if (currentchar == 10) { // \n
				specialcharacter = true;
				stringsplit.add(currentstring.toString());
				transitions.add(new Boolean(lastsplitismajor));
				lastsplitismajor = true;
				lastisbackslashr = false;
				currentstring = new StringBuffer();
			}
			if (!specialcharacter) {
				if (lastisbackslashr) {
					if (!onlymajor) {
						stringsplit.add(currentstring.toString());
						transitions.add(new Boolean(lastsplitismajor));
						lastsplitismajor = false;
						currentstring = new StringBuffer();
					} else {
						currentstring.append((char) '\r');
					}
				}
				lastisbackslashr = false;
				currentstring.append((char) currentchar);

			}
			if (currentchar == 13)
				lastisbackslashr = true;
			parseindex++;
		}
		stringsplit.add(currentstring.toString());
		transitions.add(new Boolean(lastsplitismajor));
	}

	/**
	 * create a Split chain, parsing the string for carriage return and new lines
	 * 
	 * @param chaintosplit the string to split
	 */
	public SplitString(String chaintosplit) {
		this(chaintosplit, false);

	}
}
