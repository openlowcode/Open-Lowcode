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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyDynamicDefinitionHelper;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * The flexible decimal fields property allows to store, depending on context, a
 * varying number of decimal attributes. This is not intended to be persisted on
 * the database, but only to be used on transient objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object definition
 */
public class Flexibledecimalfields<E extends DataObject<E> & FlexibledecimalfieldsInterface<E>>
		extends DataObjectProperty<E> implements HasFlexibleDefinition<E> {
	private HashMap<String, BigDecimal> values;
	private FlexibledecimalfieldsDefinitionDynamicHelper<E> dynamichelper;
	private static Logger logger = Logger.getLogger(Flexibledecimalfields.class.getName());

	@Override
	public SimpleDataElt[] getDynamicDataElt() {
		// needs to write all values present when exist, and null else
		ArrayList<DataElt> elementstoreturn = new ArrayList<DataElt>();
		for (int i = 0; i < dynamichelper.getFieldNumber(); i++) {
			String thisfieldname = dynamichelper.getFieldNameAtIndex(i);
			BigDecimal value = values.get(thisfieldname);
			DecimalDataElt element = new DecimalDataElt(thisfieldname, value);
			elementstoreturn.add(element);
		}

		return elementstoreturn.toArray(new SimpleDataElt[0]);
	}

	/**
	 * creates a flexible decimal field property
	 * 
	 * @param definition    definition of the flexible decimal field property
	 * @param parentpayload payload of the parent object
	 */
	public Flexibledecimalfields(FlexibledecimalfieldsDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.values = new HashMap<String, BigDecimal>();
	}

	/**
	 * adds a flexible decimal value on a valid field for the context
	 * 
	 * @param object parent data object
	 * @param name   name of the attribute (not the display name / label)
	 * @param value  value to store
	 */
	public void addflexibledecimalvalue(E object, String name, BigDecimal value) {
		if (dynamichelper == null)
			throw new RuntimeException(
					"Flexible Decimal Field '" + name + "' value can only be set after dynamic helper is setup");
		if (!dynamichelper.hasField(Named.cleanName(name)))
			throw new RuntimeException("Field '" + name + "' is not defined as valid is dynamic helper, valid values = "
					+ dynamichelper.dropValidFieldNames());
		logger.finer("Add value " + value + " for field " + name + " for object " + object.dropIdToString());
		values.put(Named.cleanName(name), value);
	}

	/**
	 * gets the decimal value for the provided flexible decomal field
	 * 
	 * @param object parent data object
	 * @param name   name of the field (not the display name / label)
	 * @return the value of the flexible field for the given name
	 */
	public BigDecimal getflexibledecimalvalue(E object, String name) {
		if (dynamichelper == null)
			throw new RuntimeException(
					"Flexible Decimal Field '" + name + "' value can only be set after dynamic helper is setup");
		if (!dynamichelper.hasField(Named.cleanName(name)))
			throw new RuntimeException("Field '" + name + "' is not defined as valid is dynamic helper, valid values = "
					+ dynamichelper.dropValidFieldNames());

		return values.get(Named.cleanName(name));
	}

	/**
	 * sets the dynamic helper of this flexible decimal field. This defines the list
	 * of fields valid for the object in the precise execution context
	 * 
	 * @param dynamichelper dynamic helper for the context
	 */
	public void setDynamicHelper(FlexibledecimalfieldsDefinitionDynamicHelper<E> dynamichelper) {
		this.dynamichelper = dynamichelper;
	}

	@Override
	public PropertyDynamicDefinitionHelper<E, ?> getFlexibleDefinition() {
		return this.dynamichelper;
	}

}
