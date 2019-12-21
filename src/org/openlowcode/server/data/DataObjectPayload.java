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

import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.storage.DeleteQuery;
import org.openlowcode.server.data.storage.Field;
import org.openlowcode.server.data.storage.MultipleTableRow;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.StoredField;

import org.openlowcode.server.data.storage.StoredTableRow;
import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.data.storage.UpdateQuery;

/**
 * the DataObjectPayload stores the fields, properties, and the link to the
 * persistence layer. Each Data Object contains a DataObjectPayload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectPayload {

	@SuppressWarnings("rawtypes")
	private NamedList<DataObjectField> fields;
	@SuppressWarnings("rawtypes")
	private NamedList<DataObjectProperty> properties;
	private StoredTableSchema schema;

	/**
	 * gets the property with the specified name
	 * 
	 * @param name name
	 * @return the property if it exists, else null
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectProperty lookupPropertyOnName(String name) {
		return properties.lookupOnName(name);
	}

	/**
	 * gets the field with the specified name
	 * 
	 * @param name name
	 * @return the field if it exists, else null
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectField lookupSimpleFieldOnName(String name) {
		return fields.lookupOnName(name);
	}

	/**
	 * @return the number of fields in this object payload
	 */
	public int getFieldNumber() {
		return fields.getSize();
	}

	/**
	 * @param index and integer between 0 (included) and getFieldNumber (excluded)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectField getFieldAtIndex(int index) {
		return fields.get(index);
	}

	/**
	 * @return the number of properties
	 */
	public int getPropertyNumber() {
		return properties.getSize();
	}

	/**
	 * @param index and integer between 0 (included) and getPropertyNumber
	 *              (excluded)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectProperty getPropertyAtIndex(int index) {
		return properties.get(index);
	}

	/**
	 * Creates a new DataobjectPayload linked to the specified persisted schema
	 * 
	 * @param schema schema for this data object payload
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectPayload(StoredTableSchema schema) {
		this.schema = schema;
		fields = new NamedList<DataObjectField>();
		properties = new NamedList<DataObjectProperty>();
	}

	/**
	 * adds a field to this DataobjectPayload
	 * 
	 * @param field the field added
	 */
	@SuppressWarnings("rawtypes")
	public void addField(DataObjectField field) {
		this.fields.add(field);
	}

	/**
	 * adds a property to this DataobjectPayload
	 * 
	 * @param property the property added
	 */
	@SuppressWarnings("rawtypes")
	public void addProperty(DataObjectProperty property) {
		this.properties.add(property);
	}

	/**
	 * initiates this DataobjectPayload from the row of data
	 * 
	 * @param row   a row from the databse
	 * @param alias the alias to look at
	 */
	public void initFromDB(Row row, TableAlias alias) {
		for (int i = 0; i < fields.getSize(); i++) {
			@SuppressWarnings("rawtypes")
			DataObjectElement field = fields.get(i);
			field.initFromDB(row, alias);

		}
		for (int i = 0; i < properties.getSize(); i++) {
			@SuppressWarnings("rawtypes")
			DataObjectProperty property = properties.get(i);
			property.initFromDB(row, alias);
		}
	}

	/**
	 * generates the list of stored fields for this object, taking into account all
	 * fields and all properties
	 * 
	 * @return the list of stored fields
	 */
	@SuppressWarnings("rawtypes")
	private NamedList<StoredField> generateStoredFieldList() {
		NamedList<StoredField> allfields = new NamedList<StoredField>();

		for (int i = 0; i < fields.getSize(); i++) {
			DataObjectField thisfield = fields.get(i);
			for (int j = 0; j < thisfield.getFieldNumber(); j++) {
				Field field = thisfield.getStoredField(j);
				if (field instanceof StoredField)
					allfields.add((StoredField) (field));
			}
		}
		for (int i = 0; i < properties.getSize(); i++) {
			DataObjectProperty thisproperty = properties.get(i);
			for (int j = 0; j < thisproperty.getFieldNumber(); j++) {
				Field field = thisproperty.getStoredField(j);
				if (field instanceof StoredField) {
					allfields.add((StoredField) (field));
				}
			}
		}
		return allfields;
	}

	/**
	 * Gets a stored table row initiated with the data in this DataobjectPayload
	 * 
	 * @return the stored table row object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private StoredTableRow getStoredObject() {
		StoredTableRow row = new StoredTableRow(schema);
		for (int i = 0; i < fields.getSize(); i++) {
			DataObjectField thisfield = fields.get(i);
			for (int j = 0; j < thisfield.getFieldNumber(); j++) {
				Field field = thisfield.getStoredField(j);
				row.setPayload(field.getFieldSchema(), field.getPayload());
			}
		}
		for (int i = 0; i < properties.getSize(); i++) {
			DataObjectProperty thisproperty = properties.get(i);
			for (int j = 0; j < thisproperty.getFieldNumber(); j++) {
				Field field = thisproperty.getStoredField(j);
				if (field instanceof StoredField) {

					row.setPayload(field.getFieldSchema(), field.getPayload());
				}
			}
		}
		return row;
	}

	/**
	 * 
	 */
	public void insert() {
		PersistentStorage store = PersistenceGateway.getStorage();
		store.insertOnDB(getStoredObject());

		PersistenceGateway.checkinStorage(store);

	}

	/**
	 * Performs a massive update
	 * 
	 * @param payloads        the payloads of objects to update
	 * @param queryconditions the query conditions for each object to ensure only
	 *                        data entered is updated
	 */
	public static void massiveupdate(DataObjectPayload[] payloads, QueryCondition[] queryconditions) {
		if (payloads == null)
			throw new RuntimeException("Payload table is null");
		if (queryconditions == null)
			throw new RuntimeException("QueryCondition is null");

		if (payloads.length != queryconditions.length)
			throw new RuntimeException("Payload length " + payloads.length
					+ " is different from query condition length " + queryconditions.length);
		if (payloads.length > 0) {
			MultipleTableRow multiplerow = new MultipleTableRow(payloads[0].schema);
			for (int i = 0; i < payloads.length; i++) {
				if (i > 0)
					multiplerow.setNextQuery();
				multiplerow.addAllStoredFieldToCurrentRow(payloads[i].generateStoredFieldList());
				multiplerow.addQueryCondition(queryconditions[i]);
			}
			PersistentStorage store = PersistenceGateway.getStorage();
			store.MassiveUpdateOnDB(multiplerow);
			PersistenceGateway.checkinStorage(store);
		}
	}

	/**
	 * Performs a massive delete in the databse
	 * 
	 * @param payloads        the array of payloads to delete
	 * @param queryconditions query conditions for each payload (array has to be
	 *                        same length as payload array)
	 */
	public static void massivedelete(DataObjectPayload[] payloads, QueryCondition[] queryconditions) {
		if (payloads == null)
			throw new RuntimeException("Payload table is null");
		if (queryconditions == null)
			throw new RuntimeException("QueryCondition is null");

		if (payloads.length != queryconditions.length)
			throw new RuntimeException("Payload length " + payloads.length
					+ " is different from query condition length " + queryconditions.length);
		if (payloads.length > 0) {
			MultipleTableRow multiplerow = new MultipleTableRow(payloads[0].schema);
			for (int i = 0; i < payloads.length; i++) {
				if (i > 0)
					multiplerow.setNextQuery();
				multiplerow.addQueryCondition(queryconditions[i]);
			}
			PersistentStorage store = PersistenceGateway.getStorage();
			store.MassiveDeleteOnDB(multiplerow);
			PersistenceGateway.checkinStorage(store);
		}
	}

	/**
	 * performs a massive insert of a series of payloads. This uses massive array
	 * processing in the database, and is typically significantly faster
	 * 
	 * @param payloads the array of payload.
	 */
	public static void massiveinsert(DataObjectPayload[] payloads) {
		if (payloads != null)
			if (payloads.length > 0) {
				MultipleTableRow multiplerow = new MultipleTableRow(payloads[0].schema);
				for (int i = 0; i < payloads.length; i++) {
					if (i > 0)
						multiplerow.setNextQuery();
					multiplerow.addAllStoredFieldToCurrentRow(payloads[i].generateStoredFieldList());
				}
				PersistentStorage store = PersistenceGateway.getStorage();
				store.MassiveInsertOnDB(multiplerow);
				PersistenceGateway.checkinStorage(store);
			}
	}

	/**
	 * performs an update in the persistence layer
	 * 
	 * @param condition this method should be called with a query condition ensuring
	 *                  that the correct rows are updated
	 */
	public void update(QueryCondition condition) {
		PersistentStorage store = PersistenceGateway.getStorage();
		store.UpdateOnDB(new UpdateQuery(getStoredObject(), condition));
		PersistenceGateway.checkinStorage(store);
	}

	/**
	 * performs a delete in the persistence layer
	 * 
	 * @param condition this method should be called with a query condition ensuring
	 *                  that the correct rows are deleted
	 */
	public void delete(QueryCondition condition) {
		PersistentStorage store = PersistenceGateway.getStorage();
		store.DeleteOnDB(new DeleteQuery(getStoredObject().getStoredTableSchema(), condition));
		PersistenceGateway.checkinStorage(store);
	}

	/**
	 * a list of elements in the object for logging purposes
	 * 
	 * @return a list of the fields and the properties present
	 */
	public String dropPayloadObjectList() {
		String schematext = "null";
		if (schema != null)
			schematext = schema.getName();
		String fieldtext = "null";
		if (this.fields != null)
			fieldtext = this.fields.dropNameList();
		String propertytext = "null";
		if (this.properties != null)
			propertytext = this.properties.dropNameList();
		return "[" + schematext + ";FLD:" + fieldtext + ";PRT:" + propertytext + "]";
	}
}
