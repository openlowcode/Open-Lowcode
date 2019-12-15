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

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A flat data table in the storage component of the application
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class StoredTableSchema extends Named {

	private NamedList<StoredFieldSchema> fieldlist;
	/**
	 * @param name a unique name of an object for the application
	 */
	private NamedList<StoredTableIndex> indexlist;

	/**
	 * creates a blank table schema
	 * @param name name of the table schema
	 */
	public StoredTableSchema(String name) {
		super(name);
		fieldlist = new NamedList<StoredFieldSchema>();
		indexlist = new NamedList<StoredTableIndex>();
	}

	/**
	 * @return the number of indexes
	 */
	public int getIndexSize() {
		return indexlist.getSize();
	}

	/**
	 * gets the index at specified index
	 * @param i the index between 0 (included) and getIndexSize() (excluded)
	 * @return definition of the index
	 */
	public StoredTableIndex getIndex(int i) {
		return indexlist.get(i);
	}

	/**
	 * adds a new stored field in the table shcema
	 * @param field a stored field schema
	 */
	public void addField(StoredFieldSchema field) {
		this.fieldlist.add(field);
	}

	/**
	 * gets the number of stored fields in this table schema
	 * @return the number of stored fields
	 */
	public int getStoredFieldNumber() {
		return fieldlist.getSize();
	}

	/**
	 * gets the field schema at the specified index
	 * @param index a number between 0 (included) and getStoredFieldNumber() (excluded)
	 * @return the stored field schema at the given index
	 */
	public StoredFieldSchema getStoredField(int index) {
		return fieldlist.get(index);
	}

	/**
	 * looks-up a field schema by name
	 * @param name name of the field
	 * @return the field schema
	 */
	public StoredFieldSchema lookupFieldByName(String name) {
		return fieldlist.lookupOnName(name);
	}


	/**
	 * adds a new index to this table schema
	 * @param index the index
	 */
	public void addIndex(StoredTableIndex index) {
		index.setParent(this);
		if (indexlist.lookupOnName(index.getName()) != null)
			throw new RuntimeException("Table schema " + this.getName() + " duplicate index suffix " + index.getName());
		indexlist.add(index);
		if (index.getAllFields() == null)
			throw new RuntimeException(
					"Table schema " + this.getName() + " suffix " + index.getName() + " field list is null");
		if (index.getAllFields().length == 0)
			throw new RuntimeException(
					"Table schema " + this.getName() + " suffix " + index.getName() + " field list has zero element");

		for (int i = 0; i < index.getAllFields().length; i++) {
			StoredFieldSchema thisfield = index.getAllFields()[i];
			if (thisfield == null)
				throw new RuntimeException("For index " + index.getName() + ", Field index = " + i + " is null");
			if (fieldlist.lookupOnName(thisfield.getName()) == null)
				throw new RuntimeException("for table " + this.getName() + " for index suffix " + index.getName()
						+ " reference to non existing field " + thisfield.getName());

		}

	}

	@Override
	public String toString() {

		return this.getName();
	}

}
