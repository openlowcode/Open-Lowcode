/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.util.ArrayList;

/**
 * a query to update only some fields on a table for all rows corresponding to
 * the query condition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.14
 */
public class LimitedFieldsUpdateQuery {

	private StoredTableSchema schema;
	private QueryCondition condition;
	private ArrayList<SimpleEqualQueryCondition<?>> specificupdatedfields;
	
	/**
	 * @param schema    table to perform the delete statement
	 * @param condition condition to select just the rows in the database
	 */
	public LimitedFieldsUpdateQuery(StoredTableSchema schema, QueryCondition condition) {
		this.schema = schema;
		this.condition = condition;
		this.specificupdatedfields = new ArrayList<SimpleEqualQueryCondition<?>>();
	}
	
	/**
	 * @return the table schema to perform the limited fields update statement on
	 */
	public StoredTableSchema getTableSchema() {
		return this.schema;
	}

	/**
	 * @return the condition to perform the limited fields update statement on
	 */
	public QueryCondition getCondition() {
		return condition;
	}
	
	/**
	 * add a specific field and payload to update
	 * 
	 * @param specificupdatedfield a condition specifying the field to update and the payload
	 */
	public void addFieldUpdate(SimpleEqualQueryCondition<?> specificupdatedfield) {
		this.specificupdatedfields.add(specificupdatedfield);
	}
	
	/**
	 * @return the number of specific field updates
	 */
	public int getUpdatedFieldsNumber() {
		return this.specificupdatedfields.size();
	}
	
	/**
	 * @param index a number between 0 (included) and getUpdatedFieldsNumber (excluded)
	 * @return the update field condition
	 */
	public SimpleEqualQueryCondition<?> getFieldUpdateAt(int index) {
		return this.specificupdatedfields.get(index);
	}
}
