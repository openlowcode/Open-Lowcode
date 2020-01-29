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
 * creates a stored element in the database using a timestamp
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimestampStoredElement
		extends
		StoredElement {

	/**
	 * creates a timestamp stored element
	 * 
	 * @param suffix suffix to add to the object element name (can be empty)
	 */
	public TimestampStoredElement(String suffix) {
		super(suffix);
	}

	@Override
	public String getJavaFieldName() {
		return "Date";
	}

}
