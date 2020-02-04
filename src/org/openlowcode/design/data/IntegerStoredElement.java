
/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.design.data;

/**
 * a stored element that aims at storing an integer fitting with java integer
 * type (min value = -2147483648, max value = 2147483647)
 * 
 * @author Nicolas de Mauroy
 *
 */
public class IntegerStoredElement
		extends
		StoredElement {
	/**
	 * Creates an Integer Stored Element
	 * 
	 * @param suffix suffix of the data element
	 */
	public IntegerStoredElement(String suffix) {
		super(suffix);

	}

	@Override
	public String getJavaFieldName() {
		return "Integer";
	}

}
