/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.specificstorage;

import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.QueryOperator;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.storage.JoinQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * the definition of a join query condition that can be used in several contexts
 * (generates a join query condition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of object on which the field is the payload
 */
public class JoinQueryConditionDefinition<E extends Object> {
	private StoredTableSchema maintable;
	private StoredFieldSchema<E> maintablefield;
	private StoredTableSchema sidetable;
	private StoredFieldSchema<E> sidetablefield;
	private String sidetablesuffix;
	private QueryOperator<E> joinqueryoperator;
	private DataObjectDefinition<?> sidetableobjectdefinition;

	/**
	 * creates a join query condition definition
	 * 
	 * @param maintable         main table
	 * @param maintablefield    main table field for the join query
	 * @param sidetable         side table
	 * @param sidetablefield    side table field for the join query
	 * @param joinqueryoperator Operator (typically equal)
	 */
	public JoinQueryConditionDefinition(StoredTableSchema maintable, StoredFieldSchema<E> maintablefield,
			StoredTableSchema sidetable, String sidetablesuffix, StoredFieldSchema<E> sidetablefield,
			DataObjectDefinition<?> sidetableobjectdefinition, QueryOperator<E> joinqueryoperator) {
		super();
		this.maintable = maintable;
		this.maintablefield = maintablefield;
		this.sidetable = sidetable;
		this.sidetablefield = sidetablefield;
		this.sidetablesuffix = sidetablesuffix;
		this.sidetableobjectdefinition = sidetableobjectdefinition;
		this.joinqueryoperator = joinqueryoperator;

	}

	/**
	 * generates the query condition for the specified main table alias
	 * 
	 * @param maintablealias main table alias
	 * @return the join query condition
	 */
	public JoinQueryCondition<E> generateJoinQueryCondition(String maintablealias) {
		return new JoinQueryCondition<E>(new TableAlias(maintable, maintablealias), maintablefield,
				new TableAlias(sidetable, maintablealias + sidetablesuffix), sidetablefield, joinqueryoperator);
	}

	/**
	 * generates the universal query condition on the side object. This will ensure
	 * the join to the side object does not impact the query on the main object
	 * (e.g. only take the latest iteration of the side object)
	 * 
	 * @param maintablealias main table alias
	 * @return the universal query condition for the side object
	 */
	public QueryCondition generateSideTableUniversalQueryCondition(String maintablealias) {
		return sidetableobjectdefinition.getUniversalQueryCondition(null, maintablealias + sidetablesuffix);
	}

}
