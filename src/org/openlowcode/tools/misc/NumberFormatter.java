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
 * An utility method to format big numbers with prefix, in an easy to read
 * format. E.G. will format 1234567 as 1.2M
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NumberFormatter {
	/**
	 * @param number a number
	 * @return an easy to read form expressed in K (1000), M (1000000) or G
	 *         (1000000000)
	 */
	public static String formatNumber(long number) {
		if (number < 1000)
			return "" + number;
		if ((number >= 1000) & (number < 1000000))
			return "" + (number / 1000) + "." + ((number / 100) % 10) + "K";
		if ((number >= 1000000) & (number < 1000000000))

			return "" + (number / 1000000) + "." + ((number / 100000) % 10) + "M";
		return "" + (number / 1000000000) + "." + ((number / 100000000) % 10) + "G";

	}
}
