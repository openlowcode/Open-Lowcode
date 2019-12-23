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
import java.util.HashMap;

import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Query helper to search for objects with a uniqueidentified property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UniqueidentifiedQueryHelper {
	private static UniqueidentifiedQueryHelper singleton = new UniqueidentifiedQueryHelper();
	private static final int BATCH_QUERY_SIZE = 20;
	private static final String BLANK_ID = "NEVERLAND";

	/**
	 * @return the singleton query helper
	 */
	public static UniqueidentifiedQueryHelper get() {
		return singleton;
	}

	/**
	 * Generates a query condition to filter on object id
	 * 
	 * @param alias            alias of the object
	 * @param idvalue          value of the id
	 * @param parentdefinition definition of the data object
	 * @return the requested query condition
	 */
	public static <E extends DataObject<E>> QueryCondition getIdQueryCondition(TableAlias alias, String idvalue,
			DataObjectDefinition<E> parentdefinition) {
		UniqueidentifiedDefinition<E> definition = new UniqueidentifiedDefinition<E>(parentdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> id = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("ID");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, id, new QueryOperatorEqual<String>(), idvalue);
		return new SimpleQueryCondition<String>(alias, id, new QueryOperatorEqual<String>(), idvalue);
	}

	/**
	 * Reads one element on the provided id
	 * 
	 * @param id                 unique id of the object
	 * @param definition         definition of the object type
	 * @param propertydefinition definition of the unique identified property
	 * @return the object if found, null if nothing found
	 */
	public <E extends DataObject<E>> E readone(DataObjectId<E> id, DataObjectDefinition<E> definition,
			UniqueidentifiedDefinition<E> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				"SINGLEOBJECT");
		QueryCondition uniqueidcondition = UniqueidentifiedQueryHelper.getIdQueryCondition(alias, id.getId(),
				definition);
		QueryCondition finalcondition = uniqueidcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
		}

		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		boolean hasline = answer.next();
		if (hasline) {
			E formattedanswer = definition.generateFromRow(answer, alias);
			if (answer.next()) {
				String firstobject = formattedanswer.dropToString();
				E secondillegalanswer = definition.generateFromRow(answer, alias);
				String secondillegalanswerdrop = secondillegalanswer.dropToString();
				answer.close();
				throw new RuntimeException(
						"Expected one row, got more, for object type = " + definition.getName() + ", id = " + id.getId()
								+ " normal object " + firstobject + ", additional object " + secondillegalanswerdrop);
			}

			return formattedanswer;
		} else {
			return null;
		}
	}

	/**
	 * gets a list of objects, some of them may not be existing
	 * 
	 * @param id                 a list of object ids
	 * @param definition         definition of the data object
	 * @param propertydefinition definition of the unique identified property for
	 *                           the object
	 * @return the list of objects
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] readseveralpotentialexisting(
			DataObjectId<E>[] id, DataObjectDefinition<E> definition,
			UniqueidentifiedDefinition<E> propertydefinition) {
		return readseveral(id, definition, propertydefinition, false);
	}

	/**
	 * gets a list of objects
	 * 
	 * @param id                 list of the object ids
	 * @param definition         definition of the data boject
	 * @param propertydefinition definition of the unique identified property for
	 *                           the object
	 * @param blowifabsent       if an unknown object is in the list, generates an
	 *                           exception if true
	 * @return an array of objects corresponding to the provided ids in the order of
	 *         the ids provided
	 */
	private <E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] readseveral(DataObjectId<E>[] id,
			DataObjectDefinition<E> definition, UniqueidentifiedDefinition<E> propertydefinition,
			boolean blowifabsent) {
		ArrayList<E> results = new ArrayList<E>();
		HashMap<String, E> resultsbyid = new HashMap<String, E>();
		// work by batches to ensure query is not too long
		for (int i = 0; i < (id.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = definition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
					"SINGLEOBJECT");
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;

			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < id.length) {
					thisuniqueidcondition = UniqueidentifiedQueryHelper.getIdQueryCondition(alias, id[j].getId(),
							definition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = UniqueidentifiedQueryHelper.getIdQueryCondition(alias, BLANK_ID,
							definition);
				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}

			QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			while (answer.next()) {
				E formattedanswer = definition.generateFromRow(answer, alias);
				// put all results in a hasmap;
				resultsbyid.put(formattedanswer.getId().getId(), formattedanswer);
			}
		}
		for (int i = 0; i < id.length; i++) {
			DataObjectId<E> thisid = id[i];
			E object = resultsbyid.get(thisid.getId());
			if (object == null)
				if (blowifabsent)
					throw new RuntimeException("No object found for id = " + thisid);
			results.add(object);
		}
		return results.toArray(definition.generateArrayTemplate());
	}

	/**
	 * gets a list of objects, generates an exception if one of them is not existing
	 * 
	 * @param id                 a list of object ids
	 * @param definition         definition of the data object
	 * @param propertydefinition definition of the unique identified property for
	 *                           the object
	 * @return the list of objects
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] readseveral(DataObjectId<E>[] id,
			DataObjectDefinition<E> definition, UniqueidentifiedDefinition<E> propertydefinition) {
		return readseveral(id, definition, propertydefinition, true);
	}

}
