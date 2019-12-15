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

import org.openlowcode.tools.misc.NamedList;

/**
 * A selection query to get several rows
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SelectQuery {

	private NamedList<TableAlias> tables;
	private QueryCondition qd;

	/**
	 * @param tables
	 * @param qd     query condition on objects in the table or join
	 */
	public SelectQuery(NamedList<TableAlias> tables, QueryCondition qd) {
		this.tables = tables;
		this.qd = qd;
	}

	/**
	 * returns the number of table aliases in the query
	 * @return an integer with the number of tble aliases
	 */
	public int getTableNumber() {
		return tables.getSize();
	}

	/**
	 * @param index a number between 0 (included) and getTableNumber (excluded)
	 * @return the table alias at the given index
	 */
	public TableAlias getTable(int index) {
		return tables.get(index);
	}

	/**
	 * query condition 
	 * @return the query condition of the select query
	 */
	public QueryCondition getQueryCondition() {
		return qd;
	}
}
