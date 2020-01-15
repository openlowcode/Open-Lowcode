/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
 * Query Helper for objects with an iterated link property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedlinkQueryHelper {
	private static IteratedlinkQueryHelper singleton = new IteratedlinkQueryHelper();

	/**
	 * get the singleton helper
	 * 
	 * @return singleton helper
	 */
	public static IteratedlinkQueryHelper get() {
		return singleton;
	}

	/**
	 * generates iteration query condition on the link (will only return links for
	 * the given iteration of the left object)
	 * 
	 * @param alias                  alias of the link table
	 * @param iteration              iteration to consider
	 * @param parentdefinition       definition of the data object of the link
	 * @param iteratedlinkdefinition definition of the iterated link property
	 * @return
	 */
	public <
			E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>,
			F extends DataObject<F> & IteratedInterface<F>,
			G extends DataObject<G> & UniqueidentifiedInterface<G>> QueryCondition getIterationQueryCondition(
					TableAlias alias,
					Integer iteration,
					DataObjectDefinition<E> parentdefinition,
					IteratedlinkDefinition<E, F, G> iteratedlinkdefinition) {
		@SuppressWarnings("unchecked")
		StoredFieldSchema<Integer> leftfromiteration = (StoredFieldSchema<Integer>) iteratedlinkdefinition
				.getDefinition().lookupOnName("LFFIRSTITER");
		@SuppressWarnings("unchecked")
		StoredFieldSchema<Integer> lefttoiteration = (StoredFieldSchema<Integer>) iteratedlinkdefinition.getDefinition()
				.lookupOnName("LFLASTITER");

		SimpleQueryCondition<Integer> fromiterationcondition = new SimpleQueryCondition<Integer>(alias,
				leftfromiteration, new QueryOperatorSmallerOrEqualTo<Integer>(), iteration);
		SimpleQueryCondition<Integer> toiterationcondition = new SimpleQueryCondition<Integer>(alias, lefttoiteration,
				new QueryOperatorGreaterOrEqualTo<Integer>(), iteration);

		return new AndQueryCondition(fromiterationcondition, toiterationcondition);
	}

	/**
	 * get all links from left iteration for the given left object
	 * 
	 * @param leftid                 left object id
	 * @param leftiteration          left iteraion
	 * @param additionalcondition    additional condition for the query
	 * @param parentobjectdefinition objet definition of the link
	 * @param leftobjectdefinition   object definition of the left object for the
	 *                               link
	 * @param rightobjectdefinition  object definition of the right object for the
	 *                               link
	 * @param iteratedlinkdefinition definition of the iterated link property
	 * @return
	 */
	public <
			E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>,
			F extends DataObject<F> & IteratedInterface<F>,
			G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromleftiteration(
					DataObjectId<F> leftid,
					Integer leftiteration,
					QueryFilter additionalcondition,
					DataObjectDefinition<E> parentobjectdefinition,
					DataObjectDefinition<F> leftobjectdefinition,
					DataObjectDefinition<G> rightobjectdefinition,
					IteratedlinkDefinition<E, F, G> iteratedlinkdefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				LinkobjectQueryHelper.get().getLeftidQueryCondition(alias, leftid, parentobjectdefinition,
						leftobjectdefinition, rightobjectdefinition));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
		QueryCondition linkuniversalcondition = parentobjectdefinition
				.getUniversalQueryCondition(iteratedlinkdefinition, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);
		extendedcondition = new AndQueryCondition(extendedcondition,
				getIterationQueryCondition(alias, leftiteration, parentobjectdefinition, iteratedlinkdefinition));
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}

		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}
}
