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
 * A condition that will check that a string field is not empty
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConditionStringNotEmpty
		extends
		ConditionForPattern {
	private StringField stringfield;

	/**
	 * creates a condition that will return true if the StringField payload is not
	 * empty
	 * 
	 * @param stringfield the string field to check for content
	 */
	public ConditionStringNotEmpty(StringField stringfield) {
		this.stringfield = stringfield;
	}

	@Override
	public String writeCondition() {

		return "object.get" + StringFormatter.formatForJavaClass(stringfield.getName()) + "().length()>0";
	}
}
