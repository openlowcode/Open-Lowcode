/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.jdbcpool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Specifies the interface for a connection pool for Open Lowcode
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface ConnectionPool {
	/**
	 * this method tries for a defined period of time to get a database connection in the pool. If after the timeout, connection could not be gotten,
	 * then it returns null.
	 * @return a connection if one connection was available, null else
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public Connection getConnectionWithRetry() throws SQLException, InterruptedException; 
	/**
	 * @param connection brought back the connection to the pool
	 */
	public void checkin(Connection connection);
	
	/**
	 * @param connection brought back to the connection pool and marked as error, will be recreated before next usage 
	 */
	public void checkinandreset(Connection connection);
	
	/**
	 * will free all connections for the current thread
	 */
	public void freecurrentthreadconnections();
}
