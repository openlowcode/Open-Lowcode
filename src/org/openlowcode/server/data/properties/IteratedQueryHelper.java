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
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * query helper for objects being iterated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedQueryHelper {
	private static IteratedQueryHelper singleton = new IteratedQueryHelper();
	public static String maintablealiasforgetallactive = "U0";

	/**
	 * @return the singleton query helper
	 */
	public static IteratedQueryHelper get() {
		return singleton;
	}

	/**
	 * get a query condition for iteration on the object
	 * 
	 * @param alias            table alias
	 * @param iteration        iteration
	 * @param parentdefinition definition of the iterated data object
	 * @return the query condition
	 */
	public <E extends DataObject<E> & IteratedInterface<E>> QueryCondition getIterationQueryCondition(
			TableAlias alias,
			long iteration,
			DataObjectDefinition<E> parentdefinition) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		IteratedDefinition<E> definition = new IteratedDefinition(parentdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<Integer> iterationfield = (StoredFieldSchema<Integer>) definition.getDefinition()
				.lookupOnName("ITERATION");
		SimpleQueryCondition<Integer> iterationcondition = new SimpleQueryCondition<Integer>(alias, iterationfield,
				new QueryOperatorEqual<Integer>(), new Integer((int) iteration));
		return iterationcondition;
	}

	/**
	 * gets all the object iterations
	 * 
	 * @param id                 id of the data object
	 * @param definition         definition of the data object
	 * @param iteratedDefinition definition of the iterated property
	 * @return an array of objects gathering all iterations for a data object
	 */
	public <E extends DataObject<E> & IteratedInterface<E>> E[] getallobjectiterationsbyobjectid(
			DataObjectId<E> id,
			DataObjectDefinition<E> definition,
			IteratedDefinition<E> iteratedDefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(iteratedDefinition,
				"SINGLEOBJECT");
		QueryCondition uniqueidcondition = UniqueidentifiedQueryHelper.getIdQueryCondition(alias, id.getId(),
				definition);
		QueryCondition finalcondition = uniqueidcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
		}

		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(definition.generateFromRow(row, alias));
		}
		return returnlist.toArray(definition.generateArrayTemplate());
	}

	/**
	 * reads a precise iteration of the data object
	 * 
	 * @param id                 id of the data object
	 * @param iteration          iteration of the data object
	 * @param definition         definition of the data object
	 * @param iteratedDefinition iterated property on the data object
	 * @return
	 */
	public <E extends DataObject<E> & IteratedInterface<E>> E readiteration(
			DataObjectId<E> id,
			long iteration,
			DataObjectDefinition<E> definition,
			IteratedDefinition<E> iteratedDefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(iteratedDefinition,
				"SINGLEOBJECT");
		QueryCondition uniqueidcondition = UniqueidentifiedQueryHelper.getIdQueryCondition(alias, id.getId(),
				definition);
		QueryCondition finalcondition = uniqueidcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
		}

		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		extendedcondition = new AndQueryCondition(extendedcondition,
				getIterationQueryCondition(alias, iteration, definition));
		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		boolean hasline = answer.next();
		if (hasline) {
			E formattedanswer = definition.generateFromRow(answer, alias);
			if (answer.next()) {
				String firstobject = formattedanswer.dropToString();
				E secondillegalanswer = definition.generateFromRow(answer, alias);
				String secondillegalanswerdrop = secondillegalanswer.dropToString();
				throw new RuntimeException(
						"Expected one row, got more, for object type = " + definition.getName() + ", id = " + id.getId()
								+ " normal object " + firstobject + ", additional object " + secondillegalanswerdrop);
			}
			return formattedanswer;
		} else {
			return null;
		}

	}

}
