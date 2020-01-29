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
 * 
 * a stored element in the database holding a texxt string
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringStoredElement
		extends
		StoredElement {
	private int length;

	/**
	 * creates a stored element holding a string as payload
	 * 
	 * @param suffix suffix to be added to the object element name
	 * @param length length of the field
	 */
	public StringStoredElement(String suffix, int length) {
		super(suffix);
		this.length = length;
	}

	@Override
	public String getJavaFieldName() {
		return "String";
	}

	/**
	 * @return the length of the field
	 */
	public int getLength() {
		return this.length;
	}
}
