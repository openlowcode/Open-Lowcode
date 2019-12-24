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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A simple CSV parser
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CSVParser implements FileParser {

	private int separator;
	private int enclosechar;
	private Reader reader;

	/**
	 * creates CSVParser with specified separators
	 * 
	 * @param separator   typically ',' or ';', the separator of fields
	 * @param enclosechar typically double quote '"', the field content may be
	 *                    enclosed by enclosed char with escape through two enclose
	 *                    chars,
	 */
	public CSVParser(int separator, int enclosechar, Reader reader) {
		this.separator = separator;
		this.enclosechar = enclosechar;
		this.reader = reader;
	}

	/**
	 * @param currentchar a character
	 * @return true if it is a line jump character
	 */
	public boolean isreturnChar(int currentchar) {
		if (currentchar == '\n')
			return true;

		return false;
	}

	/**
	 * @param currentchar a character
	 * @return true if windows special character for line jumb
	 */
	public boolean isWindowsParasite(int currentchar) {
		if (currentchar == '\r')
			return true;
		return false;
	}

	@Override
	public String[] parseOneLine() throws IOException {
		ArrayList<String> returnarray = new ArrayList<String>();
		int currentchar = reader.read();
		StringBuffer currentstring = new StringBuffer();
		boolean enclosed = false;
		boolean lastenclosed = false;

		while (currentchar != -1) {
			boolean treated = false;

			if (enclosed) {
				if (lastenclosed) {
					if (currentchar == enclosechar) {
						currentstring.append((char) currentchar);
						currentchar = -5;
						treated = true;
					} else {
						enclosed = false;
					}
				}
			} else {
				if (lastenclosed)
					enclosed = true;
			}

			if (!enclosed) {
				// change field
				if (currentchar == separator) {
					returnarray.add(currentstring.toString());
					currentstring = new StringBuffer();
					treated = true;
				}

				// finish line
				if (isreturnChar(currentchar)) {
					break;
				}
				if (isWindowsParasite(currentchar)) {
					treated = true;
				}

			}
			if (currentchar == enclosechar)
				treated = true;
			if (!treated)
				currentstring.append((char) currentchar);
			// recording separator
			if (currentchar == enclosechar) {
				lastenclosed = true;
			} else {
				lastenclosed = false;
			}
			currentchar = reader.read();
		}

		if (currentstring.length() > 0)
			returnarray.add(currentstring.toString());
		if (returnarray.size() > 0)
			return returnarray.toArray(new String[0]);
		return null;
	}

	/**
	 * @param parsedline the elements as parsed
	 * @return a valid csv line from those elements
	 */
	public String rebuildCSVLine(String[] parsedline) {
		StringBuffer returnvalue = new StringBuffer();
		for (int i = 0; i < parsedline.length; i++) {
			if (i > 0)
				returnvalue.append((char) (separator));

			String currentvalue = parsedline[i];
			boolean specialcharacter = false;
			if (currentvalue.indexOf('"') != -1)
				specialcharacter = true;
			if (currentvalue.indexOf(separator) != -1)
				specialcharacter = true;
			if (currentvalue.indexOf('\n') != -1)
				specialcharacter = true;
			if (specialcharacter) {
				returnvalue.append('"');

				returnvalue.append(currentvalue.replace("\"", "\"\""));
				returnvalue.append('"');
			} else {
				returnvalue.append(currentvalue);
			}
		}
		return returnvalue.toString();
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}
