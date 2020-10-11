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

import java.util.ArrayList;

import org.openlowcode.tools.misc.Named;

/**
 * An index on the table
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings("rawtypes")
public class StoredTableIndex extends Named {

	private ArrayList<StoredFieldSchema> indexfields;
	private StoredTableSchema parent;

	/**
	 * gets the parent table schema
	 * 
	 * @return the parent table schema
	 */
	public StoredTableSchema getParent() {
		return parent;
	}

	/**
	 * get all fields of the index
	 * 
	 * @return all the fields of the index
	 */
	public StoredFieldSchema[] getAllFields() {
		return indexfields.toArray(new StoredFieldSchema[0]);
	}

	/**
	 * creates an index with a name (but not field specified yet)
	 * 
	 * @param name name of the index
	 */
	public StoredTableIndex(String name) {
		super(name);
		indexfields = new ArrayList<StoredFieldSchema>();

	}

	/**
	 * 
	 * @param parent specifies the tableschema
	 */
	public void setParent(StoredTableSchema parent) {
		this.parent = parent;
	}

	/**
	 * adds a new stored field in the index
	 * 
	 * @param indexfield the field to add at current position in the index
	 */
	public void addStoredFieldSchema(StoredFieldSchema indexfield) {
		indexfields.add(indexfield);
	}

	/**
	 * get the number of fields in this index
	 * 
	 * @return the number of fields
	 */
	public int getFieldNumber() {
		return indexfields.size();
	}

	/**
	 * gets a field in the index
	 * 
	 * @param i a field between 0 (included) and getFieldNumber() (excluded)
	 * @return the field at the specified index
	 */
	public StoredFieldSchema getField(int i) {
		return indexfields.get(i);
	}

	/**
	 * generates the name in the database
	 * 
	 * @return the name to use to store the index in the databse
	 */
	public String getFullName() {
		return parent.getName().toUpperCase() + "_" + this.getName().toUpperCase();
	}

}
