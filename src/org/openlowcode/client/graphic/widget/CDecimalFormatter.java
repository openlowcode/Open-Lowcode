/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * creates a decimal formatter (alternative graphical show of the decimal field)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CDecimalFormatter
		implements
		ValueFormatter<CDecimalField.LockableBigDecimal> {

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
	private DecimalFormat formatfordecimal;

	/**
	 * creates a decimal formatter from the server message
	 * 
	 * @param reader reader of the message coming from the server
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if a problem happens during the message
	 *                            transmission
	 */
	public CDecimalFormatter(MessageReader reader) throws OLcRemoteException, IOException {
		reader.returnNextStartStructure("DEF");
		minimum = reader.returnNextDecimalField("MIN");
		maximum = reader.returnNextDecimalField("MAX");
		linear = reader.returnNextBooleanField("LIN");
		colorscheme = reader.returnNextBooleanField("CLS");
		reader.returnNextEndStructure("DEF");
		String formatfordecimalstring = "###,###.###";
		formatfordecimal = new DecimalFormat(formatfordecimalstring);
		DecimalFormatSymbols formatforsymbol = formatfordecimal.getDecimalFormatSymbols();
		formatforsymbol.setGroupingSeparator(' ');
		formatforsymbol.setDecimalSeparator('.');
		formatfordecimal.setDecimalFormatSymbols(formatforsymbol);
		formatfordecimal.setParseBigDecimal(true);
	}

	/**
	 * @return the minimum for display of the range
	 */
	public BigDecimal getMinimum() {
		return minimum;
	}

	/**
	 * @return maximum for display of the range
	 */
	public BigDecimal getMaximum() {
		return maximum;
	}

	/**
	 * @return true if display is linear, false if display is logarithmic
	 */
	public boolean isLinear() {
		return linear;
	}

	/**
	 * @return true if specific color scheme is enabled
	 */
	public boolean isColorscheme() {
		return colorscheme;
	}

	@Override
	public Node getWidget(CDecimalField.LockableBigDecimal value) {
		if (value != null)
			if (value.getValue() != null) {
				double floatvalue = value.getValue().floatValue();
				double floatmaximum = maximum.floatValue();
				double floatminimum = minimum.floatValue();
				double valuetodisplay = 0;
				if (linear) {

					valuetodisplay = (floatvalue - floatminimum) / (floatmaximum - floatminimum);
					if (valuetodisplay > 1)
						valuetodisplay = 1;
					if (valuetodisplay < 0)
						valuetodisplay = 0;
				}
				if (!linear) {
					if (floatvalue > floatminimum) {
						double valuelog = Math.log10((floatvalue / floatminimum));
						double intervallog = Math.log10(floatmaximum / floatminimum);
						valuetodisplay = valuelog / intervallog;
						if (valuetodisplay < 0)
							valuetodisplay = 0;
						if (valuetodisplay > 1)
							valuetodisplay = 1;
					}

				}
				ProgressBar progressbar = new ProgressBar(valuetodisplay);

				progressbar.setStyle("-fx-accent: " + toRGBCode(getColorForRange(valuetodisplay)) + "; ");

				progressbar.setTooltip(new Tooltip(
						(value != null ? (value.getValue() != null ? formatfordecimal.format(value.getValue()) : "")
								: "")));
				return progressbar;
			}

		Label label = new Label("");
		return label;

	}

	/**
	 * gets color for range
	 * 
	 * @param valuetodisplay value to display (between 0,1)
	 * @return the color in the default range
	 */
	public static Color getColorForRange(double valuetodisplay) {
		if (valuetodisplay < 0.5)
			return Color.color(0.8, valuetodisplay * 1.6, 0);
		return Color.color((1 - valuetodisplay) * 1.6, 0.8, 0);
	}

	/**
	 * get the rgb code string for use in javafx
	 * 
	 * @param color the color
	 * @return a formatted string for use in javafx
	 */
	public static String toRGBCode(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}
}
