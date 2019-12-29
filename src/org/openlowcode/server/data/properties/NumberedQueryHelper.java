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
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.QueryOperatorLike;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * A query helper utility class to perform queries on object with a number
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NumberedQueryHelper {
	public static String SINGLEOBJECT = "SINGLEOBJECT";
	private static NumberedQueryHelper singleton = new NumberedQueryHelper();

	public static NumberedQueryHelper get() {
		return singleton;
	}

	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> QueryCondition getNrLikeQueryCondition(
			TableAlias alias, String nrlikepattern, DataObjectDefinition<E> parentdefinition) {
		NumberedDefinition<E> definition = new NumberedDefinition<E>(parentdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> nr = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("NR");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, nr, new QueryOperatorEqual<String>(), nrlikepattern);
		return new SimpleQueryCondition<String>(alias, nr, new QueryOperatorLike<String>(), nrlikepattern);
	}

	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> QueryCondition getNrQueryCondition(TableAlias alias,
			String nrvalue, DataObjectDefinition<E> parentdefinition) {
		NumberedDefinition<E> definition = new NumberedDefinition<E>(parentdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> nr = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("NR");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, nr, new QueryOperatorEqual<String>(), nrvalue);
		return new SimpleQueryCondition<String>(alias, nr, new QueryOperatorEqual<String>(), nrvalue);
	}

	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] getobjectbynumber(String nr,
			DataObjectDefinition<E> definition, NumberedDefinition<E> propertydefinition) {
		return getobjectbynumber(nr, null, definition, propertydefinition);
	}

	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] getobjectbynumber(String nr,
			QueryCondition additionalcondition, DataObjectDefinition<E> definition,
			NumberedDefinition<E> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias(SINGLEOBJECT);
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				SINGLEOBJECT);
		QueryCondition numbercondition = getNrQueryCondition(alias, nr, definition);
		QueryCondition finalcondition = numbercondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, numbercondition);
		}
		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		if (additionalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition);
		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (answer.next()) {
			returnlist.add(definition.generateFromRow(answer, alias));
		}
		return returnlist.toArray(definition.generateArrayTemplate());

	}

	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E getuniqueobjectbynumber(String nr,
			QueryFilter additionalcondition, DataObjectDefinition<E> definition,
			NumberedDefinition<E> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias(SINGLEOBJECT);
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(propertydefinition,
				SINGLEOBJECT);
		QueryCondition numbercondition = getNrQueryCondition(alias, nr, definition);
		QueryCondition finalcondition = numbercondition;
		if (additionalcondition != null)
			finalcondition = new AndQueryCondition(additionalcondition.getCondition(), numbercondition);
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, numbercondition);
		}
		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		if (additionalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		if (!answer.next())
			throw new RuntimeException("Found no object '" + definition.getLabel() + "' for number '" + nr + "' "
					+ (additionalcondition != null ? "and additional condition " + additionalcondition : ""));
		E returnobject = definition.generateFromRow(answer, alias);
		if (answer.next())
			throw new RuntimeException("Found several objects '" + definition.getLabel() + "' for number '" + nr + "' "
					+ (additionalcondition != null ? "and additional condition " + additionalcondition : ""));
		return returnobject;
	}

}
