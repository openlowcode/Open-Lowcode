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
 * A query condition for select, updates, and deletes
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class QueryCondition {
	public static final int MAX_CIRCUIT_BREAKER = 200;

	/**
	 * Visitor for query condition
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public interface Visitor {
		/**
		 * @param simplequerycondition
		 */
		public <E extends Object> void visit(SimpleQueryCondition<E> simplequerycondition);

		/**
		 * @param joinquerycondition
		 */
		public <E extends Object> void visit(JoinQueryCondition<E> joinquerycondition);

		/**
		 * @param andquerycondition
		 */
		public void visit(AndQueryCondition andquerycondition);

		/**
		 * @param orQueryCondition
		 */
		public void visit(OrQueryCondition orQueryCondition);

		/**
		 * @param always
		 */
		public void visit(QueryConditionAlways always);

		/**
		 * @param never
		 */
		public void visit(QueryConditionNever never);

	}

	/**
	 * @param visitor visitor for query condition
	 */
	public abstract void accept(QueryCondition.Visitor visitor);

	/**
	 * @param circuitbreaker
	 * @return true if condition if significant (e.g. an 'AND' query condition with zero elements is not significant
	 */
	public abstract boolean isSignificant(int circuitbreaker);
}
