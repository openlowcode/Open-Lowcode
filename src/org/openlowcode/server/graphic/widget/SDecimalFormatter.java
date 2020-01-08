/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;
import java.math.BigDecimal;

import org.openlowcode.tools.messages.MessageWriter;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SDecimalFormatter {
	private BigDecimal minimum;
	private BigDecimal maximum;

	private boolean linear;
	private boolean colorscheme;

	/**
	 * creates a decimal formatter, i.e. display the given number as a progress bar
	 * 
	 * @param minimum     minimum of the scale of the bar
	 * @param maximum     maximum of the scale of the bar
	 * @param linear      true: progress bar will be linear, false: progress bar
	 *                    will be decimal exponential : minimum, 10*minimum,
	 *                    100*minimum
	 * @param colorscheme true: red for minimum, yellow in the middle, green for
	 *                    maximum, false: from slightly dark blue to pale blue
	 */
	public SDecimalFormatter(BigDecimal minimum, BigDecimal maximum, boolean linear, boolean colorscheme) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.linear = linear;
		this.colorscheme = colorscheme;
	}

	/**
	 * writes the formatter in a message
	 * @param writer writer of the message
 	 * @throws IOException if any communication is encountered writing the message
	 */
	public void writepayload(MessageWriter writer) throws IOException {
		writer.addDecimalField("MIN", minimum);
		writer.addDecimalField("MAX", maximum);
		writer.addBooleanField("LIN", linear);
		writer.addBooleanField("CLS", colorscheme);

	}

}
