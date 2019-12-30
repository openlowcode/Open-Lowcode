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

import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * A field storing an integer value as payload.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class IntegerDataObjectField<E extends DataObject<E>>
		extends DataObjectField<IntegerDataObjectFieldDefinition<E>, E> {
	private StoredField<Integer> integerfield;

	/**
	 * creates an Integer DataObject field
	 * 
	 * @param integerDataObjectFieldDefinition field definition
	 * @param parentpayload                    object parent payload
	 */
	@SuppressWarnings("unchecked")
	public IntegerDataObjectField(IntegerDataObjectFieldDefinition<E> integerDataObjectFieldDefinition,
			DataObjectPayload parentpayload) {
		super(integerDataObjectFieldDefinition, parentpayload);
		this.integerfield = (StoredField<Integer>) this.field.get(0);
	}

	@Override
	public SimpleDataElt getDataElement() {
		return new IntegerDataElt(this.getName(), this.integerfield.getPayload());
	}

	/**
	 * sets the payload of the field
	 * 
	 * @param integer an Integer object
	 */
	public void setValue(Integer integer) {
		this.integerfield.setPayload(integer);
	}

	/**
	 * gets the field payload
	 * 
	 * @return the integer payload as Integer object
	 */
	public Integer getValue() {
		return integerfield.getPayload();
	}

}
