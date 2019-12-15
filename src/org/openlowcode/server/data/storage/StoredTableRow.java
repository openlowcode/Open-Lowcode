/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;

/**
 * A single row in a stored table
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class StoredTableRow {
	private StoredTableSchema schema;
	private static Logger logger = Logger.getLogger(StoredTableRow.class.getName());

	private NamedList<StoredField> payloadbyName;

	/**
	 * creates a row for the specified table schema
	 * 
	 * @param schema the schema of the table
	 */
	public StoredTableRow(StoredTableSchema schema) {
		this.schema = schema;
		payloadbyName = new NamedList<StoredField>();

		for (int i = 0; i < schema.getStoredFieldNumber(); i++) {

			StoredField field = new StoredField(schema.getStoredField(i));
			payloadbyName.add(field);

		}
	}

	/**
	 * sets the payload for specified field
	 * 
	 * @param field   field schema
	 * @param payload payload to add for this row for the field schema
	 */
	public <E> void setPayload(FieldSchema<E> field, E payload) {
		if (field == null)
			throw new RuntimeException("trying to set payload for a blank field for payload [" + payload
					+ "] for object " + schema.getName());
		StoredField thisstoredfield = payloadbyName.lookupOnName(field.getName());
		if (thisstoredfield == null) {
			String fieldsavailable = "[";
			for (int i = 0; i < payloadbyName.getSize(); i++) {
				if (i > 0)
					fieldsavailable += ",";
				fieldsavailable += payloadbyName.get(i).getName();

			}
			fieldsavailable += "]";
			throw new RuntimeException("trying to set payload for a blank field " + field.getName() + " for payload ["
					+ payload + "], for object" + schema.getName() + ", availablefields = " + fieldsavailable);
		}
		logger.finest("-- set value for field " + thisstoredfield.getName() + ", payload = " + payload);
		thisstoredfield.setPayload(payload);
	}

	/**
	 * gets the payload for the specified field
	 * 
	 * @param field the field schema
	 * @return the payload
	 */
	public <E> E getPayload(StoredFieldSchema<E> field) {
		StoredField thisfield = payloadbyName.lookupOnName(field.getName());
		logger.finest("-- get value of field " + thisfield.getName() + ", payload = " + thisfield.getPayload());
		return field.castToType(thisfield.getPayload());
	}

	/**
	 * get the table schema this row relates to
	 * 
	 * @return table schema
	 */
	public StoredTableSchema getStoredTableSchema() {
		return this.schema;
	}
}
