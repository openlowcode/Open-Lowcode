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

/**
 * the interface to all persistence storage. This manages persistence in table,
 * typically stored in a sql database
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings("rawtypes")
public interface PersistentStorage {
	/**
	 * This method will be played once at the launch of the server. It serves to
	 * initiate workaround for databases that do not support all expected features.
	 * Today, this is used to create a table to store sequences for databases that
	 * do not support them.
	 * 
	 */
	public void technicalInit();

	/**
	 * selects data in the persistent storage
	 * 
	 * @param sq select query
	 * @return a set of Rows (Row is actually an iterator)
	 */
	public Row selectOnDB(SelectQuery sq);

	/**
	 * Insert a single row in the persistent storage
	 * 
	 * @param row the row to insert
	 */
	public void insertOnDB(StoredTableRow row);

	/**
	 * Insert multiple rows in the persistent storage
	 * 
	 * @param row multiple rows to insert
	 */
	public void MassiveInsertOnDB(MultipleTableRow row);

	/**
	 * updates multiple rows in the persistent storage
	 * 
	 * @param row multiple rows to update
	 */
	public void MassiveUpdateOnDB(MultipleTableRow row);

	/**
	 * deletes multiple rows in the persistent storage
	 * 
	 * @param row multiple rows to update
	 */
	public void MassiveDeleteOnDB(MultipleTableRow rowstodelete);

	/**
	 * updates a single line in the database
	 * 
	 * @param uq query to update a single row
	 */
	public void UpdateOnDB(UpdateQuery uq);

	/**
	 * deletes a single line in the database
	 * 
	 * @param dq query to delete a single row
	 */
	public void DeleteOnDB(DeleteQuery dq);

	/**
	 * Introspection of the datamodel: checks if the table exists
	 * 
	 * @param object the table schema to check
	 * @return true if the table already exists
	 */
	public boolean DoesObjectExist(StoredTableSchema object);

	/**
	 * @param object     the table schema to check
	 * @param fieldindex field to check (by index)
	 * @return
	 */
	public boolean DoesFieldExist(StoredTableSchema object, int fieldindex);

	/**
	 * value if index is OK in persistence layer
	 */
	public static final int INDEX_OK = 0;
	/**
	 * value if index is different
	 */
	public static final int INDEX_DIFFERENT = 1;
	/**
	 * value if indes is not present at all
	 */
	public static final int INDEX_NOT_PRESENT = 2;

	/**
	 * drops the index as specified by name
	 * 
	 * @param name name of the index
	 */
	public void dropIndex(String name);

	/**
	 * checks if the index exists and indexes the specified fields
	 * 
	 * @param object object to create the index on
	 * @param fields ordered list of fields
	 * @param name   name of the index
	 * @return a value as defined by static int in this class
	 */
	public int DoesIndexExist(StoredTableSchema object, StoredFieldSchema[] fields, String name);

	/**
	 * creates a new table in the persistence layer
	 * 
	 * @param object definition (schema) of the stored table
	 */
	public void createObject(StoredTableSchema object);

	/**
	 * creates the n-th field of the specified object
	 * 
	 * @param object     object definition (schema) of the stored table
	 * @param fieldindex index of the field to create
	 */
	public void createField(StoredTableSchema object, int fieldindex);

	/**
	 * @param name
	 * @param object
	 * @param fields
	 * @param unique
	 */
	public void createSearchIndex(String name, StoredTableSchema object, StoredFieldSchema[] fields, boolean unique);

	/**
	 * sets the persistence layer as either autocommit (each action is persisted) or
	 * manual commit (several actions can be packaged in )
	 * 
	 * @param autocommit true for autocommit, false for manual commit
	 */
	public void setAutoCommit(boolean autocommit);

	/**
	 * in case of manual commit, starts a transaction
	 */
	public void startTransaction();

	/**
	 * in case of manual commit, finishes a transaction
	 */
	public void commitTransaction();

	/**
	 * in case of manual commit, rollbacks a transaction
	 */
	public void rollbackTransaction();

	/**
	 * Checks if a sequence already exists
	 * 
	 * @param sequencename name of the sequence
	 * @return true if the sequence exists, false if the sequence does not exist
	 */
	public boolean isSequenceExisting(String sequencename);

	/**
	 * creates a unique sequence in the database
	 * 
	 * @param sequencename name of the sequence
	 * @param firstvalue   first integer value of the sequence
	 */
	public void createSequence(String sequencename, int firstvalue);

	/**
	 * gets the next value from the sequence
	 * 
	 * @param sequencename name of the sequence
	 * @return the next value
	 */
	public int getNextValue(String sequencename);

	/**
	 * close all connections of the persistence layer
	 */
	public void closeConnections();

}
