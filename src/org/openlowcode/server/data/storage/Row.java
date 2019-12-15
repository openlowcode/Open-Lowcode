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
 * A set of rows given as a result of a select query
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *         
 * A row is one line of result from a query. It can span around several StoredTable datas in case of join
 *
 */
public interface Row {
	/**
	 * @param fd a stored field
	 * @param objectalias the alias of the object 
	 * @return the payload object
	 */
	public <E extends Object> E getValue(FieldSchema<E> fd,TableAlias objectalias) ;
	/**
	 * goes to next row
	 * @return true if there is a line of data, false else. Note: the first time
	 * it is called, it positions at the start of the first row of data
	 */
	public boolean next();
	
	/**
	 * closes the data set (typically a JDBC resultset)
	 */
	public void close();
}
