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

import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;

/**
 * A text data object field that can be stored on a simple field on a database.
 * There are typically limits, depending on type of databases, on how long a
 * text field can be. For MariaDB, maximum length if 65K bytes, which makes
 * around 20K characters worst case in UTF-8.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object of the field
 */
public class StringDataObjectField<E extends DataObject<E>>
		extends
		DataObjectField<StringDataObjectFieldDefinition<E>, E> {

	boolean easysearch;
	protected StoredField<String> content;
	protected StoredField<String> cleancontent;

	@SuppressWarnings("unchecked")
	public StringDataObjectField(StringDataObjectFieldDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.easysearch = definition.isEasysearch();
		content = (StoredField<String>) this.field.get(0);
		if (this.easysearch) {
			cleancontent = (StoredField<String>) this.field.get(1);
		}

	}

	/**
	 * sets the value of the string field
	 * 
	 * @param value new value of the string field
	 */
	public void setValue(String value) {
		if (value == null)
			value = "";
		if (value.length() > this.definition.getMaxlength())
			throw new RuntimeException(String.format(
					"field %s : incorrect length of string provided, expected %d char, got '%s' (%d char)",
					this.definition.getName(), this.definition.getMaxlength(), value, value.length()));

		this.content.setPayload(value);
		if (easysearch) {
			this.cleancontent.setPayload(value);
		}
	}

	/**
	 * get the value of the string field
	 * 
	 * @return get the value of the string field
	 */
	public String getValue() {
		return this.content.getPayload();
	}

	@Override
	public SimpleDataElt getDataElement() {
		return new TextDataElt(this.getName(), this.getValue());
	}

}
