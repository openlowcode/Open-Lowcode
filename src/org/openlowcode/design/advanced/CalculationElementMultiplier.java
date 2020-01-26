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

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalField;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A calculation element using the decimal field as a multiplier
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CalculationElementMultiplier
		extends
		CalculationElement {

	private DecimalField field;
	private DataObjectDefinition parentobject;
	private boolean percentage;

	/**
	 * creates a multiplier calculation multiplier using the given decimal field
	 * 
	 * @param field field to use in the multiplier
	 */
	public CalculationElementMultiplier(DecimalField field) {
		this(field, false);
	}

	/**
	 * creates a multiplier calculation multiplier using the given decimal field
	 * 
	 * @param field      field to use in the multiplier
	 * @param percentage uses the field as a percentage (divides the value by 100
	 *                   when doing the multiplication)
	 */
	public CalculationElementMultiplier(DecimalField field, boolean percentage) {
		if (field == null)
			throw new RuntimeException("DecimalField cannot be null");
		if (field.getParentObject() == null)
			throw new RuntimeException("DecimalField " + field.getName() + " does not have a parent object");
		this.field = field;
		this.parentobject = field.getParentObject();
		this.percentage = percentage;
	}

	@Override
	public DataObjectDefinition getParent() {
		return parentobject;
	}

	@Override
	protected void writeMultiplier(SourceGenerator sg, String extraindent, String prefix) throws IOException {
		String objectvariable = StringFormatter.formatForAttribute(parentobject.getName());
		String fieldclass = StringFormatter.formatForJavaClass(field.getName());
		sg.wl(extraindent + " 			if (this" + objectvariable + "step" + prefix + ".get" + fieldclass
				+ "()!=null) {");
		sg.wl(extraindent + " 				step" + prefix + "multiplier = "
				+ (percentage ? "ReportTree.multiplyIfNotNull(" : "") + "ReportTree.multiplyIfNotNull(step" + prefix
				+ "multiplier,this" + objectvariable + "step" + prefix + ".get" + fieldclass + "())"
				+ (percentage ? ",new BigDecimal(0.01))" : "") + ";");
		sg.wl(extraindent + " 				}");

	}

}
