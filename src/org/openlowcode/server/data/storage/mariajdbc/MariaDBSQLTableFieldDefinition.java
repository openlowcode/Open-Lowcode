/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.mariajdbc;

import java.text.SimpleDateFormat;

import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.server.data.storage.StoredFieldSchema.Visitor;

/**
 * the visitor to implement the Maria DB storage
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MariaDBSQLTableFieldDefinition implements Visitor {
	private static SimpleDateFormat mariadbtimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private StringBuffer buffer;

	/**
	 * creates a single use visitor
	 * 
	 * @param buffer buffer to be used
	 */
	public MariaDBSQLTableFieldDefinition(StringBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void visit(StringStoredField stringfield) {

		// TODO to workaround 64K row limit, declare texts bigger than 500 CHAR as large
		// text
		buffer.append(" VARCHAR(");
		buffer.append(stringfield.getMaximumLength());
		buffer.append(") ");
		if (stringfield.defaultValueAtColumnCreation() != null) {
			buffer.append(" DEFAULT '");
			buffer.append(stringfield.defaultValueAtColumnCreation().replace("'", "''"));
			buffer.append("' ");
		}
	}

	@Override
	public void visit(TimestampStoredField timestampfield) {

		buffer.append(" TIMESTAMP ");
		if (timestampfield.defaultValueAtColumnCreation() != null) {
			buffer.append(" DEFAULT '");
			buffer.append(mariadbtimestamp.format(timestampfield.defaultValueAtColumnCreation()));
			buffer.append("'");

		} else {
			buffer.append(" NULL");
		}

	}

	@Override
	public void visit(DecimalStoredField decimalStoredField) {

		buffer.append(" DECIMAL(");
		buffer.append(decimalStoredField.getPrecision());
		buffer.append(",");
		buffer.append(decimalStoredField.getScale());

		buffer.append(") ");

	}

	@Override
	public void visit(IntegerStoredField integerStoredField) {

		buffer.append(" INTEGER");
		if (integerStoredField.defaultValueAtColumnCreation() != null) {
			buffer.append(" DEFAULT ");
			buffer.append(integerStoredField.defaultValueAtColumnCreation().intValue());
			buffer.append(" ");

		}
	}

	@Override
	public void visit(LargeBinaryStoredField largebinarystoredfield) {

		buffer.append(" LONGBLOB ");
		if (largebinarystoredfield.getMaxFileSize() > 0) {
			if (largebinarystoredfield.getMaxFileSize() >= 1024 * 1024 * 4)
				throw new RuntimeException(" field " + largebinarystoredfield.getName()
						+ " is too long for MariaDB 10.2.X, max size is 2GB, size specified = "
						+ largebinarystoredfield.getMaxFileSize() + "Kb");

		}

	}

}
