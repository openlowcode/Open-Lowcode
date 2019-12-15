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
import java.util.logging.Logger;

/**
 * Composition of several query conditions with an AND operator
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AndQueryCondition extends QueryCondition {
	private ArrayList<QueryCondition> conditions;
	private static Logger logger = Logger.getLogger(AndQueryCondition.class.getName());

	/**
	 * Creates an empty query condition. Members have to be added later
	 */
	public AndQueryCondition() {
		conditions = new ArrayList<QueryCondition>();
	}

	/**
	 * A convenience constructor to create an AND query condition with two members
	 * 
	 * @param firstcondition  first query condition
	 * @param secondcondition second query condition
	 */
	public AndQueryCondition(QueryCondition firstcondition, QueryCondition secondcondition) {
		this();

		addCondition(firstcondition);
		addCondition(secondcondition);
	}

	/**
	 * A convenience constructor to create an AND query condition with three members
	 * 
	 * @param firstcondition  first query condition
	 * @param secondcondition second query condition
	 * @param thirdcondition  third query condition
	 */
	public AndQueryCondition(QueryCondition firstcondition, QueryCondition secondcondition,
			QueryCondition thirdcondition) {
		this();
		addCondition(firstcondition);
		addCondition(secondcondition);
		addCondition(thirdcondition);
	}

	/**
	 * A convenience constructor to create an AND query condition with three members
	 * 
	 * @param firstcondition  first query condition
	 * @param secondcondition second query condition
	 * @param thirdcondition  third query condition
	 * @param fourthcondition third query condition
	 */
	public AndQueryCondition(QueryCondition firstcondition, QueryCondition secondcondition,
			QueryCondition thirdcondition, QueryCondition fourthcondition) {
		this();
		addCondition(firstcondition);
		addCondition(secondcondition);
		addCondition(thirdcondition);
		addCondition(fourthcondition);

	}

	/**
	 * adds a new condition to the AND query condition
	 * 
	 * @param condition condition to add
	 */
	public void addCondition(QueryCondition condition) {
		if (condition != null)
			if (condition.isSignificant(0)) {
				conditions.add(condition);
				return;
			}
		// sending a warning that null query condition is added
		// Note: still needs to check the performance penalty of this tracing
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		logger.fine("------------- Adding null condition to AND statement --------------------------");
		for (int i = 0; i < stacktrace.length; i++)
			logger.fine("     * " + stacktrace[i]);
		logger.fine("------------------------------------------------------------------------------");
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);

	}

	/**
	 * @return an array with all query conditions
	 */
	public QueryCondition[] returnAllConditions() {
		return conditions.toArray(new QueryCondition[0]);
	}

	@Override
	public boolean isSignificant(int circuitbreaker) {
		if (circuitbreaker > QueryCondition.MAX_CIRCUIT_BREAKER)
			throw new RuntimeException("Circuit Breaker");
		for (int i = 0; i < conditions.size(); i++) {
			if (conditions.get(i).isSignificant(circuitbreaker + 1))
				return true;
		}
		return false;
	}
}
