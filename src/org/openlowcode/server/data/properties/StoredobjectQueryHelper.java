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
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * The helper allowing to perform queries for objects implepmenting the
 * Storedobject properties
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StoredobjectQueryHelper {
	private static StoredobjectQueryHelper singleton = new StoredobjectQueryHelper();
	/**
	 * the alias for the main table for get all active query
	 */
	public static String maintablealiasforgetallactive = "U0";

	/**
	 * @return the singleton query helper
	 */
	public static StoredobjectQueryHelper get() {
		return singleton;
	}

	/**
	 * gets all the possible values for a field of a data object
	 * 
	 * @param condition  extra condition for the query
	 * @param definition definition of the object
	 * @param property   stored object property of the object
	 * @return the list of unique values of the field
	 */
	public <E extends DataObject<E>,F extends Object> String[] getallvaluesforfield(StoredFieldSchema<String> fieldtoextract,
			QueryFilter condition,
			DataObjectDefinition<E> definition,
			StoredobjectDefinition<E> propertydefinition) {
		NamedList<TableAlias> tablelist = new NamedList<TableAlias>();
		if (definition == null)
			throw new RuntimeException("definition is expected to be not null");
		TableAlias mainobjectalias = definition.getAlias(maintablealiasforgetallactive);
		mainobjectalias.addFieldSelection(fieldtoextract);
		tablelist.add(mainobjectalias);
		if (condition != null)
			if (condition.getAliases() != null)
				for (int i = 0; i < condition.getAliases().length; i++) {
					TableAlias presentalias = condition.getAliases()[i];
					if (tablelist.lookupOnName(presentalias.getName()) != null) {
						TableAlias otheraliaswithsamename = tablelist.lookupOnName(presentalias.getName());
						if (!otheraliaswithsamename.getTable().getName().equals(presentalias.getTable().getName()))
							throw new RuntimeException("For alias " + presentalias.getName()
									+ ", two inconsistent tables are used " + presentalias.getTable().getName() + " - "
									+ otheraliaswithsamename.getTable().getName());
					} else {
						tablelist.add(condition.getAliases()[i]);
					}
				}
		QueryCondition finalcondition = (condition != null ? condition.getCondition() : null);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				maintablealiasforgetallactive);
		
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(finalcondition, objectuniversalcondition);
		}

		// QueryCondition enhancedcondition = definition.extendquery(tablelist, mainobjectalias, finalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(tablelist, finalcondition,true));
		ArrayList<String> returnstrings = new ArrayList<String>();
		while (row.next()) {
			String value = row.getValue(fieldtoextract, mainobjectalias);
			if (value!=null) if (value.length()>0) returnstrings.add(value);
			}
		return returnstrings.toArray(new String[0]);
	}

	/**
	 * @param condition          condition to filter further all active records
	 * @param definition         definition of the object
	 * @param propertydefinition definition of the stored object property for the
	 *                           object
	 * @return the list of objects brought back by the query
	 */
	public <E extends DataObject<E>> E[] getallactive(
			QueryFilter condition,
			DataObjectDefinition<E> definition,
			StoredobjectDefinition<E> propertydefinition) {
		NamedList<TableAlias> tablelist = new NamedList<TableAlias>();
		if (definition == null)
			throw new RuntimeException("definition is expected to be not null");
		TableAlias mainobjectalias = definition.getAlias(maintablealiasforgetallactive);
		tablelist.add(mainobjectalias);
		if (condition != null)
			if (condition.getAliases() != null)
				for (int i = 0; i < condition.getAliases().length; i++) {
					TableAlias presentalias = condition.getAliases()[i];
					if (tablelist.lookupOnName(presentalias.getName()) != null) {
						TableAlias otheraliaswithsamename = tablelist.lookupOnName(presentalias.getName());
						if (!otheraliaswithsamename.getTable().getName().equals(presentalias.getTable().getName()))
							throw new RuntimeException("For alias " + presentalias.getName()
									+ ", two inconsistent tables are used " + presentalias.getTable().getName() + " - "
									+ otheraliaswithsamename.getTable().getName());
					} else {
						tablelist.add(condition.getAliases()[i]);
					}
				}
		QueryCondition finalcondition = (condition != null ? condition.getCondition() : null);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				maintablealiasforgetallactive);
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(finalcondition, objectuniversalcondition);
		}

		QueryCondition enhancedcondition = definition.extendquery(tablelist, mainobjectalias, finalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(tablelist, enhancedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(definition.generateFromRow(row, mainobjectalias));
		}
		return returnlist.toArray(definition.generateArrayTemplate());
	}

}
