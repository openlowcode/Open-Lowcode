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

/**
 * A query filter is a combination of a query condition, and potentially aliases
 * used to extend the query
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class QueryFilter {
	/**
	 * Creates a query filter
	 * 
	 * @param querycondition conditions
	 * @param aliasarray     list of aliases
	 */
	public QueryFilter(QueryCondition querycondition, TableAlias[] aliasarray) {
		this.querycondition = querycondition;
		this.aliases = new ArrayList<TableAlias>();
		if (aliasarray != null)
			for (int i = 0; i < aliasarray.length; i++)
				aliases.add(aliasarray[i]);
	}

	/**
	 * convenience method to create a query filter without any aliases specified
	 * 
	 * @param querycondition
	 * @return the query filter
	 */
	public static QueryFilter get(QueryCondition querycondition) {
		return new QueryFilter(querycondition, null);
	}

	/**
	 * convenience method to create a query filter with a query condition and a
	 * single alias
	 * 
	 * @param querycondition query condition
	 * @param singlealias    single alias
	 * @return the query filter
	 */
	public static QueryFilter get(QueryCondition querycondition, TableAlias singlealias) {
		return new QueryFilter(querycondition, new TableAlias[] { singlealias });
	}

	/**
	 * adds a new alias
	 * 
	 * @param alias alias to add
	 */
	public void addAlias(TableAlias alias) {
		this.aliases.add(alias);
	}

	private QueryCondition querycondition;
	private ArrayList<TableAlias> aliases;

	/**
	 * @return the query condition in this alias
	 */
	public QueryCondition getCondition() {
		return this.querycondition;
	}

	/**
	 * get the list of aliases
	 * 
	 * @return aliases as an array
	 */
	public TableAlias[] getAliases() {
		return aliases.toArray(new TableAlias[0]);
	}

}
