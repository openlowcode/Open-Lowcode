/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.derbyjdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StoredFieldSchema.Visitor;
import org.openlowcode.server.data.storage.standardjdbc.BaseJDBCStorage;

/**
 * Standard JDBC storage built for Derby database
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DerbyJDBCStorage
		extends
		BaseJDBCStorage {

	private static Logger LOGGER = Logger.getLogger(DerbyJDBCStorage.class.getName());

	@Override
	public void extendField(StoredTableSchema object, int fieldindex) {
		if (fieldindex >= object.getStoredFieldNumber())
			throw new RuntimeException(String.format("field index %d is outside of table %s range (%d)", fieldindex,
					object.getName(), object.getStoredFieldNumber()));
		StringBuffer query = new StringBuffer();
		// -------- Step 1 : CREATE COLUMN WITH NEW DEFINITION -------------------
		query.append(" ALTER TABLE ");
		query.append(object.getName());
		query.append(" ADD COLUMN ");

		StoredFieldSchema<?> field = object.getStoredField(fieldindex);
		String columnname = field.getName().toUpperCase();
		query.append(columnname);
		query.append("_NEW ");
		Visitor fielddefvisitor = fieldvisitorgenerator.apply(query);
		field.accept(fielddefvisitor);
		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.warning("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}
		// -------------- Step 2 : TRANSFER DATA -------------------------
		StringBuffer secondquery = new StringBuffer();
		secondquery.append(" UPDATE ");
		secondquery.append(object.getName());
		secondquery.append(" SET ");
		secondquery.append(columnname);
		secondquery.append("_NEW = ");
		secondquery.append(columnname);
		String secondquerystring = secondquery.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(secondquerystring);
			ps.execute();
			ps.close();
			LOGGER.warning("[PERSISTENCE] Model update: " + secondquery);
		} catch (Throwable e) {
			throw treatThrowable(e, secondquerystring);
		}
		
		StringBuffer thirdquery = new StringBuffer();
		thirdquery.append(" ALTER TABLE ");
		thirdquery.append(object.getName());
		thirdquery.append(" DROP COLUMN ");
		thirdquery.append(columnname);
		String thirdquerystring = thirdquery.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(thirdquerystring);
			ps.execute();
			ps.close();
			LOGGER.warning("[PERSISTENCE] Model update: " + thirdquerystring);
		} catch (Throwable e) {
			throw treatThrowable(e, thirdquerystring);
		}
		StringBuffer fourthquery = new StringBuffer();
		fourthquery.append(" RENAME COLUMN ");
		fourthquery.append(object.getName());
		fourthquery.append(".");
		fourthquery.append(columnname);
		fourthquery.append("_NEW TO ");
		fourthquery.append(columnname);
		String fourthquerystring = fourthquery.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(fourthquerystring);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + fourthquerystring);
		} catch (Throwable e) {
			throw treatThrowable(e, fourthquerystring);
		}
	}

	public DerbyJDBCStorage(Connection connection) {
		super(connection, (a -> new DerbySQLTableFieldDefinition(a)), (a -> new DerbySQLTableFieldCheck(a)));

	}

}
