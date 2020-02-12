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
import java.util.HashMap;

import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Helper class to perform queries on the parent / child relation
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LinkedtoparentQueryHelper {
	private static HashMap<
			String, LinkedtoparentQueryHelper> helperlist = new HashMap<String, LinkedtoparentQueryHelper>();
	private static Logger logger = Logger.getLogger(LinkedtoparentQueryHelper.class.getCanonicalName());
	public final static String CHILD_OBJECT_ALIAS = "SINGLEOBJECT";
	private static final int BATCH_QUERY_SIZE = 20;
	private static final String BLANK_ID = "NEVERLAND";
	private String name;

	/**
	 * gets a linked to parent query helper for the given linked to parent name
	 * 
	 * @param name name
	 * @return the query helper
	 */
	public static LinkedtoparentQueryHelper get(String name) {
		LinkedtoparentQueryHelper answer = helperlist.get(name);
		if (answer != null)
			return answer;
		answer = new LinkedtoparentQueryHelper(name);
		helperlist.put(name, answer);
		return answer;
	}

	/**
	 * Creates a linked to parent query helper for the given name
	 * 
	 * @param name name of the property
	 */
	private LinkedtoparentQueryHelper(String name) {
		this.name = name;
	}

	/**
	 * gets the condition on the parent id
	 * 
	 * @param alias                  alias of the child object
	 * @param parentidvalue          id of the parent
	 * @param parentdefinition       definition of the child object
	 * @param linkedobjectdefinition definition of the parent object
	 * @return the specified query condition
	 */
	public <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & UniqueidentifiedInterface<F>> QueryCondition getParentIdQueryCondition(
					TableAlias alias,
					DataObjectId<F> parentidvalue,
					DataObjectDefinition<E> parentdefinition,
					DataObjectDefinition<F> linkedobjectdefinition) {

		LinkedtoparentDefinition<
				E, F> definition = new LinkedtoparentDefinition<E, F>(parentdefinition, name, linkedobjectdefinition);

		String fieldname = name.toUpperCase() + "ID";
		logger.info("generated field name " + fieldname + " for parentidcondition for parentdefiniton = "
				+ parentdefinition.getName() + " for linkedobjectdefiniion = " + linkedobjectdefinition.getName());
		@SuppressWarnings("unchecked")
		StoredFieldSchema<
				String> parentid = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName(fieldname);
		if (parentid == null)
			throw new RuntimeException("could not find field in definition with name = '" + fieldname
					+ "', available values = " + definition.getDefinition().dropNameList());
		if (alias == null)
			return new SimpleQueryCondition<String>(null, parentid, new QueryOperatorEqual<String>(),
					(parentidvalue != null ? parentidvalue.getId() : BLANK_ID));
		return new SimpleQueryCondition<String>(alias, parentid, new QueryOperatorEqual<String>(),
				(parentidvalue != null ? parentidvalue.getId() : BLANK_ID));
	}

	/**
	 * a method to perform a massive query to get all children for several parents
	 * 
	 * @param parentid               array of parent id
	 * @param additionalcondition    additional filter condition on the children
	 * @param parentobjectdefinition definition of the child object
	 * @param linkedobjectdefinition definition of the parent object
	 * @param propertydefinition     definition of the linkedtoparent property for
	 *                               the child object
	 * @return the children for the specified parent ids
	 */
	public <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & UniqueidentifiedInterface<F>> E[] getallchildrenforseveralparents(
					DataObjectId<F>[] parentid,
					QueryFilter additionalcondition,
					DataObjectDefinition<E> parentobjectdefinition,
					DataObjectDefinition<F> linkedobjectdefinition,
					LinkedtoparentDefinition<E, F> propertydefinition) {
		ArrayList<E> results = new ArrayList<E>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (parentid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = parentobjectdefinition.getAlias(CHILD_OBJECT_ALIAS);
			aliaslist.add(alias);
			if (additionalcondition != null)
				if (additionalcondition.getAliases() != null)
					for (int j = 0; j < additionalcondition.getAliases().length; j++)
						aliaslist.add(additionalcondition.getAliases()[j]);
			QueryCondition objectuniversalcondition = parentobjectdefinition
					.getUniversalQueryCondition(propertydefinition, CHILD_OBJECT_ALIAS);
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			// condition added for issue #28 to avoid query without parent clause
			if (min < parentid.length) {
				for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
					QueryCondition thisuniqueparentidcondition = null;
					if (j < parentid.length) {
						thisuniqueparentidcondition = getParentIdQueryCondition(alias, parentid[j],
								parentobjectdefinition, linkedobjectdefinition);
						uniqueidcondition.addCondition(thisuniqueparentidcondition);

					}

				}

				QueryCondition finalcondition = uniqueidcondition;
				if (objectuniversalcondition != null) {
					finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
				}

				QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
				if (additionalcondition != null)
					if (additionalcondition.getCondition() != null)
						extendedcondition = new AndQueryCondition(extendedcondition,
								additionalcondition.getCondition());
				Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
				while (answer.next()) {
					E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
					// put all results in a hasmap;
					results.add(formattedanswer);
				}
			}
		}

		return results.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all the children for the specified parent id
	 * 
	 * @param parentid               parent object id
	 * @param additionalcondition    additional filter condition on the child object
	 * @param parentobjectdefinition definition of the child object
	 * @param linkedobjectdefinition definition of the parent object
	 * @param propertydefinition     definition of the linkedtoparent property for
	 *                               the child object
	 * @return the children for the specified parent id
	 */
	public <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & UniqueidentifiedInterface<F>> E[] getallchildren(
					DataObjectId<F> parentid,
					QueryFilter additionalcondition,
					DataObjectDefinition<E> parentobjectdefinition,
					DataObjectDefinition<F> linkedobjectdefinition,
					LinkedtoparentDefinition<E, F> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias(CHILD_OBJECT_ALIAS);
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		QueryCondition finalcondition = getParentIdQueryCondition(alias, parentid, parentobjectdefinition,
				linkedobjectdefinition);
		QueryCondition objectuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(propertydefinition,
				CHILD_OBJECT_ALIAS);
		if (objectuniversalcondition != null)
			finalcondition = new AndQueryCondition(finalcondition, objectuniversalcondition);
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				finalcondition = new AndQueryCondition(finalcondition, additionalcondition.getCondition());
		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		// TODO solve this mess
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}
		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}
}
