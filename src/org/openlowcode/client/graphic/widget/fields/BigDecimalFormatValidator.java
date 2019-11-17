/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.fields;

/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * A format validator to enter numbers. It manages all tricky cases during entry
 * of numbers, such as having trailing zeros after decimal dot
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class BigDecimalFormatValidator implements FormatValidator {
	private int precision;
	private int scale;
	private DecimalFormat formatfordecimal;

	/**
	 * creates a validator for Big Decimal. Edition is done in the Open Lowcode
	 * standard decimal format, with thousands separated by space and decimal
	 * separator with a '.'
	 * 
	 * @param precision precision as defined in java Big Decimal
	 * @param scale     scale as defined in java Big Decimal
	 */
	public BigDecimalFormatValidator(int precision, int scale) {
		this.precision = precision;
		this.scale = scale;
		String formatfordecimalstring = "###,###.###";
		formatfordecimal = new DecimalFormat(formatfordecimalstring);
		DecimalFormatSymbols formatforsymbol = formatfordecimal.getDecimalFormatSymbols();
		formatforsymbol.setGroupingSeparator(' ');
		formatforsymbol.setDecimalSeparator('.');
		formatfordecimal.setDecimalFormatSymbols(formatforsymbol);
		formatfordecimal.setParseBigDecimal(true);
	}

	/**
	 * @param character a character
	 * @return true if between 1 and 9, false else
	 */
	public static boolean isNonZeroFigure(char character) {
		if (character >= '1')
			if (character <= '9')
				return true;
		return false;
	}

	/**
	 * @param character a character
	 * @return true if figure between 0 and 9, false else
	 */
	public static boolean isFigure(char character) {
		if (character == '0')
			return true;
		return isNonZeroFigure(character);
	}

	/**
	 * @param s a string representing a decimal
	 * @return true is there are trailing zeros after decimal separator. 1.00
	 *         returns true, 1.01 returns false, 100 returns false.
	 */
	public String finishesbyzerosafterdecimal(String s) {
		if (s == null)
			return null;
		boolean founddot = false;
		int numberofzeros = 0;
		int numberofdecimalbeforezero = 0;
		int numberofpotentialdecimalbeforezero = 0;
		for (int i = 0; i < s.length(); i++) {
			if (founddot) {
				if (isFigure(s.charAt(i)))
					numberofpotentialdecimalbeforezero++;
				if (s.charAt(i) == '0')
					numberofzeros++;
				if (isNonZeroFigure(s.charAt(i))) {
					numberofdecimalbeforezero = numberofpotentialdecimalbeforezero;
					numberofzeros = 0;
				}

			}

			if (s.charAt(i) == '.')
				founddot = true;
		}
		if (founddot)
			if (numberofdecimalbeforezero > 0)
				if (numberofzeros > 0) {
					StringBuffer endstring = new StringBuffer();
					for (int i = 0; i < numberofzeros; i++) {
						if (i > 0)
							if ((i + numberofdecimalbeforezero) % 3 == 0)
								endstring.append(' ');
						endstring.append('0');
					}
					return endstring.toString();
				}
		return null;
	}

	/**
	 * @param s
	 * @return
	 */
	public String finishesbydotandzero(String s) {
		if (s == null)
			return null;
		boolean founddot = false;
		int numberofzeros = 0;
		for (int i = 0; i < s.length(); i++) {
			if (founddot)
				if (s.charAt(i) == ' ' || s.charAt(i) == '0') {
					if (s.charAt(i) == '0')
						numberofzeros++;
				} else {
					return null;
				}
			if (s.charAt(i) == '.')
				founddot = true;

		}
		if (founddot)
			if (numberofzeros > 0) {
				StringBuffer endstring = new StringBuffer();
				endstring.append('.');
				for (int i = 0; i < numberofzeros; i++) {
					if (i > 0)
						if (i % 3 == 0)
							endstring.append(' ');
					endstring.append('0');
				}
				return endstring.toString();
			}
		return null;
	}

	@Override
	public String valid(String valueasstring) {

		if (valueasstring.length() > 0) {

			try {
				String newvaluecleaned = valueasstring.replaceAll("\\s+", "");
				if (valueasstring.equals("-"))
					return ("-");
				boolean hasdotatend = false;
				String dotsandzerosatend = null;
				String zerosatend = null;
				if (newvaluecleaned.length() > 0)
					if (newvaluecleaned.charAt(newvaluecleaned.length() - 1) == '.') {
						hasdotatend = true;
						newvaluecleaned = newvaluecleaned.substring(0, newvaluecleaned.length() - 1);
					}
				dotsandzerosatend = finishesbydotandzero(newvaluecleaned);
				zerosatend = finishesbyzerosafterdecimal(newvaluecleaned);
				BigDecimal bigdecimal = new BigDecimal(newvaluecleaned);
				int entryprecision = bigdecimal.precision();
				int entryscale = bigdecimal.scale();
				if ((entryprecision - entryscale) > (precision - scale))
					return null;
				if (entryscale > scale)
					return null;
				return formatfordecimal.format(bigdecimal) + (hasdotatend ? "." : "")
						+ (dotsandzerosatend != null ? dotsandzerosatend : "") + (zerosatend != null ? zerosatend : "");
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return valueasstring;
	}

}
