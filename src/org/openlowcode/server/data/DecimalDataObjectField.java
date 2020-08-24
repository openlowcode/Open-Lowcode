/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.math.BigDecimal;

import java.util.logging.Logger;

import org.openlowcode.server.data.formula.DataUpdateTrigger;

import org.openlowcode.server.data.storage.StoredField;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * A decimal field for an object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class DecimalDataObjectField<E extends DataObject<E>>
		extends DataObjectField<DecimalDataObjectFieldDefinition<E>, E> {
	private static final Logger logger = Logger.getLogger(DecimalDataObjectField.class.getName());
	private StoredField<BigDecimal> decimalfield;

	/**
	 * @param decimalDataObjectFieldDefinition
	 * @param parentpayload
	 */
	@SuppressWarnings("unchecked")
	public DecimalDataObjectField(DecimalDataObjectFieldDefinition<E> decimalDataObjectFieldDefinition,
			DataObjectPayload parentpayload) {
		super(decimalDataObjectFieldDefinition, parentpayload);
		this.decimalfield = (StoredField<BigDecimal>) this.field.get(0);
	}

	/**
	 * This will ensure there are no errors when scale is restricted to a number,
	 * and a figure has more zeros than what is authorized by the scale
	 * 
	 * @param input a BigDecimal
	 * @param scale the authorized scale
	 * @return
	 */
	public static BigDecimal cleanBigDecimal(BigDecimal input, int scale) {
		if (input == null)
			return null;
		BigDecimal processeddecimal = input.stripTrailingZeros();
		if (processeddecimal.scale() < scale)
			processeddecimal = processeddecimal.setScale(scale);
		return processeddecimal;
	}

	/**
	 * cheks that the BigDecimal is compliant with format, and if so, sets the new
	 * value to the provided input
	 * 
	 * @param value candidate for new value
	 */
	public void setValue(BigDecimal value) {
		BigDecimal cleanvalue = cleanBigDecimal(value, definition.getScale());
		if (cleanvalue != null)
			if (cleanvalue.precision() > this.definition.getPrecision())
				throw new RuntimeException(String.format(
						"field %s : incorrect precision of decimal provided, expected %d decimal, got '%d' (%d char)",
						this.definition.getName(), this.definition.getPrecision(), cleanvalue, cleanvalue.precision()));
		if (cleanvalue != null)
			if (cleanvalue.scale() > this.definition.getScale())
				throw new RuntimeException(String.format(
						"field %s : incorrect length of decimal provided, expected %d decimal, got '%d' (%d char)",
						this.definition.getName(), this.definition.getScale(), cleanvalue, cleanvalue.scale()));

		this.decimalfield.setPayload(cleanvalue);

	}

	/**
	 * @return the big decimal payload
	 */
	public BigDecimal getValue() {

		if (this.decimalfield != null)
			return this.decimalfield.getPayload();
		return null;
	}

	@Override
	public SimpleDataElt getDataElement() {
		return new DecimalDataElt(this.getName(), this.getValue());
	}

	@Override
	public NamedList<DataUpdateTrigger<E>> getTriggersForThisUpdate() {
		if (decimalfield.updated()) {
			NamedList<DataUpdateTrigger<E>> triggerlist = definition.getTriggerlist();
			logger.fine("Update on decimal field " + decimalfield.getName() + ", sending triggers "
					+ triggerlist.getSize());
			return definition.getTriggerlist();
		} else {
			logger.fine("No update on decimal field " + decimalfield.getName() + ", sending nothing ");
			return new NamedList<DataUpdateTrigger<E>>();
		}
	}

	@Override
	public NamedList<DataUpdateTrigger<E>> getAllTriggersForRefresh() {
		NamedList<DataUpdateTrigger<E>> triggerlist = definition.getTriggerlist();
		logger.fine("Force refresh on decimal field " + decimalfield.getName() + ", sending triggers "
				+ triggerlist.getSize());
		return definition.getTriggerlist();

	}
	public static String printDecimal(BigDecimal decimal) {
		if (decimal==null) return null;
		return decimal.toPlainString();
	}
}
