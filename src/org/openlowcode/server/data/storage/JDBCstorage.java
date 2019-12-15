/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.sql.Connection;

/**
 * a common interface to all JDBC storage connecion 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface JDBCstorage {
	/**
	 * @return a connection
	 */
	public Connection getConnection();
	/**
	 * cleans up the connection
	 */
	public void cleanup();
	/**
	 * @param connection refresh the given connection
	 */
	public void refreshConnection(Connection connection);
}
