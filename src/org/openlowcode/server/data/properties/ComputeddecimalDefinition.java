/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.formula.Formula;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.graphic.widget.SDecimalFormatter;

/**
 * definition of the computed decimal object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class ComputeddecimalDefinition<E extends DataObject<E>>
		extends
		DataObjectPropertyDefinition<E> {
	private DecimalStoredField computedValue;
	private String label;
	private Formula<E> formula;
	private SDecimalFormatter formatter;
	private int priority;

	public Formula<E> getFormula() {
		return this.formula;
	}

	/**
	 * creates a new definition for a computed decimal property
	 * 
	 * @param parentobject parent data object
	 * @param name         name of the computed decimal (there can be several per
	 *                     objects, name should be unique per object)
	 * @param label        label for display of the computed decimal
	 * @param precision    precision in the sense of java BigDecimal (total number
	 *                     of digits)
	 * @param scale        scale in the sense of java BigDecimal (maximum number of
	 *                     digits after comma)
	 * @param formula      the formula to compute the decimal
	 * @param formatter    formatter of the decimal field
	 * @param priority     priority for the display field (between -1000 and 1000)
	 */
	public ComputeddecimalDefinition(
			DataObjectDefinition<E> parentobject,
			String name,
			String label,
			int precision,
			int scale,
			Formula<E> formula,
			SDecimalFormatter formatter,
			int priority) {
		super(parentobject, name);
		computedValue = new DecimalStoredField(this.getName().toUpperCase(), null, precision, scale);
		this.addFieldSchema(computedValue);
		this.label = label;
		this.formula = formula;
		this.formatter = formatter;
		this.priority = priority;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[1];
		if (formatter == null)
			returnvalue[0] = new FieldSchemaForDisplay<E>(label, "Calculated field for " + label, computedValue, false,
					false, priority, 20, this.parentobject);
		if (formatter != null)
			returnvalue[0] = new FieldSchemaForDisplay<E>(label, "Calculated field for " + label, computedValue,
					formatter, false, false, priority, 20, this.parentobject);

		return returnvalue;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Computeddecimal<E>(this, parentpayload);
	}

	public String getTriggerName() {
		return parentobject.getModuleName().toUpperCase() + "/" + parentobject.getName().toUpperCase()
				+ ":COMPUTEDDECIMAL:" + this.getName().toUpperCase();
	}

}
