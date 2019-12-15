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
 * A table in a query with an alias
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
@SuppressWarnings("rawtypes")
public class TableAlias extends Named {

	public class FieldSelectionAlias<E extends Object> {

		private StoredFieldSchema<E> field;
		private String alias;

		public StoredFieldSchema<E> getField() {
			return field;
		}

		public String getAlias() {
			return alias;
		}

		public FieldSelectionAlias(StoredFieldSchema<E> field, String alias) {
			this.field = field;
			this.alias = alias;
		}
	}

	private StoredTableSchema table;
	private boolean queryallfields;

	private ArrayList<FieldSelectionAlias> fieldselection;

	/**
	 * Creates a table alias that will query all fields for the table
	 * 
	 * @param table the table schema
	 * @param alias the alias inside the query
	 */
	public TableAlias(StoredTableSchema table, String alias) {
		super(alias);
		this.table = table;
		this.queryallfields = true;
		this.fieldselection = new ArrayList<FieldSelectionAlias>();
	}

	/**
	 * gets the table in this alias
	 * 
	 * @return the table
	 */
	public StoredTableSchema getTable() {
		return table;
	}

	/**
	 * specifies the field to display in the query for the alias. Byt default, all
	 * fields are specified. However, if using this method, only fields explicitely
	 * specified will be extracted from the database.
	 * 
	 * @param selectedfield the field to add in the query
	 * @param alias         aliasfor the field
	 */
	public <E extends Object> void addFieldSelection(StoredFieldSchema<E> selectedfield, String alias) {
		this.queryallfields = false;
		fieldselection.add(new FieldSelectionAlias<E>(selectedfield, alias));
	}

	/**
	 * tells if this table alias will query all fields
	 * 
	 * @return true if all fields are queried, false if only some fields are queried
	 */
	public boolean queryAllFields() {
		return this.queryallfields;
	}

	/**
	 * gives all field selection aliases
	 * 
	 * @return the field selection aliases if the alias does not query all fields
	 */
	public FieldSelectionAlias[] getFieldSelection() {
		return fieldselection.toArray(new FieldSelectionAlias[0]);
	}

	@Override
	public String toString() {
		String returnstring = "TableAlias[table=" + table.getName() + ",queryallfields=" + queryallfields
				+ ",fieldselection = [";
		for (int i = 0; i < fieldselection.size(); i++)
			returnstring += fieldselection.get(i).alias + ",";
		returnstring += "]]";
		return returnstring;
	}
}
