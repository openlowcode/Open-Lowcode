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
import java.util.ArrayList;
import java.util.Date;

import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.Field;
import org.openlowcode.server.data.storage.TimestampStoredField;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;

/**
 * A property is an element of a data object. It typicaly contains stored data,
 * and specific business logic
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class DataObjectProperty<E extends DataObject<E>>
		extends DataObjectElement<DataObjectPropertyDefinition<E>, E> {

	@SuppressWarnings("rawtypes")
	public DataObjectProperty(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		// Add external fields
		NamedList<ExternalFieldSchema> externalfields = definition.getAllExternalFields();
		for (int i = 0; i < externalfields.getSize(); i++) {
			this.field.add(externalfields.get(i).initBlankField());
		}

		updatedfieldingui = new NamedList<Field>();

		DataObjectFieldDefinition[] fielddefinition = this.definition.getDataObjectFieldDefinition(null);
		if (fielddefinition != null)
			for (int i = 0; i < fielddefinition.length; i++) {
				DataObjectFieldDefinition thisfielddef = fielddefinition[i];
				// only takes modifiable fields
				if (!thisfielddef.isReadOnly()) {
					Field field = this.field.lookupOnName(thisfielddef.getName());
					if (field == null)
						throw new RuntimeException("could not find a field with name = " + thisfielddef.getName()
								+ " in namedlist with content " + this.field.dropNameList());
					updatedfieldingui.add(field);
				}
			}

	}

	/**
	 * this methods shows the attributes that will be displayed of this property
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public SimpleDataElt[] getDisplayDataElt() {
		ArrayList<SimpleDataElt> returnedelements = new ArrayList<SimpleDataElt>();
		DataObjectFieldDefinition[] fielddefinition = this.definition.getDataObjectFieldDefinition(null);
		if (fielddefinition != null)
			for (int i = 0; i < fielddefinition.length; i++) {
				DataObjectFieldDefinition thisfielddef = fielddefinition[i];
				Field field = this.field.lookupOnName(thisfielddef.getName());
				if (field == null)
					throw new RuntimeException("could not find a field with name = " + thisfielddef.getName()
							+ " in namedlist with content " + this.field.dropNameList());

				returnedelements.add(generateSimpleDataEltFromObject(field, this.getName()));

			}
		SimpleDataElt[] dynamicdataelts = getDynamicDataElt();
		if (dynamicdataelts != null)
			for (int i = 0; i < dynamicdataelts.length; i++) {
				returnedelements.add(dynamicdataelts[i]);
			}
		return returnedelements.toArray(new SimpleDataElt[0]);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static SimpleDataElt generateSimpleDataEltFromObject(Field field, String propertyname) {
		SimpleDataElt answer = null;
		if (field.getPayload() == null)
			field.setPayload(field.getFieldSchema().initBlankField().getPayload());

		if (field.getPayload() instanceof String) {
			String stringpayload = (String) field.getPayload();
			answer = new TextDataElt(field.getName(), stringpayload);
		}

		if (field.getPayload() instanceof Date) {
			Date datepayload = (Date) field.getPayload();
			answer = new DateDataElt(field.getName(), datepayload);
		}
		if (field.getPayload() instanceof Integer) {
			Integer integerpayload = (Integer) field.getPayload();
			answer = new IntegerDataElt(field.getName(), integerpayload);
		}
		if (field.getPayload() instanceof BigDecimal) {
			BigDecimal decimalpayload = (BigDecimal) field.getPayload();
			answer = new DecimalDataElt(field.getName(), decimalpayload);
		}

		if (answer == null)
			if (field.getFieldSchema() instanceof TimestampStoredField) {
				Date datepayload = null;
				answer = new DateDataElt(field.getName(), datepayload);
			}

		if (answer == null)
			if (field.getFieldSchema() instanceof DecimalStoredField) {
				BigDecimal nulldecimal = null;
				answer = new DecimalDataElt(field.getName(), nulldecimal);
			}

		if (answer == null) {
			String objectinfo = "null object";
			if (field.getPayload() != null)
				objectinfo = field.getPayload().getClass().toString();
			throw new RuntimeException("object not supported " + objectinfo + ":" + field.getPayload() + " name = "
					+ field.getName() + "; propertyname = " + propertyname + ", fieldschema " + field.getFieldSchema());
		}

		if (propertyname != null)
			answer.setPropertyname(propertyname);
		return answer;
	}

	/**
	 * This method should be overridden by all properties having a dynamic number of
	 * fields
	 * 
	 * @return
	 * @throws GalliumException
	 */
	public SimpleDataElt[] getDynamicDataElt() {
		return new SimpleDataElt[0];
	}

	@SuppressWarnings("rawtypes")
	protected Field getFieldBufferForGUI(String name) {
		return this.updatedfieldingui.lookupOnName(name);
	}

	/**
	 * this stores some fields that may have been updated through the GUI. They are
	 * declared as a PropertyElementDisplayDefinition for modification
	 */
	@SuppressWarnings("rawtypes")
	protected NamedList<Field> updatedfieldingui;

}
