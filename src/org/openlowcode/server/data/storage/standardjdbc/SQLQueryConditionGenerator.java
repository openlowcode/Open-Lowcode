/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.standardjdbc;

import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.JoinQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryConditionAlways;
import org.openlowcode.server.data.storage.QueryConditionNever;
import org.openlowcode.server.data.storage.QueryOperator;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.QueryOperatorGreaterOrEqualTo;
import org.openlowcode.server.data.storage.QueryOperatorGreaterThan;
import org.openlowcode.server.data.storage.QueryOperatorLike;
import org.openlowcode.server.data.storage.QueryOperatorSmallerOrEqualTo;
import org.openlowcode.server.data.storage.QueryOperatorSmallerThan;
import org.openlowcode.server.data.storage.SimpleQueryCondition;

/**
 * A visitor for query condition generating the query string
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class SQLQueryConditionGenerator implements QueryCondition.Visitor {
	private StringBuffer querybuffer;

	/**
	 * creates a single use SQLQueryConditionGenerator with given querybuffer
	 * 
	 * @param querybuffer
	 */
	public SQLQueryConditionGenerator(StringBuffer querybuffer) {
		this.querybuffer = querybuffer;
	}

	@Override
	public void visit(QueryConditionNever never) {
		querybuffer.append(" 0 = 1 ");
	}

	@Override
	public void visit(QueryConditionAlways always) {
		querybuffer.append(" 1 = 1 ");
	}

	@Override
	public <E extends Object> void visit(SimpleQueryCondition<E> simplequerycondition) {
		querybuffer.append(' ');
		if (simplequerycondition.getAlias() != null) {
			querybuffer.append(simplequerycondition.getAlias().getName());
			querybuffer.append('.');
		}
		querybuffer.append(simplequerycondition.getField().getName());
		querybuffer.append(' ');
		boolean nullvalue = false;
		if (simplequerycondition.getPayload() == null)
			nullvalue = true;
		querybuffer.append(setSQLOperator(simplequerycondition.getOperator(), nullvalue));
		if (!nullvalue)
			querybuffer.append(" ? ");

	}

	/**
	 * generates a SQLOperator
	 * 
	 * @param operator  the operator
	 * @param nullvalue true if null
	 * @return
	 */
	public String setSQLOperator(@SuppressWarnings("rawtypes") QueryOperator operator, boolean nullvalue) {
		if (operator instanceof QueryOperatorEqual) {
			if (!nullvalue)
				return " = ";
			if (nullvalue)
				return " IS NULL ";
		}

		if (operator instanceof QueryOperatorLike) {
			if (!nullvalue)
				return " LIKE ";
			if (nullvalue)
				return " IS NULL ";
		}

		if (operator instanceof QueryOperatorGreaterThan) {
			return " > ";

		}
		if (operator instanceof QueryOperatorSmallerThan) {
			return " < ";
		}

		if (operator instanceof QueryOperatorGreaterOrEqualTo) {
			return " >= ";

		}
		if (operator instanceof QueryOperatorSmallerOrEqualTo) {
			return " <= ";
		}

		return "#OPERATORNOTSUPPORTED#";
	}

	@Override
	public <E> void visit(JoinQueryCondition<E> joinquerycondition) {
		querybuffer.append(" " + joinquerycondition.getMaintable().getName());
		querybuffer.append("." + joinquerycondition.getMaintablefield().getName());
		querybuffer.append(" " + setSQLOperator(joinquerycondition.getJoinqueryoperator(), false) + " ");
		querybuffer.append(" " + joinquerycondition.getSidetable().getName());
		querybuffer.append("." + joinquerycondition.getSidetablefield().getName());

	}

	@Override
	public void visit(AndQueryCondition andquerycondition) {
		QueryCondition[] andconditions = andquerycondition.returnAllConditions();
		querybuffer.append(" ( ");
		boolean first = true;
		for (int i = 0; i < andconditions.length; i++) {

			if (andconditions[i] != null) {
				if (!first)
					querybuffer.append(" AND ");
				andconditions[i].accept(this);
				first = false;
			}
		}
		querybuffer.append(" ) ");

	}

	@Override
	public void visit(OrQueryCondition orQueryCondition) {
		QueryCondition[] orconditions = orQueryCondition.returnAllConditions();
		querybuffer.append(" ( ");
		boolean first = true;
		for (int i = 0; i < orconditions.length; i++) {

			if (orconditions[i] != null) {
				if (!first)
					querybuffer.append(" OR ");
				orconditions[i].accept(this);
				first = false;
			}
		}
		querybuffer.append(" ) ");

	}
}
