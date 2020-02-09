/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.openlowcode.client.graphic.widget.CDecimalField;

import javafx.util.StringConverter;

/**
 * A big decimal string converter providing a lockable big decimal
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NiceLockableBigDecimalStringConverter extends StringConverter<CDecimalField.LockableBigDecimal> {
	private  DecimalFormat formatfordecimal;

	/**
	 * create converter for given precision and scale
	 * 
	 * @param precision         precision in the sense of java BigDecimal (total
	 *                          number of digits)
	 * @param scale             scale in the sense of java BigDecimal (number of
	 *                          digits right of comma)
	 */
	public NiceLockableBigDecimalStringConverter(int precision,int scale) {
		String formatfordecimalstring = "###,###.###";
		formatfordecimal = new DecimalFormat(formatfordecimalstring);
		DecimalFormatSymbols formatforsymbol = formatfordecimal.getDecimalFormatSymbols();
		formatforsymbol.setGroupingSeparator(' ');
		formatforsymbol.setDecimalSeparator('.');
		formatfordecimal.setDecimalFormatSymbols(formatforsymbol);
		formatfordecimal.setParseBigDecimal(true);
	}
	@Override
	public CDecimalField.LockableBigDecimal fromString(String valueasstring) {
		String newvaluecleaned = valueasstring.replaceAll("\\s+","");
		if (newvaluecleaned.equals("-")) return null;
		if (newvaluecleaned.length()==0) return new CDecimalField.LockableBigDecimal(false,null);
		if (newvaluecleaned.charAt(newvaluecleaned.length()-1)=='.') {
			newvaluecleaned = newvaluecleaned.substring(0,newvaluecleaned.length()-1);
		}
		BigDecimal bigdecimal = new BigDecimal(newvaluecleaned);
		return new CDecimalField.LockableBigDecimal(false,bigdecimal);
	}

	@Override
	public String toString(CDecimalField.LockableBigDecimal bigdecimal) {
		if (bigdecimal!=null) if (bigdecimal.getValue()!=null)
		return formatfordecimal.format(bigdecimal.getValue());
		return "";
	}
}
