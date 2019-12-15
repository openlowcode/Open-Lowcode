/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.mariajdbc;

import java.sql.Connection;

import org.openlowcode.server.data.storage.standardjdbc.BaseJDBCStorage;

/**
 * A storage for Maria DB version 10.2 or further
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MariadDBJDBCStorage extends BaseJDBCStorage {

	/**
	 * creates a MariaDB storage for the given connection
	 * 
	 * @param connection a valid JDBC connection
	 */
	public MariadDBJDBCStorage(Connection connection) {
		super(connection, (a -> new MariaDBSQLTableFieldDefinition(a)));

	}

}
