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
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * A query helper to support property 'Numbered for Parent'.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NumberedforparentQueryHelper {
	private static NumberedforparentQueryHelper singleton = new NumberedforparentQueryHelper();

	/**
	 * gest the singleton class for this query helper
	 * 
	 * @return the singleton class
	 */
	public static NumberedforparentQueryHelper get() {
		return singleton;
	}

	/**
	 * gets the object with the provided number for the provided parent
	 * 
	 * @param nr                          number
	 * @param parentid                    id of the parent
	 * @param definition                  definition of the object with the number
	 *                                    (the child)
	 * @param parentdefinition            definition of the parent
	 * @param numberedforparentdefinition property numbered for parent
	 * @return the objects corresponding to the given number and id of the parent
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E> & NumberedforparentInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F>> E[] getobjectbynumberforparent(
			String nr, DataObjectId<F> parentid, DataObjectDefinition<E> definition,
			DataObjectDefinition<F> parentdefinition, NumberedforparentDefinition<E, F> numberedforparentdefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = definition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		QueryCondition objectuniversalcondition = definition.getUniversalQueryCondition(numberedforparentdefinition,
				"SINGLEOBJECT");
		QueryCondition numbercondition = NumberedQueryHelper.get().getNrQueryCondition(alias, nr, definition);
		QueryCondition finalcondition = numbercondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, numbercondition);
		}
		QueryCondition parentcondition = LinkedtoparentQueryHelper
				.get(numberedforparentdefinition.getRelatedLinkedToParentDefinition().getName())
				.getParentIdQueryCondition(alias, parentid, definition, parentdefinition);
		finalcondition = new AndQueryCondition(finalcondition, parentcondition);
		QueryCondition extendedcondition = definition.extendquery(aliaslist, alias, finalcondition);
		Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (answer.next()) {
			returnlist.add(definition.generateFromRow(answer, alias));
		}
		return returnlist.toArray(definition.generateArrayTemplate());

	}
}
