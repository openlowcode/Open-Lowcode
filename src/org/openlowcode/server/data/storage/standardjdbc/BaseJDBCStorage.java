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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.sql.rowset.serial.SerialBlob;

import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.DeleteQuery;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.JDBCstorage;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.LimitedFieldsUpdateQuery;
import org.openlowcode.server.data.storage.MultipleTableRow;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StoredTableRow;
import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.server.data.storage.UpdateQuery;
import org.openlowcode.server.data.storage.StoredFieldSchema.TestVisitor;
import org.openlowcode.server.data.storage.StoredFieldSchema.Visitor;
import org.openlowcode.server.data.storage.TableAlias.FieldSelectionAlias;
import org.openlowcode.server.data.storage.derbyjdbc.DerbyJDBCStorage;
import org.openlowcode.tools.messages.SFile;

/**
 * the base JDBC storage used by this persistence layer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings("rawtypes")
public abstract class BaseJDBCStorage
		implements
		PersistentStorage,
		JDBCstorage {
	public static int MAX_SQLERROR_RETRY = 5;
	public static int TIMEOUT_SQLERROR_RETRY_MS = 20; // in ms
	private static Logger LOGGER = Logger.getLogger(DerbyJDBCStorage.class.getName());

	@Override
	public Connection getConnection() {
		return connection;
	}

	protected HashMap<String, HashMap<String, DatabaseColumnType>> existingfields;
	protected Connection connection;
	private DatabaseMetaData metadata;
	protected Function<StringBuffer, Visitor> fieldvisitorgenerator;
	protected Function<DatabaseColumnType, TestVisitor<Integer>> fieldanalyzer;

	/**
	 * creates a new JDBC storage
	 * 
	 * @param connection            the connection
	 * @param fieldvisitorgenerator relevant field visitor
	 * @param fieldanalyzer      relevant field testvisitor for checking if
	 *                              current field is OK in the database 
	 * @since 1.6
	 */
	public BaseJDBCStorage(
			Connection connection,
			Function<StringBuffer, StoredFieldSchema.Visitor> fieldvisitorgenerator,
			Function<DatabaseColumnType,StoredFieldSchema.TestVisitor<Integer>> fieldanalyzer) {
		this.connection = connection;
		this.fieldvisitorgenerator = fieldvisitorgenerator;
		this.fieldanalyzer = fieldanalyzer;
	}

	/**
	 * Processes an error
	 * 
	 * @param t     a throwable
	 * @param query context query
	 * @return a RuntimeException
	 */
	public RuntimeException treatThrowable(Throwable t, String query) {
		if (t instanceof RuntimeException) {
			LOGGER.warning(" Runtime Exception during persistent storage, checking-in storage");
			PersistenceGateway.checkinStorage(this);
			LOGGER.warning("Persistent Storage checked-in");
			return (RuntimeException) t;
		}
		LOGGER.severe("---------------------- SQLException during SQL Query ------------------------");
		LOGGER.severe(" 	" + t.getClass() + " - " + t.getMessage());
		for (int i = 0; i < t.getStackTrace().length; i++)
			LOGGER.severe(" " + t.getStackTrace()[i]);
		LOGGER.severe("---------------------------------------------------------------------------------");

		if (t instanceof SQLException) {
			PersistenceGateway.checkinStorage(this);

			SQLException e = (SQLException) t;
			RuntimeException exception = new RuntimeException(String.format(
					"Error in SQL database : %d : %s for SQL query %s", e.getErrorCode(), e.getMessage(), query));
			return exception;
		} else {
			PersistenceGateway.checkinStorage(this);

		}

		return new RuntimeException(
				String.format("Error in SQL database : %s : %s for SQL query %s", t.getClass(), t.getMessage(), query));

	}

	/**
	 * A class allowing to pass as argument the planned execution of a query to a
	 * service that provides relaunch
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E>
	 */
	public abstract class SQLExecution<E extends Object> {
		private String stringquery;
		// if set to true, a rollback will be tried;
		protected boolean requiresrollback = false;
		// if requiresrollback sets to true, this setting of autocommit will be brought
		// back
		protected boolean forceautocommitifrollaback = false;
		protected boolean forceautocommitiferror;

		/**
		 * creates a new sql execution
		 * 
		 * @param forceautocommitiferror behaviour in case of error
		 * @param stringquery            query to execute
		 */
		public SQLExecution(boolean forceautocommitiferror, String stringquery) {
			this.stringquery = stringquery;
			this.forceautocommitiferror = forceautocommitiferror;
		}

		/**
		 * creates a new sql execution with force auto commit = false
		 * 
		 * @param stringquery the string query to execute
		 */
		public SQLExecution(String stringquery) {
			this.stringquery = stringquery;
			this.forceautocommitiferror = false;
		}

		/**
		 * executes the query
		 * 
		 * @return data if the query returns any data
		 * @throws SQLException
		 */
		public abstract E executes() throws SQLException;

		/**
		 * @return the string query
		 */
		public String getStringQuery() {
			return stringquery;
		}
	}

	/**
	 * @param execution the sql execution
	 * @return the data if the query returns any data
	 */
	public <E extends Object> E executeWithRelaunch(SQLExecution<E> execution) {
		SQLException lastsqlexception = null;
		for (int i = 0; i < MAX_SQLERROR_RETRY; i++) {
			if (i != 0)
				LOGGER.warning(
						"[PERSISTENCE] Retrying after SQL error, index = " + i + " query = " + execution.stringquery);
			try {
				long beforequery = new Date().getTime();
				E returnvalue = execution.executes();
				long afterquery = new Date().getTime();
				long duration = afterquery - beforequery;
				String performancelog = "[PERSISTENCE] query executed in " + duration + "ms, query = "
						+ execution.stringquery;
				if (duration > 2000)
					LOGGER.warning(performancelog);
				if (duration > 300)
					if (duration <= 2000)
						LOGGER.info(performancelog);
				if (duration <= 300)
					LOGGER.fine(performancelog);

				return returnvalue;
			} catch (SQLException e) {
				LOGGER.warning("[PERSISTENCE] - SQLException detected in round " + i + " " + e);
				for (int st = 0; st < e.getStackTrace().length; st++) {
					LOGGER.warning("[PERSISTENCE] -    " + e.getStackTrace()[st]);
				}
				lastsqlexception = e;
				if (execution.forceautocommitiferror == true) {
					try {
						connection.setAutoCommit(true);
					} catch (SQLException rollbackexception) {
						LOGGER.warning(" ---- Exception during set back of autocommit to true " + rollbackexception);
					}
				}
				if (execution.requiresrollback) {
					try {
						connection.rollback();
						connection.setAutoCommit(execution.forceautocommitifrollaback);
					} catch (SQLException rollbackexception) {
						LOGGER.warning(" ---- Exception during rollback " + rollbackexception);
					}
				}
				while (i < MAX_SQLERROR_RETRY) {
					try {
						try {
							Thread.sleep(TIMEOUT_SQLERROR_RETRY_MS * (i + 1) * (i + 1));
						} catch (InterruptedException interruptexception) {
							throw new RuntimeException("Interrupt Exception " + interruptexception);
						}
						LOGGER.warning("[PERSISTENCE] trying to refresh storage ");
						// only get back the connection if this is not the last cycle. Else, just put
						// back the connection.
						PersistenceGateway.refreshStorage(this, (i < MAX_SQLERROR_RETRY - 1 ? true : false));
						LOGGER.warning("[PERSISTENCE] storage refreshed ");

						break;
					} catch (RuntimeException exceptionduringreset) {
						i++;
					}
				}

			}

		}
		throw new RuntimeException("Database sqlerror, even after retries " + lastsqlexception);
	}

	
	
	

	@Override
	public Row selectOnDB(SelectQuery sq) {

		StringBuffer query = new StringBuffer();
		query.append(" SELECT ");
		if (sq.isDistinctValues()) query.append(" DISTINCT ");
		// build selectclause
		for (int i = 0; i < sq.getTableNumber(); i++) {
			TableAlias thisalias = sq.getTable(i);
			if (thisalias.queryAllFields()) {
				for (int j = 0; j < thisalias.getTable().getStoredFieldNumber(); j++) {
					if ((i != 0) || (j != 0))
						query.append(" , "); // adds coma if not the first field

					StoredFieldSchema thisfield = thisalias.getTable().getStoredField(j);
					query.append(thisalias.getName());
					query.append('.');
					query.append(thisfield.getName());
					query.append(" AS ");
					query.append(thisalias.getName());
					query.append('_');
					query.append(thisfield.getName());
				}
			} else {// only selected fied
				for (int j = 0; j < thisalias.getFieldSelection().length; j++) {
					if ((i != 0) || (j != 0))
						query.append(" , ");

					FieldSelectionAlias thisfield = thisalias.getFieldSelection()[j];
					query.append(thisalias.getName());
					query.append('.');
					if (thisfield.getField() == null)
						throw new RuntimeException("field is null for " + thisalias.getName() + "("
								+ thisalias.getTable().getName() + ") index=" + j);
					query.append(thisfield.getField().getName());
					query.append(" AS ");
					query.append(thisfield.getAlias());
				}
			}

		}
		query.append(" FROM ");
		// builds from clause
		for (int i = 0; i < sq.getTableNumber(); i++) {
			if (i != 0)
				query.append(" , ");
			TableAlias thisalias = sq.getTable(i);
			query.append(thisalias.getTable().getName());
			query.append(' ');
			query.append(thisalias.getName());
		}
		QueryCondition condition = sq.getQueryCondition();

		if (condition != null)
			if (condition.isSignificant(0)) {
				query.append(" WHERE ");
				SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
				condition.accept(generator);
			}
		// end of query init

		String stringquery = query.toString();
		return this.executeWithRelaunch(new SQLExecution<JDBCRow>(stringquery) {

			@Override
			public JDBCRow executes() throws SQLException {
				PreparedStatement ps = connection.prepareStatement(stringquery);
				if (condition != null) {
					SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, 1);
					condition.accept(filler);
				}
				ResultSet rs = ps.executeQuery();
				return new JDBCRow(ps, rs, stringquery);
			}

		});

	}

	@Override
	public void MassiveInsertOnDB(MultipleTableRow multiplerow) {
		StoredTableSchema tableschema = multiplerow.getTableSchema();
		// ---------- Generate Insert

		StringBuffer query = new StringBuffer();
		query.append(" INSERT INTO ");
		query.append(tableschema.getName());
		query.append(" ( ");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(",");
			StoredFieldSchema thisfield = tableschema.getStoredField(i);
			query.append(thisfield.getName());
		}
		query.append(" ) ");
		query.append(" VALUES (");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(",");
			query.append("?");
		}
		query.append(" ) ");
		String stringquery = query.toString();
		// generates data

		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				boolean autocommit = connection.getAutoCommit();
				if (autocommit)
					connection.setAutoCommit(false);
				PreparedStatement ps = connection.prepareStatement(stringquery);
				for (int rowindex = 0; rowindex < multiplerow.getPayloadSize(); rowindex++) {

					for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
						StoredFieldSchema thisfieldschema = tableschema.getStoredField(i);

						boolean treated = false;
						if (thisfieldschema instanceof StringStoredField) {
							StringStoredField castedfieldschema = (StringStoredField) thisfieldschema;
							String payload = multiplerow.getPayload(rowindex, castedfieldschema);
							ps.setString(i + 1, payload);
							LOGGER.fine("JDBC Prepared Statement SetString " + (i + 1) + " " + payload);
							treated = true;
						}
						if (thisfieldschema instanceof TimestampStoredField) {
							TimestampStoredField castedfieldschema = (TimestampStoredField) thisfieldschema;
							Date thisdate = (Date) multiplerow.getPayload(rowindex, castedfieldschema);
							LOGGER.fine("JDBC Prepared Statement Timestamp " + (i + 1) + " " + thisdate);
							if (thisdate != null)
								ps.setTimestamp(i + 1, new Timestamp(thisdate.getTime()));
							if (thisdate == null)
								ps.setTimestamp(i + 1, null);
							treated = true;
						}
						if (thisfieldschema instanceof DecimalStoredField) {
							DecimalStoredField castedfieldschema = (DecimalStoredField) thisfieldschema;
							BigDecimal thisdecimal = (BigDecimal) multiplerow.getPayload(rowindex, castedfieldschema);
							LOGGER.fine("JDBC Prepared Statement BigDecimal " + (i + 1) + " " + thisdecimal);

							if (thisdecimal != null)
								ps.setBigDecimal(i + 1, thisdecimal);
							if (thisdecimal == null)
								ps.setBigDecimal(i + 1, null);
							treated = true;
						}

						if (thisfieldschema instanceof IntegerStoredField) {
							IntegerStoredField castedfieldschema = (IntegerStoredField) thisfieldschema;
							Integer thisinteger = (Integer) multiplerow.getPayload(rowindex, castedfieldschema);
							LOGGER.fine("JDBC Prepared Statement Integer " + (i + 1) + " " + thisinteger);

							if (thisinteger != null)
								ps.setInt(i + 1, thisinteger.intValue());
							if (thisinteger == null)
								ps.setNull(i + 1, java.sql.Types.INTEGER);
							treated = true;
						}
						if (thisfieldschema instanceof LargeBinaryStoredField) {
							LargeBinaryStoredField castedfieldschema = (LargeBinaryStoredField) thisfieldschema;
							SFile thisfile = (SFile) multiplerow.getPayload(rowindex, castedfieldschema);
							LOGGER.fine("JDBC Prepared Statement set File " + (i + 1) + " empty=" + thisfile.isEmpty());

							if (thisfile.isEmpty())
								ps.setNull(i + 1, java.sql.Types.BLOB);
							if (!thisfile.isEmpty())
								ps.setBlob(i + 1, new SerialBlob(thisfile.getContent()));
							treated = true;
						}

						if (!treated)
							throw new RuntimeException(String.format("object type not supported for %s.%s (%s)",
									tableschema.getName(), thisfieldschema.getName(), thisfieldschema.getClass()));

					}
					ps.addBatch();
				}
				ps.executeBatch();
				ps.close();
				connection.setAutoCommit(autocommit);
				return null;
			}
		});
	}

	@Override
	public void MassiveDeleteOnDB(MultipleTableRow rowstodelete) {
		StoredTableSchema tableschema = rowstodelete.getTableSchema();
		StringBuffer query = new StringBuffer();
		query.append(" DELETE FROM ");
		query.append(tableschema.getName());

		query.append(" WHERE ");
		SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
		rowstodelete.getQueryCondition(0).accept(generator);
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				boolean autocommit = connection.getAutoCommit();
				if (autocommit)
					connection.setAutoCommit(false);
				PreparedStatement ps = connection.prepareStatement(stringquery);
				// ------------ builds variable -----------------

				for (int rowindex = 0; rowindex < rowstodelete.getPayloadSize(); rowindex++) {

					// -- then condition to select correct line
					SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, 1);
					rowstodelete.getQueryCondition(rowindex).accept(filler);
					ps.addBatch();
				}
				ps.executeBatch();
				ps.close();
				connection.setAutoCommit(autocommit);
				return null;
			}

		});

	}

	@Override
	public void MassiveUpdateOnDB(MultipleTableRow multiplerow) {
		StoredTableSchema tableschema = multiplerow.getTableSchema();

		StringBuffer query = new StringBuffer();
		query.append(" UPDATE ");
		query.append(tableschema.getName());
		query.append(" SET ");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(" , ");
			StoredFieldSchema sf = tableschema.getStoredField(i);
			query.append(sf.getName());
			query.append(" = ? ");
		}
		query.append(" WHERE ");
		SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
		multiplerow.getQueryCondition(0).accept(generator);
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				boolean autocommit = connection.getAutoCommit();
				if (autocommit)
					connection.setAutoCommit(false);
				PreparedStatement ps = connection.prepareStatement(stringquery);
				// ------------ builds variable -----------------

				for (int rowindex = 0; rowindex < multiplerow.getPayloadSize(); rowindex++) {
					// -- first update fields
					for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {

						StoredFieldSchema thisfieldschema = tableschema.getStoredField(i);
						boolean treated = false;
						if (thisfieldschema instanceof StringStoredField) {
							StringStoredField castedfieldschema = (StringStoredField) thisfieldschema;
							ps.setString(i + 1, multiplerow.getPayload(rowindex, castedfieldschema));
							treated = true;
						}
						if (thisfieldschema instanceof TimestampStoredField) {
							TimestampStoredField castedfieldschema = (TimestampStoredField) thisfieldschema;
							Date thisdate = (Date) multiplerow.getPayload(rowindex, castedfieldschema);
							if (thisdate != null)
								ps.setTimestamp(i + 1, new Timestamp(thisdate.getTime()));
							if (thisdate == null)
								ps.setTimestamp(i + 1, null);
							treated = true;
						}
						if (thisfieldschema instanceof DecimalStoredField) {
							DecimalStoredField castedfieldschema = (DecimalStoredField) thisfieldschema;
							BigDecimal thisdecimal = (BigDecimal) multiplerow.getPayload(rowindex, castedfieldschema);
							if (thisdecimal != null)
								ps.setBigDecimal(i + 1, thisdecimal);
							if (thisdecimal == null)
								ps.setBigDecimal(i + 1, null);
							treated = true;
						}
						if (thisfieldschema instanceof IntegerStoredField) {
							IntegerStoredField castedfieldschema = (IntegerStoredField) thisfieldschema;
							Integer thisinteger = (Integer) multiplerow.getPayload(rowindex, castedfieldschema);
							if (thisinteger != null)
								ps.setInt(i + 1, thisinteger.intValue());
							if (thisinteger == null)
								ps.setNull(i + 1, java.sql.Types.INTEGER);
							treated = true;
						}

						if (!treated)
							throw new RuntimeException(String.format("object type not supported for %s.%s (%s)",
									tableschema.getName(), thisfieldschema.getName(), thisfieldschema.getClass()));

					}
					// -- then condition to select correct line
					SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, tableschema.getStoredFieldNumber() + 1);
					multiplerow.getQueryCondition(rowindex).accept(filler);
					ps.addBatch();
				}
				ps.executeBatch();
				ps.close();
				connection.setAutoCommit(autocommit);
				return null;
			}

		});

	}

	@Override
	public void insertOnDB(StoredTableRow row) {
		StoredTableSchema tableschema = row.getStoredTableSchema();
		StringBuffer query = new StringBuffer();
		query.append(" INSERT INTO ");
		query.append(tableschema.getName());
		query.append(" ( ");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(",");
			StoredFieldSchema thisfield = tableschema.getStoredField(i);
			query.append(thisfield.getName());
		}
		query.append(" ) ");
		query.append(" VALUES (");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(",");
			query.append("?");
		}
		query.append(" ) ");
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				PreparedStatement ps = connection.prepareStatement(stringquery);
				for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
					StoredFieldSchema thisfieldschema = tableschema.getStoredField(i);

					boolean treated = false;
					if (thisfieldschema instanceof StringStoredField) {
						StringStoredField castedfieldschema = (StringStoredField) thisfieldschema;
						String payload = row.getPayload(castedfieldschema);
						ps.setString(i + 1, payload);
						LOGGER.fine("JDBC Prepared Statement SetString " + (i + 1) + " " + payload);
						treated = true;
					}
					if (thisfieldschema instanceof TimestampStoredField) {
						TimestampStoredField castedfieldschema = (TimestampStoredField) thisfieldschema;
						Date thisdate = (Date) row.getPayload(castedfieldschema);
						LOGGER.fine("JDBC Prepared Statement Timestamp " + (i + 1) + " " + thisdate);
						if (thisdate != null)
							ps.setTimestamp(i + 1, new Timestamp(thisdate.getTime()));
						if (thisdate == null)
							ps.setTimestamp(i + 1, null);
						treated = true;
					}
					if (thisfieldschema instanceof DecimalStoredField) {
						DecimalStoredField castedfieldschema = (DecimalStoredField) thisfieldschema;
						BigDecimal thisdecimal = (BigDecimal) row.getPayload(castedfieldschema);
						LOGGER.fine("JDBC Prepared Statement BigDecimal " + (i + 1) + " " + thisdecimal);

						if (thisdecimal != null)
							ps.setBigDecimal(i + 1, thisdecimal);
						if (thisdecimal == null)
							ps.setBigDecimal(i + 1, null);
						treated = true;
					}

					if (thisfieldschema instanceof IntegerStoredField) {
						IntegerStoredField castedfieldschema = (IntegerStoredField) thisfieldschema;
						Integer thisinteger = (Integer) row.getPayload(castedfieldschema);
						LOGGER.fine("JDBC Prepared Statement Integer " + (i + 1) + " " + thisinteger);

						if (thisinteger != null)
							ps.setInt(i + 1, thisinteger.intValue());
						if (thisinteger == null)
							ps.setNull(i + 1, java.sql.Types.INTEGER);
						treated = true;
					}
					if (thisfieldschema instanceof LargeBinaryStoredField) {
						LargeBinaryStoredField castedfieldschema = (LargeBinaryStoredField) thisfieldschema;
						SFile thisfile = (SFile) row.getPayload(castedfieldschema);
						LOGGER.fine("JDBC Prepared Statement set File " + (i + 1) + " empty=" + thisfile.isEmpty());

						if (thisfile.isEmpty())
							ps.setNull(i + 1, java.sql.Types.BLOB);
						if (!thisfile.isEmpty())
							ps.setBlob(i + 1, new SerialBlob(thisfile.getContent()));
						treated = true;
					}

					if (!treated)
						throw new RuntimeException(String.format("object type not supported for %s.%s (%s)",
								tableschema.getName(), thisfieldschema.getName(), thisfieldschema.getClass()));

				}
				ps.execute();
				ps.close();
				return null;
			}
		});

	}

	@Override
	public void UpdateOnDB(UpdateQuery uq) {
		StoredTableSchema tableschema = uq.getRow().getStoredTableSchema();

		StringBuffer query = new StringBuffer();
		query.append(" UPDATE ");
		query.append(tableschema.getName());
		query.append(" SET ");
		for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
			if (i != 0)
				query.append(" , ");
			StoredFieldSchema sf = tableschema.getStoredField(i);
			query.append(sf.getName());
			query.append(" = ? ");
		}
		query.append(" WHERE ");
		SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
		uq.getCondition().accept(generator);
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				PreparedStatement ps = connection.prepareStatement(stringquery);
				// ------------ builds variable -----------------

				// -- first update fields
				for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {

					StoredFieldSchema thisfieldschema = tableschema.getStoredField(i);
					boolean treated = false;
					if (thisfieldschema instanceof StringStoredField) {
						StringStoredField castedfieldschema = (StringStoredField) thisfieldschema;
						String payload = uq.getRow().getPayload(castedfieldschema);
						ps.setString(i + 1, payload);
						LOGGER.info("JDBC preparedstatement update setString " + (i + 1) + "," + payload);
						treated = true;
					}
					if (thisfieldschema instanceof TimestampStoredField) {
						TimestampStoredField castedfieldschema = (TimestampStoredField) thisfieldschema;
						Date thisdate = (Date) uq.getRow().getPayload(castedfieldschema);
						if (thisdate != null)
							ps.setTimestamp(i + 1, new Timestamp(thisdate.getTime()));
						if (thisdate == null)
							ps.setTimestamp(i + 1, null);
						LOGGER.info("JDBC preparedstatement update setTimestamp " + (i + 1) + "," + thisdate);
						treated = true;
					}
					if (thisfieldschema instanceof DecimalStoredField) {
						DecimalStoredField castedfieldschema = (DecimalStoredField) thisfieldschema;
						BigDecimal thisdecimal = (BigDecimal) uq.getRow().getPayload(castedfieldschema);
						if (thisdecimal != null)
							ps.setBigDecimal(i + 1, thisdecimal);
						if (thisdecimal == null)
							ps.setBigDecimal(i + 1, null);
						LOGGER.info("JDBC preparedstatement update setDecimal " + (i + 1) + "," + thisdecimal);
						treated = true;
					}
					if (thisfieldschema instanceof IntegerStoredField) {
						IntegerStoredField castedfieldschema = (IntegerStoredField) thisfieldschema;
						Integer thisinteger = (Integer) uq.getRow().getPayload(castedfieldschema);
						if (thisinteger != null)
							ps.setInt(i + 1, thisinteger.intValue());
						if (thisinteger == null)
							ps.setNull(i + 1, java.sql.Types.INTEGER);
						LOGGER.info("JDBC preparedstatement update setInteger " + (i + 1) + "," + thisinteger);
						treated = true;
					}

					if (!treated)
						throw new RuntimeException(String.format("object type not supported for %s.%s (%s)",
								tableschema.getName(), thisfieldschema.getName(), thisfieldschema.getClass()));

				}
				// -- then condition to select correct line
				SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, tableschema.getStoredFieldNumber() + 1);
				uq.getCondition().accept(filler);

				ps.execute();
				ps.close();
				return null;
			}

		});

	}

	public void initMedaData() throws SQLException {
		this.metadata = connection.getMetaData();
		this.existingfields = new HashMap<String, HashMap<String, DatabaseColumnType>>();

	}

	@Override
	public boolean DoesObjectExist(StoredTableSchema object) {
		try {
			if (this.metadata == null)
				initMedaData();
			ResultSet rs = metadata.getTables(null, null, object.getName(), new String[] { "TABLE" });
			boolean tableexists = false;
			if (rs.next())
				tableexists = true;
			rs.close();
			return tableexists;
			/*
			 * PreparedStatement ps = connection.prepareStatement(stringquery);
			 * ps.executeQuery(); return true;
			 */
		} catch (Throwable t) {
			throw treatThrowable(t, "METADATA.GETTABLES");

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int DoesFieldExist(StoredTableSchema object, int fieldindex) {

		if (fieldindex >= object.getStoredFieldNumber())
			throw new RuntimeException(String.format("field index %d is outside of table %s range (%d)", fieldindex,
					object.getName(), object.getStoredFieldNumber()));
		try {
			if (this.metadata == null)
				initMedaData();
			if (this.existingfields.get(object.getName()) == null) {
				ResultSet fulltable = this.metadata.getColumns(null, null, object.getName(), null);
				HashMap<String, DatabaseColumnType> fieldlistfortable = new HashMap<String, DatabaseColumnType>();
				this.existingfields.put(object.getName().toUpperCase(), fieldlistfortable);
				while (fulltable.next()) {
					String columnname = fulltable.getString("COLUMN_NAME");
					String columntype = fulltable.getString("TYPE_NAME");
					int columnsize = fulltable.getInt("COLUMN_SIZE");
					int precision = fulltable.getInt("DECIMAL_DIGITS");
					String columndefault = fulltable.getString("COLUMN_DEF");
					fieldlistfortable.put(columnname,
							new DatabaseColumnType(columntype, columnsize, precision, columndefault));
				}
				fulltable.close();
			}

			StoredFieldSchema fieldtocheck = object.getStoredField(fieldindex);
			DatabaseColumnType columntype = existingfields.get(object.getName()).get(fieldtocheck.getName());

			if (columntype == null) {
				LOGGER.warning("[PERSISTENCE] --------------------------------- TABLE AUDIT ------------------------");
				LOGGER.warning("[PERSISTENCE]   Table name "+object.getName()+", field name = "+object.getStoredField(fieldindex).getName()+" is missing ");
				HashMap<String, DatabaseColumnType> fieldsforobject = this.existingfields.get(object.getName());
				Iterator<String> keyiterator = fieldsforobject.keySet().iterator();
				while (keyiterator.hasNext()) {
					String key = keyiterator.next();
					DatabaseColumnType columndetail = fieldsforobject.get(key);
					LOGGER.warning("[PERSISTENCE]                - "+key+", "+columndetail);
				}
				LOGGER.warning("[PERSISTENCE] -----------------------------------------------------------------------");
				return FIELD_NOT_PRESENT;
			}
			TestVisitor<Integer> fieldresult = fieldanalyzer.apply(columntype);
			return (Integer)(fieldtocheck.accept(fieldresult));

		} catch (Throwable e) {
			throw treatThrowable(e, "METADATA.GETGOLUMNS");

		}
	}

	@Override
	public int DoesIndexExist(StoredTableSchema object, StoredFieldSchema[] columns, String name) {
		name = name.toUpperCase();
		boolean found = false;

		try {
			if (this.metadata == null)
				initMedaData();
			ResultSet allindexes = metadata.getIndexInfo(connection.getCatalog(), null, object.getName(), false, false);
			while (allindexes.next()) {
				String thisindexname = allindexes.getString("INDEX_NAME").toUpperCase();
				if (thisindexname.equals(name)) {
					found = true;

					String thiscolumnname = allindexes.getString("COLUMN_NAME").toUpperCase();
					int ordinalposition = allindexes.getShort("ORDINAL_POSITION") - 1;
					if (ordinalposition < columns.length) {
						String correspondingcolumnname = columns[ordinalposition].getName().toUpperCase();
						if (!correspondingcolumnname.equals(thiscolumnname)) {
							allindexes.close();
							return PersistentStorage.INDEX_DIFFERENT;
						}
					} else {

						allindexes.close();
						return PersistentStorage.INDEX_DIFFERENT;
					}
				}
			}
			allindexes.close();
			if (found)
				return PersistentStorage.INDEX_OK;
			return PersistentStorage.INDEX_NOT_PRESENT;

		} catch (Throwable e) {
			throw treatThrowable(e, "metadata.getIndexInfo");
		}
	}

	@Override
	public void createObject(StoredTableSchema object) {

		StringBuffer query = new StringBuffer();
		query.append(" CREATE TABLE ");
		query.append(object.getName());
		query.append(" ( ");
		Visitor fielddefvisitor = fieldvisitorgenerator.apply(query);
		for (int i = 0; i < object.getStoredFieldNumber(); i++) {
			if (i > 0)
				query.append(',');
			StoredFieldSchema field = object.getStoredField(i);
			query.append(field.getName().toUpperCase());
			query.append(" ");
			field.accept(fielddefvisitor);
		}
		query.append(" ) ");
		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}

	}

	@Override
	public void createField(StoredTableSchema object, int fieldindex) {

		if (fieldindex >= object.getStoredFieldNumber())
			throw new RuntimeException(String.format("field index %d is outside of table %s range (%d)", fieldindex,
					object.getName(), object.getStoredFieldNumber()));
		StringBuffer query = new StringBuffer();
		query.append(" ALTER TABLE ");
		query.append(object.getName());
		query.append(" ADD COLUMN ");
		StoredFieldSchema field = object.getStoredField(fieldindex);
		query.append(field.getName().toUpperCase());
		query.append(" ");
		Visitor fielddefvisitor = fieldvisitorgenerator.apply(query);
		field.accept(fielddefvisitor);
		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}

	}

	@Override
	public void extendField(StoredTableSchema object, int fieldindex) {
		if (fieldindex >= object.getStoredFieldNumber())
			throw new RuntimeException(String.format("field index %d is outside of table %s range (%d)", fieldindex,
					object.getName(), object.getStoredFieldNumber()));
		StringBuffer query = new StringBuffer();
		query.append(" ALTER TABLE ");
		query.append(object.getName());
		query.append(" MODIFY COLUMN ");
		query.append(object.getStoredField(fieldindex).getName().toUpperCase());
		query.append(" ");
		Visitor fielddefvisitor = fieldvisitorgenerator.apply(query);
		object.getStoredField(fieldindex).accept(fielddefvisitor);
		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}
	}

	@Override
	public void createSearchIndex(String name, StoredTableSchema object, StoredFieldSchema[] fields, boolean unique) {
		StringBuffer query = new StringBuffer();
		query.append(" CREATE ");
		if (unique)
			query.append(" UNIQUE ");
		query.append(" INDEX ");
		query.append(name.toUpperCase());
		query.append(" ON ");
		query.append(object.getName().toUpperCase());
		query.append(" ( ");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				query.append(" , ");
			query.append(fields[i].getName().toUpperCase());
		}
		query.append(" ) ");

		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}

	}

	@Override
	public void setAutoCommit(boolean autocommit) {
		String pseudoquery = "# SET AUTOCOMMIT TO " + autocommit + "#";
		try {
			connection.setAutoCommit(autocommit);

			LOGGER.info("[PERSISTENCE] " + pseudoquery);
		} catch (Throwable e) {

			throw treatThrowable(e, pseudoquery);
		}
	}

	@Override
	public void startTransaction() {
		String stringquery = " START TRANSACTION ";

		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.finer("[PERSISTENCE] " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}
	}

	@Override
	public void commitTransaction() {
		String pseudoquery = "# COMMIT #";

		try {
			connection.commit();
			LOGGER.finer("[PERSISTENCE] " + pseudoquery);
		} catch (Throwable e) {
			throw treatThrowable(e, pseudoquery);
		}
	}

	@Override
	public void rollbackTransaction() {
		String pseudoquery = "# COMMIT #";
		try {
			connection.rollback();
			LOGGER.finer("[PERSISTENCE] " + pseudoquery);

		} catch (Throwable e) {

			throw treatThrowable(e, pseudoquery);
		}
	}

	@Override
	public void closeConnections() {
		try {
			LOGGER.severe("Shutting down connection");
			connection.rollback();
			connection.close();
			LOGGER.severe("database connection gracefully closed");
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
			LOGGER.severe("embedded database engine shutdown");
		} catch (Throwable e) {
			throw treatThrowable(e, "connection.close()");
		}
	}

	@Override
	public void LimitedFieldUpdateOnDB(LimitedFieldsUpdateQuery limitedfieldsupdatequery) {
		StringBuffer query = new StringBuffer();
		query.append(" UPDATE ");
		query.append(limitedfieldsupdatequery.getTableSchema().getName());
	
		query.append(" SET ");
		for (int i=0;i<limitedfieldsupdatequery.getUpdatedFieldsNumber();i++) {
			if (i>0) query.append(" , ");
			SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
			limitedfieldsupdatequery.getFieldUpdateAt(i).accept(generator);
			
		}
		
		query.append(" WHERE  ");
		SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
		limitedfieldsupdatequery.getCondition().accept(generator);
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				PreparedStatement ps = connection.prepareStatement(stringquery);
				SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, 1);
				for (int i=0;i<limitedfieldsupdatequery.getUpdatedFieldsNumber();i++) {
					limitedfieldsupdatequery.getFieldUpdateAt(i).accept(filler);
				}
				limitedfieldsupdatequery.getCondition().accept(filler);
				ps.execute();
				ps.close();
				return null;
			}

		});
	}
	
	
	@Override
	public void DeleteOnDB(DeleteQuery dq) {
		StoredTableSchema tableschema = dq.getTableSchema();

		StringBuffer query = new StringBuffer();
		query.append(" DELETE FROM ");
		query.append(tableschema.getName());
		query.append(" WHERE ");
		SQLQueryConditionGenerator generator = new SQLQueryConditionGenerator(query);
		dq.getCondition().accept(generator);
		String stringquery = query.toString();
		this.executeWithRelaunch(new SQLExecution<Object>(stringquery) {

			@Override
			public Object executes() throws SQLException {
				PreparedStatement ps = connection.prepareStatement(stringquery);
				// ------------ builds variable -----------------

				// -- then condition to select correct line
				SQLQueryPSFiller filler = new SQLQueryPSFiller(ps, 1);
				dq.getCondition().accept(filler);

				ps.execute();
				ps.close();
				return null;
			}

		});

	}

	public void cleanup() {

		connection = null;
	}

	@Override
	public void finalize() {
		try {
			cleanup();
		} finally {
			try {
				super.finalize();
			} catch (Throwable t) {
				LOGGER.warning("Unplanned error " + t.getMessage() + " in finalizing standard JDBC Storage");
			}
		}
	}

	@Override
	public void dropIndex(String name) {
		StringBuffer query = new StringBuffer();
		query.append(" DROP INDEX ");

		query.append(name.toUpperCase());

		String stringquery = query.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] Model update: " + stringquery);
		} catch (Throwable e) {
			throw treatThrowable(e, stringquery);
		}

	}

	@Override
	public void refreshConnection(Connection connection) {
		this.connection = connection;

	}

	@Override
	public void technicalInit() {

		// declaredfirst object

		StoredTableSchema gsequences = new StoredTableSchema("GSEQUENCE");
		StringStoredField seqname = new StringStoredField("SEQNAME", gsequences, 64);
		gsequences.addField(seqname);
		gsequences.addField(new IntegerStoredField("SEQVALUE", gsequences, new Integer(0)));

		// if does not exist, create table.

		boolean sequenceexists = this.DoesObjectExist(new StoredTableSchema("GSEQUENCE"));
		if (!sequenceexists) {
			LOGGER.info("Creating sequence artefact");

			this.createObject(gsequences);

			// this.createSearchIndex("GSEQUENCE_NAME_INDEX", gsequences, new int[]{0},
			// false);
		}
		String SEQUENCE_INDEX_NAME = "GSEQUENCE_SEQNAME";
		int sequenceindexexists = this.DoesIndexExist(gsequences, new StoredFieldSchema[] { seqname },
				SEQUENCE_INDEX_NAME);
		if (sequenceindexexists == BaseJDBCStorage.INDEX_NOT_PRESENT) {
			this.createSearchIndex(SEQUENCE_INDEX_NAME, gsequences, new StoredFieldSchema[] { seqname }, true);
		}
	}

	@Override
	public boolean isSequenceExisting(String sequencename) {

		String query = "SELECT * FROM GSEQUENCE WHERE SEQNAME = '" + sequencename.toUpperCase() + "'";

		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return true;
			return false;
		} catch (Throwable e) {
			throw treatThrowable(e, query);
		}

	}

	@Override
	public void createSequence(String sequencename, int firstvalue) {
		// this is specific Derby. Needs to be put in a specific Derby subclass
		StringBuffer stringbuffer = new StringBuffer();
		stringbuffer.append("  INSERT INTO GSEQUENCE(SEQNAME,SEQVALUE) VALUES('");
		stringbuffer.append(sequencename.toUpperCase());
		stringbuffer.append("',");
		stringbuffer.append(firstvalue);
		stringbuffer.append(")");
		String stringquery = stringbuffer.toString();
		try {
			PreparedStatement ps = connection.prepareStatement(stringquery);
			boolean result = ps.execute();
			ps.close();
			LOGGER.info("[PERSISTENCE] query executed (" + result + "):  " + stringquery);
		} catch (Throwable e) {
			try {
				if (!connection.isValid(5000)) {
					throw treatThrowable(e, stringquery);
				} else {
					// LOGGER.info("[PERSISTENCE] query executed in error: "+stringquery);
					// LOGGER.info("[PERSISTENCE] bypassed error in sequence creation :
					// "+e.getMessage());
					throw treatThrowable(e, stringquery);
				}
			} catch (Throwable f) {
				throw treatThrowable(f, stringquery);
			}
		}
	}

	@Override
	public int getNextValue(String sequencename) {

		StringBuffer buffer = new StringBuffer();
		buffer.append("SELECT SEQVALUE FROM GSEQUENCE WHERE SEQNAME = '");
		buffer.append(sequencename.toUpperCase());
		buffer.append("' FOR UPDATE");
		String stringquery = buffer.toString();

		Integer sequence = this.executeWithRelaunch(new SQLExecution<Integer>(stringquery) {

			@Override
			public Integer executes() throws SQLException {
				boolean autocommit = connection.getAutoCommit();
				this.requiresrollback = true;
				this.forceautocommitifrollaback = autocommit;
				if (autocommit) {
					this.forceautocommitifrollaback = true;

				}

				if (autocommit)
					connection.setAutoCommit(false);
				PreparedStatement ps = connection.prepareStatement(stringquery);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					int value = rs.getInt(1);
					rs.close();
					StringBuffer update = new StringBuffer("UPDATE GSEQUENCE SET SEQVALUE = ");
					update.append(value + 1);
					update.append(" WHERE SEQNAME = '");
					update.append(sequencename.toUpperCase());
					update.append("'");
					PreparedStatement updateps = connection.prepareStatement(update.toString());
					updateps.execute();
					updateps.close();
					connection.commit();
					connection.setAutoCommit(autocommit);
					return value;

				}
				return null;
			}

		});
		if (sequence != null)
			return sequence.intValue();
		throw new RuntimeException("No value returned for sequence " + sequencename);

	}

	/**
	 * A simple class to store the definition of a field as extracted from the JDBC
	 * metadata query on table columns
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 * @since 1.6
	 */
	public static class DatabaseColumnType {
		private String type;
		private int length;
		private int scale;
		private String defaultvalue;

		/**
		 * @return the type (may be specific per database)
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return the length (may not always be relevant)
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @return the precision (may not always be relevant)
		 */
		public int getScale() {
			return scale;
		}
		
		/**
		 * @return the default value as defined in the database. If String, is inserted
		 *         inside simple quotes
		 */
		public String getDefaultvalue() {
			return defaultvalue;
		}

		/**
		 * Creates a database column type
		 * 
		 * @param type         database type
		 * @param length       length (if relevant for the type)
		 * @param scale    	scale (if relevant for the type)
		 * @param defaultvalue default value if defined, may be null
		 */
		public DatabaseColumnType(String type, int length, int scale, String defaultvalue) {
			super();
			this.type = type;
			this.length = length;
			this.scale = scale;
			this.defaultvalue = defaultvalue;
		}

		@Override
		public String toString() {
			return type+";LEN="+length+";SCALE="+scale+";DEFAULT="+defaultvalue;
		}

		
	}
}
