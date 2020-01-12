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

import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * query helper for data objects that have a generic link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GenericlinkQueryHelper {
	private static HashMap<String, GenericlinkQueryHelper> helperlist = new HashMap<String, GenericlinkQueryHelper>();
	public static String maintablealiasforgetallforgenericid = "U0";
	private String name;

	/**
	 * generates a query helper for the generic link of the given name
	 * 
	 * @param name generic link property name
	 */
	public GenericlinkQueryHelper(String name) {
		this.name = name;
	}

	/**
	 * gets the query helper for the provided property name
	 * 
	 * @param name generic link property name
	 * @return the generic link query helper
	 */
	public static GenericlinkQueryHelper get(String name) {
		GenericlinkQueryHelper answer = helperlist.get(name);
		if (answer != null)
			return answer;
		answer = new GenericlinkQueryHelper(name);
		helperlist.put(name, answer);
		return answer;
	}

	/**
	 * generates a condition to filter a data object based on a generic id
	 * 
	 * @param alias            alias of the table of the object having the generic
	 *                         link property
	 * @param genericidvalue   data object id of the data object the generic link is
	 *                         pointing to
	 * @param parentdefinition definition of the data object holding the generic
	 *                         link
	 * @return the corresponding query condition
	 */
	public <E extends DataObject<E>> QueryCondition getGenericIdQueryCondition(TableAlias alias,
			DataObjectId<?> genericidvalue, DataObjectDefinition<E> parentdefinition) {
		GenericlinkDefinition<E> genericlinkdefinition = new GenericlinkDefinition<E>(parentdefinition, name);

		String fieldname = name.toUpperCase() + "ID";
		@SuppressWarnings({ "unchecked", "rawtypes" })
		StoredFieldSchema<String> genericobjectidfield = (StoredFieldSchema) genericlinkdefinition.getDefinition()
				.lookupOnName(fieldname);
		if (genericobjectidfield == null)
			throw new RuntimeException("could not find field in definition with name = '" + fieldname
					+ "', available values = " + genericlinkdefinition.getDefinition().dropNameList());
		SimpleQueryCondition<String> querycondition = new SimpleQueryCondition<String>(alias, genericobjectidfield,
				new QueryOperatorEqual<String>(), genericidvalue.getId());
		return querycondition;
	}

	/**
	 * gets all the objects that have the provided generic object id. Note: the
	 * function will work correctly provided data object ids are unique across the
	 * whole application (i.e. object A cannot have the same id as an object B even
	 * if they are different data object types)
	 * 
	 * @param genericobjectid                        generic object data object id
	 * @param additionalcondition                    additional condition
	 * @param parentdefinition                       definition of the parent data
	 *                                               object holding the generic link
	 * @param genericlinkforworkflowobjectDefinition definition of the generic link
	 *                                               property
	 * @return all the objects pointing to the provided data object id through a
	 *         generic link property
	 */
	public <E extends DataObject<E>> E[] getallforgenericid(DataObjectId<?> genericobjectid,
			QueryFilter additionalcondition, DataObjectDefinition<E> parentdefinition,
			GenericlinkDefinition<E> genericlinkforworkflowobjectDefinition) {
		// Generate Alias
		TableAlias alias = new TableAlias(parentdefinition.getTableschema(), maintablealiasforgetallforgenericid);
		QueryCondition querycondition = getGenericIdQueryCondition(alias, genericobjectid, parentdefinition);

		// Perform query
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		QueryCondition finalcondition = querycondition;
		QueryCondition objectuniversalcondition = parentdefinition.getUniversalQueryCondition(
				genericlinkforworkflowobjectDefinition, maintablealiasforgetallforgenericid);
		if (objectuniversalcondition != null)
			finalcondition = new AndQueryCondition(finalcondition, objectuniversalcondition);
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				finalcondition = new AndQueryCondition(finalcondition, additionalcondition.getCondition());
		QueryCondition extendedcondition = parentdefinition.extendquery(aliaslist, alias, finalcondition);
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentdefinition.generateFromRow(row, alias));
		}
		return returnlist.toArray(parentdefinition.generateArrayTemplate());
	}

}
