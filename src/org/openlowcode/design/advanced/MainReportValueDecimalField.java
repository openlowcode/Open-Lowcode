/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalField;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.StringFormatter;

/**
 * the main report value is the numeric data consolidated in a grid-like shape,
 * according to defined lines and columns
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MainReportValueDecimalField
		extends
		MainReportValue {
	private DataObjectDefinition parentobject;
	private DecimalField field;

	/**
	 * creates a main report value for a decimal field without a total column
	 * 
	 * @param field             decimal field
	 * @param valueconsolidator value consolidator. This explains how values should
	 *                          be consolidated (by sum, average...)
	 */
	public MainReportValueDecimalField(DecimalField field, ValueConsolidator valueconsolidator) {
		super(valueconsolidator);
		if (field == null)
			throw new RuntimeException("DecimalField cannot be null");
		if (field.getParentObject() == null)
			throw new RuntimeException("DecimalField " + field.getName() + " does not have a parent object");
		this.field = field;
		this.parentobject = field.getParentObject();
	}

	/**
	 * creates a main report value for a decimal field
	 * 
	 * @param field             decimal field
	 * @param valueconsolidator value consolidator. This explains how values should
	 *                          be consolidated (by sum, average...)
	 * @param hastotal          true if a total column should be created
	 */
	public MainReportValueDecimalField(DecimalField field, ValueConsolidator valueconsolidator, boolean hastotal) {
		super(valueconsolidator, hastotal);
		if (field == null)
			throw new RuntimeException("DecimalField cannot be null");
		if (field.getParentObject() == null)
			throw new RuntimeException("DecimalField " + field.getName() + " does not have a parent object");
		this.field = field;
		this.parentobject = field.getParentObject();
	}

	@Override
	public DataObjectDefinition getParentObject() {
		return this.parentobject;
	}

	@Override
	protected String printExtractor(String objectname) {

		return objectname + ".get" + StringFormatter.formatForJavaClass(field.getName()) + "()";
	}

	@Override
	public Field copyFieldForTotal(String newname, String newlabel) {
		return field.copy(newname, newlabel);
	}
}
