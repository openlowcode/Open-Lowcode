/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.math.BigDecimal;

/**
 * A decimal formatter will display a decimal as a progress bar
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DecimalFormatter {

	private BigDecimal minimum;
	private BigDecimal maximum;

	/**
	 * true: progress bar will be linear false: progress bar will be decimal
	 * exponential : minimum, 10*minimum, 100*minimum
	 */
	private boolean linear;

	/**
	 * true: red for minimum, yellow in the middle, green for maximum false: from
	 * slightly dark blue to pale blue
	 */
	private boolean colorscheme;

	/**
	 * creates a decimal formatter
	 * 
	 * @param minimum     value corresponding to empty progress bar
	 * @param maximum     value corresponding to full progress bar
	 * @param linear      true if linear, false if logarithmic (e.g. 10% progress
	 *                    bar = 1, 20% progress bar = 10, 30* progress bar = 100)
	 * @param colorscheme true: red, yellow, green, false : dark to pale blue
	 */
	public DecimalFormatter(BigDecimal minimum, BigDecimal maximum, boolean linear, boolean colorscheme) {
		this.minimum = minimum;
		if (maximum.compareTo(minimum) <= 0)
			throw new RuntimeException("maximum should be strictly superior to minimum");
		this.maximum = maximum;
		this.linear = linear;
		if (!this.linear)
			if (minimum.compareTo(new BigDecimal(0)) <= 0)
				throw new RuntimeException("for logarithmic scale, minimum should be strictly positive");
		this.colorscheme = colorscheme;
	}

	/**
	 * Creates a decimal formatter between 0, and 1, linear, and with color schema
	 * red/yellow/green
	 */
	public DecimalFormatter() {
		this(new BigDecimal(0), new BigDecimal(1), true, true);
	}

	/**
	 * generates the definition of the formatter
	 * 
	 * @return the definition of the formatter
	 */
	public String generateDefinition() {
		return "new SDecimalFormatter(new BigDecimal(" + minimum.toPlainString() + "),new BigDecimal("
				+ maximum.toPlainString() + ")," + linear + "," + colorscheme + ")";
	}
}
