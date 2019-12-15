/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.standardjdbc;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.ExternalFieldSchemaTemplate;
import org.openlowcode.server.data.storage.FieldSchema;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.tools.messages.SFile;

/**
 * a wrapper around JDBC resultset as a result of queries in the persistence
 * storage
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JDBCRow implements Row {
	private static Logger logger = Logger.getLogger(JDBCRow.class.getName());
	private ResultSet rs;
	private PreparedStatement ps;
	private ResultSetMetaData rsmetadata;
	private String columnlist = null;
	private long readcolumns;
	private String stringquery;

	/**
	 * creates a JDBC Row
	 * @param ps prepared statement
	 * @param rs resultset
	 * @param stringquery the query for further reference (error handling)
	 */
	public JDBCRow(PreparedStatement ps, ResultSet rs, String stringquery) {
		this.ps = ps;
		this.rs = rs;
		this.readcolumns = 0;
		this.stringquery = stringquery;
	}

	@Override
	public <E> E getValue(FieldSchema<E> fd, TableAlias objectalias) {
		if (rs == null)
			throw new RuntimeException("End of ResultSet reached for preparedStatement");
		if (objectalias == null)
			throw new RuntimeException("ObjectAlias is null");
		try {
			if (this.rsmetadata == null) {
				this.rsmetadata = rs.getMetaData();
				columnlist = "[";
				for (int i = 1; i <= this.rsmetadata.getColumnCount(); i++) {
					if (i > 1)
						columnlist += "; ";
					columnlist += this.rsmetadata.getTableName(i) + ":" + this.rsmetadata.getColumnName(i) + ":"
							+ this.rsmetadata.getColumnTypeName(i);
				}
				columnlist += "]";
			}
			String fieldname = objectalias.getName() + "_" + fd.getName();
			if (fd instanceof StringStoredField) {
				String result = rs.getString(fieldname);
				logger.finest("processing string stored field " + fieldname + ": " + result);
				return fd.castToType(result);
			}
			if (fd instanceof TimestampStoredField) {
				Timestamp result = rs.getTimestamp(fieldname);
				logger.finest("processing timestamp stored field " + fieldname + ": " + result);
				return fd.castToType(result);
			}

			if (fd instanceof DecimalStoredField) {
				BigDecimal result = rs.getBigDecimal(fieldname);
				logger.finest("processing bigdecimal stored field " + fieldname + ": " + result);
				return fd.castToType(result);
			}
			if (fd instanceof IntegerStoredField) {
				int result = rs.getInt(fieldname);
				logger.finest("processing timestamp stored field " + fieldname + ": " + result);
				return fd.castToType(new Integer(result));
			}
			if (fd instanceof LargeBinaryStoredField) {
				Blob blob = rs.getBlob(fieldname);
				if (blob != null) {
					byte bytes[] = blob.getBytes(1, (int) (blob.length()));
					logger.finest("processing largebinary stored field " + fieldname + " size = " + bytes.length + "b");

					return fd.castToType(new SFile("TEMPORARY", bytes));
				} else {
					return fd.castToType(new SFile());
				}
			}

			if (fd instanceof ExternalFieldSchemaTemplate) {
				logger.finest("processing external field");

				ExternalFieldSchemaTemplate externalfieldschema = (ExternalFieldSchemaTemplate) fd;

				ArrayList<StoredFieldSchema<E>> externalfieldschemalist = externalfieldschema.getExternalTableField();
				if (externalfieldschemalist.size() == 0)
					throw new RuntimeException(
							"no field defined in external field schema " + externalfieldschema.getName());
				if (externalfieldschemalist.size() == 1) {

					StoredFieldSchema<E> referencedfield = externalfieldschemalist.get(0);
					String name = objectalias.getName() + externalfieldschema.getName() + "_0";

					if (referencedfield instanceof StringStoredField) {
						StringStoredField referencedstringfield = (StringStoredField) referencedfield;
						logger.finest("processed field = " + referencedstringfield.getName() + " from field "
								+ externalfieldschema.getName());
						return (E) referencedstringfield.castToType(rs.getString(name));

					}
					if (referencedfield instanceof TimestampStoredField) {

						return referencedfield.castToType(rs.getTimestamp(name));
					}
					if (referencedfield instanceof DecimalStoredField) {
						return referencedfield.castToType(rs.getBigDecimal(name));
					}
				}
				if (externalfieldschemalist.size() > 1) {
					String compactstring = "";
					for (int i = 0; i < externalfieldschemalist.size(); i++) {
						String name = objectalias.getName() + externalfieldschema.getName() + "_" + i;
						if (i > 1)
							compactstring += " ";
						compactstring += rs.getString(name);
						if (i == 0)
							compactstring += " (";
					}
					compactstring += ")";
					// crappy code but E is string (see a pattern here ?)
					return (E) (compactstring);

				}

			}
		} catch (SQLException e) {

			String exceptionline = "";
			for (int i = 0; i < e.getStackTrace().length; i++) {

				String thisline = e.getStackTrace()[i].toString();
				if (thisline.indexOf("gallium.server.data") != -1) {
					exceptionline = thisline;
					break;
				}
			}
			throw new RuntimeException("SQL Exception %s " + e.getMessage() + " at " + exceptionline
					+ ", drop table field list " + this.columnlist + "\\			---> Query = " + this.stringquery);
		}
		throw new RuntimeException(" type of attribute not supported yet " + fd.getClass().getCanonicalName());
	}

	@Override
	public boolean next() {
		try {
			this.readcolumns++;
			if (readcolumns % 2000 == 0) {
				if (readcolumns % 10000 == 0) {
					logger.warning(" --- query read ongoing : lines " + readcolumns + " for query "
							+ (this.stringquery.length() > 150 ? this.stringquery.substring(0, 150) + "..."
									: this.stringquery));
				} else {
					logger.info(" --- query read ongoing : lines " + readcolumns + " for query "
							+ (this.stringquery.length() > 50 ? this.stringquery.substring(0, 50) + "..."
									: this.stringquery));
				}
			}

			boolean hasnext = rs.next();
			// this is to ensure we close prepared statement and resultset when there is no
			// next.
			if (!hasnext)
				close();
			return hasnext;
		} catch (SQLException e) {
			throw new RuntimeException("persistence error JDBC database %s " + e.getMessage());
		}
	}

	@Override
	public void close() {
		try {
			rs.close();
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(String.format("Error in closing connection %s", e.getMessage()));
		}

	}

	@Override
	protected void finalize() throws Throwable {
		// this ensures resultset and preparesstatement are closed when row is garbage
		// collected
		close();
	}

}
