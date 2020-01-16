/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorGreaterOrEqualTo;
import org.openlowcode.server.data.storage.QueryOperatorSmallerOrEqualTo;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Query helper for the iterated subobject property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedsubobjectQueryHelper {
	private static IteratedsubobjectQueryHelper singleton = new IteratedsubobjectQueryHelper();

	/**
	 * @return the singleton helper
	 */
	public static IteratedsubobjectQueryHelper get() {

		return singleton;
	}

	/**
	 * gets all children valid for the parent iteration
	 * 
	 * @param parentid                    parent data object id
	 * @param parentiteration             parent iteration number
	 * @param additionalcondition         additional condition
	 * @param parentobjectdefinition      definition of the current data object
	 *                                    (subobject)
	 * @param parentdefinition            definition of the parent data object for
	 *                                    the link
	 * @param iteratedsubobjectdefinition definition of the iterated subobject
	 *                                    property
	 * @return
	 */
	public <
			E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedsubobjectInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> E[] getallsubobjectsfromparentiteration(
					DataObjectId<F> parentid,
					Integer parentiteration,
					QueryFilter additionalcondition,
					DataObjectDefinition<E> parentobjectdefinition,
					DataObjectDefinition<F> parentdefinition,
					IteratedsubobjectDefinition<E, F> iteratedsubobjectdefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				IteratedsubobjectQueryHelper.get().getIterationQueryCondition(alias, parentiteration,
						parentobjectdefinition, iteratedsubobjectdefinition));
		QueryCondition linkuniversalcondition = parentobjectdefinition
				.getUniversalQueryCondition(iteratedsubobjectdefinition, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);
		extendedcondition = new AndQueryCondition(extendedcondition, getIterationQueryCondition(alias, parentiteration,
				parentobjectdefinition, iteratedsubobjectdefinition));
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}

		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * generates the query condition filtering on an iteration for the subobject
	 * 
	 * @param alias                       alias of the subobject table
	 * @param iteration                   iteration to query on
	 * @param parentdefinition            definition of the current object
	 *                                    (subobject)
	 * @param iteratedsubobjectdefinition definition of the iterated subobject
	 *                                    property
	 * @return
	 */
	public <
			E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedsubobjectInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> QueryCondition getIterationQueryCondition(
					TableAlias alias,
					Integer iteration,
					DataObjectDefinition<E> parentdefinition,
					IteratedsubobjectDefinition<E, F> iteratedsubobjectdefinition) {
		@SuppressWarnings("unchecked")
		StoredFieldSchema<Integer> parentfromiteration = (StoredFieldSchema<Integer>) iteratedsubobjectdefinition
				.getDefinition().lookupOnName("PRFIRSTITER");
		@SuppressWarnings("unchecked")
		StoredFieldSchema<Integer> parenttoiteration = (StoredFieldSchema<Integer>) iteratedsubobjectdefinition
				.getDefinition().lookupOnName("PRLASTITER");

		SimpleQueryCondition<Integer> fromiterationcondition = new SimpleQueryCondition<Integer>(alias,
				parentfromiteration, new QueryOperatorGreaterOrEqualTo<Integer>(), iteration);
		SimpleQueryCondition<Integer> toiterationcondition = new SimpleQueryCondition<Integer>(alias, parenttoiteration,
				new QueryOperatorSmallerOrEqualTo<Integer>(), iteration);

		return new AndQueryCondition(fromiterationcondition, toiterationcondition);
	}

}
