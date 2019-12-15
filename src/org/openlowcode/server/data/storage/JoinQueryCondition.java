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
 * A query condition specifying a join between two tables. In current version, only
 * inner join is managed
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the payload of the field on which the join is performed.
 */
public class JoinQueryCondition<E extends Object> extends QueryCondition {
	private TableAlias maintable;
	private StoredFieldSchema<E> maintablefield;
	private TableAlias sidetable;
	private StoredFieldSchema<E> sidetablefield;
	private QueryOperator<E> joinqueryoperator;
	
	
	
	/**
	 * @return alias for main table for the join query (all select queries have a main table)
	 */
	public TableAlias getMaintable() {
		return maintable;
	}
	/**
	 * @return the field used for the join on the main table
	 */
	public StoredFieldSchema<E> getMaintablefield() {
		return maintablefield;
	}
	/**
	 * @return the side table alias
	 */
	public TableAlias getSidetable() {
		return sidetable;
	}
	/**
	 * @return
	 */
	public StoredFieldSchema<E> getSidetablefield() {
		return sidetablefield;
	}
	/**
	 * @return the operator to compare the two fields for join. Typically, it is QueryOperatorEqual
	 */
	public QueryOperator<E> getJoinqueryoperator() {
		return joinqueryoperator;
	}
	/**
	 * creates a join query condition
	 * @param maintable main table alias for the join condition
	 * @param maintablefield field used on the main table for the join condition
	 * @param sidetable
	 * @param sidetablefield
	 * @param joinqueryoperator operator (typically equal) 
	 */
	public JoinQueryCondition(TableAlias maintable,StoredFieldSchema<E> maintablefield,
			TableAlias sidetable,StoredFieldSchema<E> sidetablefield,QueryOperator<E> joinqueryoperator) {
		this.maintable = maintable;
		this.maintablefield = maintablefield;
		this.sidetable = sidetable;
		this.sidetablefield = sidetablefield;
		this.joinqueryoperator = joinqueryoperator;
	}
	/**
	 * @return the alias of the side table
	 */
	public TableAlias getSideTableAlias() {
		return sidetable;
	}
	@Override
	public void accept(Visitor visitor)  {
		visitor.visit(this);

	}
	@Override
	public boolean isSignificant(int circuitbreaker)  {
		return true;
	}
}
