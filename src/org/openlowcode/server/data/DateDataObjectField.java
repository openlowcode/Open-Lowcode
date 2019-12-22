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


import java.util.Date;

import org.openlowcode.server.data.storage.StoredField;

import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object
 */
public class DateDataObjectField<E extends DataObject<E>> extends DataObjectField<DateDataObjectFieldDefinition<E>, E> {

	private StoredField<Date> datefield;

	/**
	 * creates a new data object field from object payload
	 * 
	 * @param fieldefinition definition of the field
	 * @param parentpayload  payload of the object
	 */
	@SuppressWarnings("unchecked")
	public DateDataObjectField(DateDataObjectFieldDefinition<E> fieldefinition, DataObjectPayload parentpayload) {
		super(fieldefinition, parentpayload);
		this.datefield = (StoredField<Date>) this.field.get(0);
	}

	/**
	 * @param value the new payload
	 */
	public void setValue(Date value) {
		this.datefield.setPayload(value);
	}

	/**
	 * @return the date payload
	 */
	public Date getValue() {
		return (Date) this.datefield.getPayload();
	}

	@Override
	public SimpleDataElt getDataElement() {
		return new DateDataElt(this.getName(), this.getValue());
	}

}
