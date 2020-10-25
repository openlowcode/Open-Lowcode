/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.server.data.storage.LimitedFieldsUpdateQuery;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;

/**
 * A simple helper for a query accessing the persistence gateway
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class QueryHelper {
	private static QueryHelper singleton = new QueryHelper();

	/**
	 * @return the singleton class for the query helper
	 */
	public static QueryHelper getHelper() {
		return singleton;
	}

	/**
	 * executes properly the query through persistence gateway
	 * 
	 * @param sq a select query
	 * @return rows
	 */
	public Row query(SelectQuery sq) {
		PersistentStorage storage = PersistenceGateway.getStorage();

		Row row = storage.selectOnDB(sq);
		PersistenceGateway.checkinStorage(storage);
		return row;

	}
	
	/**
	 * Executes a limited update query thourhg the persistence gateway
	 * 
	 * @param limitedupdatequery a limited update query
	 * @since 1.14
	 */
	public void limitedUpdate(LimitedFieldsUpdateQuery limitedupdatequery) {
		PersistentStorage storage = PersistenceGateway.getStorage();
		storage.LimitedFieldUpdateOnDB(limitedupdatequery);
		PersistenceGateway.checkinStorage(storage);
	}
}
