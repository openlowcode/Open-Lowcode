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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.JoinQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryConditionAlways;
import org.openlowcode.server.data.storage.QueryConditionNever;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.tools.structure.Choice;
import org.openlowcode.tools.structure.ObjectIdInterface;

/**
 * A visitor for query condition generating prepared statement setter
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SQLQueryPSFiller implements QueryCondition.Visitor {
	private static Logger LOGGER = Logger.getLogger(SQLQueryPSFiller.class.getName());
	private PreparedStatement ps;
	private int counter;

	/**
	 * Creates a new queryfiller for the prepared statement
	 * 
	 * @param ps          prepared statement
	 * @param nextcounter counter to start for PreparedStatement setter
	 */
	public SQLQueryPSFiller(PreparedStatement ps, int nextcounter) {
		this.ps = ps;
		this.counter = nextcounter;
	}

	@Override
	public <E extends Object> void visit(SimpleQueryCondition<E> simplequerycondition) {
		try {
			if (simplequerycondition.getPayload() != null) {
				boolean found = false;
				if (simplequerycondition.getPayload() instanceof String) {
					String payload = (String) simplequerycondition.getPayload();
					ps.setString(counter, payload);
					LOGGER.info("JDBC preparedstatement setString " + counter + "," + payload);
					counter++;
					found = true;

				}

				if (simplequerycondition.getPayload() instanceof Choice) {
					Choice payload = (Choice) simplequerycondition.getPayload();
					ps.setString(counter, payload.getStorageCode());
					LOGGER.info("JDBC preparedstatement setString " + counter + "," + payload.getStorageCode());
					counter++;
					found = true;

				}

				if (simplequerycondition.getPayload() instanceof TimePeriod) {
					TimePeriod payload = (TimePeriod) simplequerycondition.getPayload();
					ps.setString(counter, payload.encode());
					LOGGER.info(
							"JDBC prepared statement setString for TimerPeriod " + counter + "," + payload.encode());
					counter++;
					found = true;
				}

				if (simplequerycondition.getPayload() instanceof Date) {
					Date payload = (Date) simplequerycondition.getPayload();

					ps.setTimestamp(counter, new Timestamp(payload.getTime()));
					LOGGER.info("JDBC preparedstatement setDate " + counter + "," + payload);
					counter++;
					found = true;

				}
				if (simplequerycondition.getPayload() instanceof ObjectIdInterface) {
					ObjectIdInterface id = (ObjectIdInterface) simplequerycondition.getPayload();
					ps.setString(counter, id.getId());
					LOGGER.info("JDBC preparedstatement setDataObjectId " + counter + "," + id);
					counter++;
					found = true;

				}

				if (simplequerycondition.getPayload() instanceof SFile) {
					SFile binarycontent = (SFile) simplequerycondition.getPayload();
					if (binarycontent.isEmpty()) {
						ps.setNull(counter, java.sql.Types.BLOB);
						LOGGER.info("JDBC preparedstatement set binary " + counter + ", NULL");
					} else {
						ps.setBlob(counter, binarycontent.getStream(), binarycontent.getLength());
						LOGGER.info("JDBC preparedstatement set binary " + counter + ", contentlength = "
								+ binarycontent.getLength());

					}
					counter++;
					found = true;
				}
				if (simplequerycondition.getPayload() instanceof Integer) {
					Integer integercontent = (Integer) simplequerycondition.getPayload();
					ps.setInt(counter, integercontent.intValue());
					LOGGER.info("JDBC preparedstatement setInteger" + counter + "," + integercontent);

					counter++;
					found = true;
				}
				if (!found)
					throw new RuntimeException("class not managed for simple query condition " + simplequerycondition
							+ ", " + simplequerycondition.getPayload());
			} else {
				LOGGER.info("JDBC preparedStatement - null content for "+counter+" - "+simplequerycondition.getField());
			}
		} catch (SQLException e) {
			throw new RuntimeException(String.format(
					"Persistence issue for simple query condition " + simplequerycondition + " original message = %s",
					e.getMessage()));
		}
	}

	@Override
	public <E> void visit(JoinQueryCondition<E> joinquerycondition) {
		// do nothing

	}

	@Override
	public void visit(AndQueryCondition andquerycondition) {
		QueryCondition[] andconditions = andquerycondition.returnAllConditions();

		for (int i = 0; i < andconditions.length; i++) {

			if (andconditions[i] != null)
				andconditions[i].accept(this);
		}

	}

	@Override
	public void visit(OrQueryCondition orQueryCondition) {
		QueryCondition[] orconditions = orQueryCondition.returnAllConditions();

		for (int i = 0; i < orconditions.length; i++) {

			if (orconditions[i] != null)
				orconditions[i].accept(this);
		}

	}

	@Override
	public void visit(QueryConditionAlways always) {
		// do nothing

	}

	@Override
	public void visit(QueryConditionNever never) {
		// do nothing

	}

}
