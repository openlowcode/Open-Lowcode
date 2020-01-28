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
 * A stored element storing a binary file in the persistent layer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LargeBinaryStoredElement
		extends
		StoredElement {

	/**
	 * craete a large binary stored element of the given name
	 * 
	 * @param name name of the element (valid java and sql field name)
	 */
	public LargeBinaryStoredElement(String name) {
		super(name);
	}

	@Override
	public String getJavaFieldName() {
		return "SFile";
	}

}
