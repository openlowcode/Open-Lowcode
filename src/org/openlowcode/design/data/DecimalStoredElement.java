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

/**
 * A decimal stored element stores a potentially large number in decimal format.
 * This corresponds to the java BigDecimal object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DecimalStoredElement
		extends
		StoredElement {
	private int scale;
	private int precision;

	/**
	 * Creates a decimal stored element with the provided name
	 * 
	 * @param suffix    local name of the element
	 * @param precision the total number of digits in the number excluding minus
	 *                  sign and decimal point (e.g. 8 for 11345.432, 13 for
	 *                  -112333451.4232)
	 * @param scale     number of digits after the decimal point (e.g. 3 for
	 *                  11345.432, 4 for -112333451.4232)
	 */
	public DecimalStoredElement(String suffix, int precision, int scale) {
		super(suffix);
		this.scale = scale;
		this.precision = precision;
	}

	@Override
	public String getJavaFieldName() {
		return "BigDecimal";
	}

	/**
	 * @return the scale of the number (e.g. 3 for 11345.432, 4 for -112333451.4232)
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * @return the total number of digits in the number excluding minus sign and
	 *         decimal point (e.g. 8 for 11345.432, 13 for -112333451.4232)
	 */
	public int getPrecision() {
		return precision;
	}

}
