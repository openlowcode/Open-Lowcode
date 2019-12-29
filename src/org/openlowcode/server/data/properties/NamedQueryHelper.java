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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Helper for queries on named object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NamedQueryHelper {

	private static NamedQueryHelper singleton = new NamedQueryHelper();

	public static NamedQueryHelper get() {
		return singleton;
	}

	/**
	 * generates a query condition to get objects where the name is exactly as
	 * specified
	 * 
	 * @param alias        table alias
	 * @param namevalue    value of the name to query on
	 * @param parentobject parent object
	 * @return a query condition as specified
	 */

	public <E extends DataObject<E>> QueryCondition getNameQueryCondition(TableAlias alias, String namevalue,
			DataObjectDefinition<E> parentobject) {
		NamedDefinition<E> definition = new NamedDefinition<E>(parentobject);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> name = (StoredFieldSchema<String>) definition.getDefinition()
				.lookupOnName("OBJECTNAME");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, name, new QueryOperatorEqual<String>(), namevalue);
		return new SimpleQueryCondition<String>(alias, name, new QueryOperatorEqual<String>(), namevalue);
	}
}
