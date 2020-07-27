/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
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
 * A helper for objects that are versioned
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class VersionedQueryHelper {
	private static VersionedQueryHelper singleton = new VersionedQueryHelper();
	public static String singleobjectalias = "SINGLEOBJECT";
	private static final int BATCH_QUERY_SIZE = 20;
	private static final String BLANK_ID = "NEVERLAND";

	/**
	 * gets a query condition filtering on master id (the common identifier to all
	 * versions of an object)
	 * 
	 * @param alias            table alias
	 * @param masteridvalue    the value to filter on
	 * @param parentdefinition definition of the object the condition is applied on
	 * @return a query condition with the specified filter
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QueryCondition getMasterIdQueryCondition(TableAlias alias, String masteridvalue,
			DataObjectDefinition parentdefinition) {
		VersionedDefinition definition = (VersionedDefinition) parentdefinition.getProperty("VERSIONED");
		StoredFieldSchema<String> masterid = (StoredFieldSchema<String>) definition.getDefinition()
				.lookupOnName("MASTERID");
		if (alias == null)
			return new SimpleQueryCondition(null, masterid, new QueryOperatorEqual<String>(), masteridvalue);
		return new SimpleQueryCondition(alias, masterid, new QueryOperatorEqual<String>(), masteridvalue);
	}

	/**
	 * gets a query condition that only returns the latest version of an object
	 * 
	 * @param alias            alias to put the condition on
	 * @param parentdefinition definition of the object
	 * @return the condition as specified
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QueryCondition getLatestVersionQueryCondition(TableAlias alias,
			DataObjectDefinition parentdefinition) {
		VersionedDefinition definition = (VersionedDefinition) parentdefinition.getProperty("VERSIONED");
		StoredFieldSchema<String> lastversion = (StoredFieldSchema<String>) definition.getDefinition()
				.lookupOnName("LASTVERSION");
		if (alias == null)
			return new SimpleQueryCondition(null, lastversion, new QueryOperatorEqual<String>(), "Y");
		return new SimpleQueryCondition(alias, lastversion, new QueryOperatorEqual<String>(), "Y");
	}

	/**
	 * a query condition that filters on the specified version value
	 * 
	 * @param alias            table alias to build the condition on
	 * @param versionvalue     version value to filter on
	 * @param parentdefinition definition of the object
	 * @return the condition as specified
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QueryCondition getVersionQueryCondition(TableAlias alias, String versionvalue,
			DataObjectDefinition parentdefinition) {
		VersionedDefinition definition = (VersionedDefinition) parentdefinition.getProperty("VERSIONED");
		StoredFieldSchema<String> version = (StoredFieldSchema<String>) definition.getDefinition()
				.lookupOnName("VERSION");
		if (alias == null)
			return new SimpleQueryCondition(null, version, new QueryOperatorEqual<String>(), versionvalue);
		return new SimpleQueryCondition(alias, version, new QueryOperatorEqual<String>(), versionvalue);

	}

	/**
	 * gets the singleton query helper
	 * 
	 * @return the singleton
	 */
	public static VersionedQueryHelper get() {
		return singleton;
	}

	/**
	 * gest the last version of the object for the specified master id
	 * 
	 * @param masterid           master id
	 * @param definition         definiton of the object
	 * @param propertydefinition definition of the versioned property for the object
	 * @return the last version of the object for the master id, or null if nothing
	 *         is found
	 */
	public <E extends DataObject<E>> E getlastversion(DataObjectMasterId<E> masterid,
			DataObjectDefinition<E> definition, VersionedDefinition<E> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias(singleobjectalias);
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				singleobjectalias);
		QueryCondition masteridcondition = getMasterIdQueryCondition(alias, masterid.getId(), definition);
		QueryCondition latestcondition = getLatestVersionQueryCondition(alias, definition);
		QueryCondition masteridlatestcondition = new AndQueryCondition(masteridcondition, latestcondition);
		QueryCondition finalcondition = masteridlatestcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, masteridlatestcondition);
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
				throw new RuntimeException("Expected one row, got more, for object type = " + definition.getName()
						+ ", masterid = " + masterid.getId() + " normal object " + firstobject + ", additional object "
						+ secondillegalanswerdrop);
			}
			return formattedanswer;
		} else {
			return null;
		}
	}

	/**
	 * gest the last version of the object for the specified list of master ids
	 * 
	 * @param masterid           master id
	 * @param definition         definiton of the object
	 * @param propertydefinition definition of the versioned property for the object
	 * @return the last version of the object for the master id, or null if nothing
	 *         is found
	 */
	public <E extends DataObject<E> & VersionedInterface<E>> E[] getlastversion(DataObjectMasterId<E>[] masterid,
			DataObjectDefinition<E> definition, VersionedDefinition<E> propertydefinition) {
		
		ArrayList<E> results = new ArrayList<E>();;
		// work by batches to ensure query is not too long
		for (int i = 0; i < (masterid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = definition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
					"SINGLEOBJECT");
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min < masterid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < masterid.length) {
					thisuniqueidcondition = VersionedQueryHelper.getMasterIdQueryCondition(alias, masterid[i].getId(),definition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = VersionedQueryHelper.getMasterIdQueryCondition(alias, BLANK_ID,definition);
				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}
			
			uniqueidcondition.addCondition(VersionedQueryHelper.getLatestVersionQueryCondition(alias, definition));

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}

			QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			while (answer.next()) {
				E formattedanswer = definition.generateFromRow(answer, alias);
				results.add(formattedanswer);
			}
			}
		}
		
		return results.toArray(definition.generateArrayTemplate());
		
		
	}

	
	/**
	 * gets all versions of the object for the specified master id
	 * 
	 * @param masterid                 master id of the object
	 * @param additionalquerycondition additional query condition if required (can
	 *                                 be null if unused)
	 * @param definition               definition of the data object
	 * @param propertydefinition       property of the data object
	 * @return all versions of the object corresponding to the master id and
	 *         potential additional query condition
	 */
	public <E extends DataObject<E>> E[] getallversions(DataObjectMasterId<E> masterid,
			QueryFilter additionalquerycondition, DataObjectDefinition<E> definition,
			VersionedDefinition<E> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias(singleobjectalias);
		aliaslist.add(alias);
		if (additionalquerycondition != null)
			if (additionalquerycondition.getAliases() != null)
				for (int i = 0; i < additionalquerycondition.getAliases().length; i++)
					aliaslist.add(additionalquerycondition.getAliases()[i]);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				singleobjectalias);
		QueryCondition masteridcondition = getMasterIdQueryCondition(alias, masterid.getId(), definition);
		QueryCondition finalcondition = masteridcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, masteridcondition);
		}

		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);

		if (additionalquerycondition != null)
			if (additionalquerycondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalquerycondition.getCondition());

		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (answer.next()) {
			returnlist.add(definition.generateFromRow(answer, alias));
		}
		return returnlist.toArray(definition.generateArrayTemplate());
	}

}
