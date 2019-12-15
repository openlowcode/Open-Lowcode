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
 * A query to update a single row
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UpdateQuery {
	private StoredTableRow row;

	private QueryCondition condition;

	/**
	 * creates an update query
	 * 
	 * @param row       row specifying the data to update
	 * @param condition condition to query the row to update
	 */
	public UpdateQuery(StoredTableRow row, QueryCondition condition) {
		this.row = row;
		this.condition = condition;
	}

	/**
	 * @return gets the row with stored data
	 */
	public StoredTableRow getRow() {
		return this.row;
	}

	/**
	 * @return gets the condition
	 */
	public QueryCondition getCondition() {
		return condition;
	}

}
