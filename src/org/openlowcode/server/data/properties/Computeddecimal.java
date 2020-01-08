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

import java.math.BigDecimal;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.formula.CalculatedField;
import org.openlowcode.server.data.formula.CalculatedFieldTrigger;
import org.openlowcode.server.data.formula.DataUpdateTrigger;
import org.openlowcode.server.data.formula.LocalPath;
import org.openlowcode.server.data.storage.StoredField;

/**
 * A computed decimal is holding a decimal value that is calculated from the
 * data of this data object or other linked data objects. There can be several
 * computed decimals in a single data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object of the computed decimal
 */
public class Computeddecimal<E extends DataObject<E>> extends DataObjectProperty<E> implements CalculatedField<E> {
	private StoredField<BigDecimal> computeddecimal;
	private ComputeddecimalDefinition<E> casteddefinition;

	/**
	 * creates a computed decimal property
	 * 
	 * @param definition definition of the property 
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public Computeddecimal(ComputeddecimalDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.casteddefinition = definition;
		computeddecimal = (StoredField<BigDecimal>) this.field.lookupOnName(this.getName().toUpperCase());
	}

	/**
	 * gets the value of the computed decimal
	 * @return the value (big decimal)
	 */
	public BigDecimal getComputeddecimal() {
		return this.computeddecimal.getPayload();
	}

	@Override
	public NamedList<DataUpdateTrigger<E>> UpdateValueWithCalculationResult(BigDecimal value) {
		computeddecimal.setPayload(value);
		if (computeddecimal.updated()) {
			NamedList<DataUpdateTrigger<E>> triggeredformula = casteddefinition.getTriggerlist();
			return triggeredformula;
		}
		return new NamedList<DataUpdateTrigger<E>>();
	}

	/**
	 * gets the trigger for the field calculation
	 * @return the trigger for the field calculation
	 */
	public CalculatedFieldTrigger<E, E> getFieldTrigger() {
		return new CalculatedFieldTrigger<E, E>(casteddefinition, new LocalPath<E>(casteddefinition.getParentObject()));
	}
}
