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

import org.openlowcode.server.data.storage.derbyjdbc.DerbyJDBCStorage;
import org.openlowcode.server.data.storage.jdbcpool.ConnectionPool;
import org.openlowcode.server.data.storage.mariajdbc.MariadDBJDBCStorage;


/**
 * a static class allowing access to the default persistence whereever
 * persistence needs to be called in the application
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PersistenceGateway {
	
	public static final String DBTYPE_DERBY = "DERBY";
	public static final String DBTYPE_MARIA10_2 = "MARIA10.2";
	
	private static ConnectionPool connectionpool;
	/**
	 * type of database. Needs to correspond to one of the constants in this class
	 */
	public static String dbtype;
	/**
	 * releases the connection for the current thread.
	 */
	public static void releaseForThread() {
		connectionpool.freecurrentthreadconnections();
	}
	
	/**
	 * gets a storage (wrapping the connection with all useful methods) for the current thread
	 * @return a free storage
	 */
	public static PersistentStorage getStorage()  {
		
		
		
			try {
				Connection connection = connectionpool.getConnectionWithRetry();
				if (connection==null) throw new RuntimeException("could not get a connection in the alloted time");
				if (dbtype.equals(DBTYPE_DERBY)) return new DerbyJDBCStorage(connection);
				if (dbtype.equals(DBTYPE_MARIA10_2)) return new MariadDBJDBCStorage(connection);
				throw new RuntimeException("DB Type not supported "+dbtype);
			} catch (Exception e) {
				throw new RuntimeException("Error in trying to establish SQL Connection : "+e.getMessage());
			}
		
		
	}
	
	/**
	 * @param storage the storage to give back and refresh 
	 * @param getconnectionback if true, gives back a new connection. If false, stored the connection
	 */
	public static void refreshStorage(PersistentStorage storage,boolean getconnectionback)  {
		
		if (storage instanceof JDBCstorage) {
			try {
				JDBCstorage jdbcstorage = (JDBCstorage) storage;
			
			Connection connection = jdbcstorage.getConnection();
			connectionpool.checkinandreset(connection);
			if (getconnectionback) {
				Connection newconnection = connectionpool.getConnectionWithRetry();
				jdbcstorage.refreshConnection(newconnection);
			}
			return;
			} catch (Exception e) {
				throw new RuntimeException("Error in trying to establish SQL Connection : "+e.getMessage());
			}
		}
		throw new RuntimeException("CheckinStorage not implemented for storage type ");
		
	}
	
	/** 
	 * returns the specified storage to the pool.
	 * @param storage returns the storage for current thread
	 */
	public static void checkinStorage(PersistentStorage storage)  {
		
		
		if (storage instanceof JDBCstorage) {
			JDBCstorage jdbcstorage = (JDBCstorage) storage;
			Connection connection = jdbcstorage.getConnection();
			connectionpool.checkin(connection);
			jdbcstorage.cleanup();	
			return;
		}
		throw new RuntimeException("CheckinStorage not implemented for storage type ");
		
	}
	/**
	 * sets the connection pool for the Persistence Gateway
	 * @param dbtype type of database defined as one of the constants in this class.
	 * @param connectionpool connection pool to use.
	 */
	public static void setconnectionpool(String dbtype,ConnectionPool connectionpool) {
		PersistenceGateway.connectionpool = connectionpool;
		PersistenceGateway.dbtype = dbtype;
	}
}
