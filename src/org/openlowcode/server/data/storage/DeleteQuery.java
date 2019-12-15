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

/**
 * a query to delete one or several rows of data
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DeleteQuery {
	private StoredTableSchema schema;

	private QueryCondition condition;

	/**
	 * @param schema    table to perform the delete statement
	 * @param condition condition to select just the rows in the database
	 */
	public DeleteQuery(StoredTableSchema schema, QueryCondition condition) {
		this.schema = schema;
		this.condition = condition;
	}

	/**
	 * @return the table schema to perform the delete statement on
	 */
	public StoredTableSchema getTableSchema() {
		return this.schema;
	}

	/**
	 * @return the condition to perform delete statement on
	 */
	public QueryCondition getCondition() {
		return condition;
	}

}
