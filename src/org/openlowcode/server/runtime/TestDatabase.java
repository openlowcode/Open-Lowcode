/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StoredTableRow;
import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.derbyjdbc.DerbyJDBCStorage;
import org.openlowcode.server.data.storage.mariajdbc.MariadDBJDBCStorage;

/**
 * A test utility to check if database is OK (accessible, Ok for persistence of
 * latine and asian characters. <br>
 * Note: this file should be compiled as UTF-8
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TestDatabase {

	public static void main(String[] args) {
		boolean parameterok = true;
		String dbtype = null;
		String url = null;
		String user = null;
		String password = null;
		boolean derby = false;
		boolean mariadb102 = false;

		if (args.length < 2) {
			parameterok = false;
		}
		if (parameterok) {
			dbtype = args[0];
			url = args[1];
			if (dbtype.equals("DERBY")) {
				url = "jdbc:derby:" + url + ";create=true";
				derby = true;
			}
			if (dbtype.equals("MARIA10.2"))
				mariadb102 = true;
			if ((derby == false) && (mariadb102 == false))
				parameterok = false;
		}
		if (mariadb102) {
			if (args.length < 4)
				parameterok = false;
			if (args.length >= 4) {
				user = args[2];
				password = args[3];
			}
		}
		if (!parameterok) {
			System.err.println(" Error. Syntax java TestDatabase DBTYPE URL [User Password]");
			System.err.println("DBTYPE is either DERBY or MARIA10.2");
			System.err.println("URL is :");
			System.err.println("	- path of the folder if derby");
			System.err.println("	- JDBC full URL for MARIA10.2");
			System.err.println("User : jdbc database user (not needed for DERBY)");
			System.err.println("Password : jdbc database password (not needed for DERBY)");
			System.exit(1);
		}
		try {
			Logger rootlogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			for (int i = 0; i < rootlogger.getHandlers().length; i++) {
				rootlogger.removeHandler(rootlogger.getHandlers()[i]);
			}
			Logger anonymouslogger = Logger.getLogger("");
			for (int i = 0; i < anonymouslogger.getHandlers().length; i++) {
				anonymouslogger.removeHandler(anonymouslogger.getHandlers()[i]);
			}
			System.err.println("Starting TestDatabase Program");

			Connection connection;
			if (user == null) {
				connection = DriverManager.getConnection(url);
			} else {
				connection = DriverManager.getConnection(url, user, password);
			}
			System.err.println("Connection succeeded");

			PersistentStorage storage = null;
			if (derby)
				storage = new DerbyJDBCStorage(connection);
			if (mariadb102)
				storage = new MariadDBJDBCStorage(connection);
			StoredTableSchema ping = new StoredTableSchema("PING");
			StringStoredField content = new StringStoredField("CONTENT", ping, 1000);
			StoredTableIndex contentindex = new StoredTableIndex("CONTENTIDX");
			contentindex.addStoredFieldSchame(content);

			ping.addField(content);
			ping.addIndex(contentindex);
			if (!storage.DoesObjectExist(ping)) {
				storage.createObject(ping);
				System.err.println("created table ping succesfully");
			}
			StoredTableRow testrowascii = new StoredTableRow(ping);
			testrowascii.setPayload(content, "abcdef");
			storage.insertOnDB(testrowascii);
			System.err.println("Stored ascii string succesfully");
			StoredTableRow testrowlatine = new StoredTableRow(ping);
			testrowlatine.setPayload(content, "abcdeféàç€ œ ß");
			storage.insertOnDB(testrowlatine);
			System.err.println("Stored latine string succesfully");
			StoredTableRow testrowunicode = new StoredTableRow(ping);
			testrowunicode.setPayload(content, "東京");
			storage.insertOnDB(testrowunicode);
			System.err.println("Stored unicode string succesfully");
		} catch (Exception e) {
			System.err.println("--------------------------------------------------------");
			System.err.println("  Exception : " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
