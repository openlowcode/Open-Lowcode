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
 * A simple query condition of the form 'FIELD <OPERATOR> VALUE' (operator being greater than, equal, lower than...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleQueryCondition<E extends Object> extends QueryCondition {
	private TableAlias alias;
	private StoredFieldSchema<E> field;
	private QueryOperator<E> operator;
	private E payload;

	/**
	 * @param alias    tableschema alias
	 * @param field    the field in the query condition
	 * @param operator operator (typically '=','LIKE', '>'
	 * @param payload  payload to compare the content to
	 */
	public SimpleQueryCondition(TableAlias alias, StoredFieldSchema<E> field, QueryOperator<E> operator, E payload) {
		this.alias = alias;
		if (field == null)
			throw new RuntimeException("Field for query condition is null");
		this.field = field;
		if (operator == null)
			throw new RuntimeException("Operator for query condition is null for field = " + field.getName());
		this.operator = operator;
		if (payload == null)
			if (!operator.supportsNullPayload())
				throw new RuntimeException(
						"Payload for query condition is null for operator " + operator + "field = " + field.getName());

		this.payload = payload;
	}

	/**
	 * gets the alias
	 * 
	 * @return alias
	 */
	public TableAlias getAlias() {
		return alias;
	}

	/**
	 * gets the field schema
	 * 
	 * @return the field schema
	 */
	public StoredFieldSchema<E> getField() {
		return field;
	}

	/**
	 * gets the operator
	 * 
	 * @return operator
	 */
	public QueryOperator<E> getOperator() {
		return operator;
	}

	/**
	 * returns the payload
	 * 
	 * @return payload
	 */
	public E getPayload() {
		return payload;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);

	}

	@Override
	public boolean isSignificant(int circuitbreaker) {
		return true;
	}

	@Override
	public String toString() {
		return "[SIMPLEQUERYCONDITION:TABLE " + (alias != null ? alias.getTable() : "NOALIAS") + "/"
				+ (alias != null ? alias.getName() : "NOALIAS") + ", FIELD " + field.getName() + operator.toString()
				+ " " + (payload != null ? payload.getClass().getName() : "") + ":" + payload;
	}

}
