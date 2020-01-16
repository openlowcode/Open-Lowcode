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
 * query helper for objects having the iterated auto-link property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedautolinkQueryHelper {
	private static IteratedautolinkQueryHelper singleton = new IteratedautolinkQueryHelper();

	/**
	 * @return the singleton iterated autolink query helper
	 */
	public static IteratedautolinkQueryHelper get() {
		return singleton;
	}

	/**
	 * get all links from the provided iteration of the left object
	 * 
	 * @param leftid                 left object id
	 * @param iteration              iteration
	 * @param additionalcondition    additional query condition, can be null
	 * @param parentobjectdefinition definition of the object holding the auto-link
	 * @param linkedobjectdefinition definition of the object referenced by the
	 *                               auto-link
	 * @param iteratedlinkdefinition definition of the iterated autolink property
	 * @return
	 */
	public <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> E[] getalllinksfromleftiteration(
					DataObjectId<F> leftid,
					Integer iteration,
					QueryFilter additionalcondition,
					DataObjectDefinition<E> parentobjectdefinition,
					DataObjectDefinition<F> linkedobjectdefinition,
					IteratedautolinkDefinition<E, F> iteratedlinkdefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				AutolinkobjectQueryHelper.get().getLeftidQueryCondition(alias, leftid, parentobjectdefinition,
						linkedobjectdefinition));
		QueryCondition linkuniversalcondition = parentobjectdefinition
				.getUniversalQueryCondition(iteratedlinkdefinition, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);
		extendedcondition = new AndQueryCondition(extendedcondition,
				getIterationQueryCondition(alias, iteration, parentobjectdefinition, iteratedlinkdefinition));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}

		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * generates the iteration query condition for filtering autolinks that are
	 * iterated
	 * 
	 * @param alias                  parent table alias
	 * @param iteration              iteration number
	 * @param parentdefinition       definition of the object holding the auto-link
	 * @param iteratedlinkdefinition definition of the iterated auto-link property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> QueryCondition getIterationQueryCondition(
					TableAlias alias,
					Integer iteration,
					DataObjectDefinition<E> parentdefinition,
					IteratedautolinkDefinition<E, F> iteratedlinkdefinition) {

		StoredFieldSchema<Integer> leftfromiteration = (StoredFieldSchema<Integer>) iteratedlinkdefinition
				.getDefinition().lookupOnName("LFFIRSTITER");
		StoredFieldSchema<Integer> lefttoiteration = (StoredFieldSchema<Integer>) iteratedlinkdefinition.getDefinition()
				.lookupOnName("LFLASTITER");
		SimpleQueryCondition<Integer> fromiterationcondition = new SimpleQueryCondition<Integer>(alias,
				leftfromiteration, new QueryOperatorSmallerOrEqualTo<Integer>(), iteration);
		SimpleQueryCondition<Integer> toiterationcondition = new SimpleQueryCondition<Integer>(alias, lefttoiteration,
				new QueryOperatorGreaterOrEqualTo<Integer>(), iteration);

		return new AndQueryCondition(fromiterationcondition, toiterationcondition);
	}

}
