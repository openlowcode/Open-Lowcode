/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.derbyjdbc;

import org.openlowcode.server.data.storage.standardjdbc.BaseJDBCStorage;
import org.openlowcode.server.data.storage.standardjdbc.StandardJDBCTableFieldCheck;


/**
 * A class comparing the definition of a field in the databse with the
 * definition of a field in the application data model. Value sent back in each
 * case is an integer as defined as static int in the class PersistenceStorage
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.6
 *
 */
public class DerbySQLTableFieldCheck extends StandardJDBCTableFieldCheck {

	/**
	 * Creates a TableFieldCheck object (to be used only once)
	 * @param columntype type of column to perform the check on
	 */
	public DerbySQLTableFieldCheck(BaseJDBCStorage.DatabaseColumnType columntype) {
		super(columntype);
	}

	@Override
	public String getStringDBType() {
		return "VARCHAR";
	}

	@Override
	public String getTimestampDBType() {
		return "TIMESTAMP";
	}

	@Override
	public String getDecimalDBType() {
		return "DECIMAL";
	}

	@Override
	public String getIntegerDBType() {
		return "INTEGER";
	}

	@Override
	public String getBinaryDBType() {
		return "BLOB";
	}

	@Override
	public boolean MetaDataEscapesDefaultString() {
		return true;
	}
	

}
