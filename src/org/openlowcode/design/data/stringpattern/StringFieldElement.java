/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.stringpattern;

import org.openlowcode.design.data.StringField;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A string field element is a text element getting the content of a string
 * field on the data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringFieldElement
		extends
		PatternElement {
	private StringField field;

	/**
	 * creates a string field element referring to an object string field
	 * 
	 * @param field a field in the data object
	 */
	public StringFieldElement(StringField field) {
		this.field = field;
	}

	@Override
	public String generateSource() {
		return "object.get" + StringFormatter.formatForJavaClass(field.getName()) + "()";
	}

	@Override
	public String[] generateImport() {
		return new String[0];
	}

}
