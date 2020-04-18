/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.standardjdbc;

import java.util.logging.Logger;

import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.server.data.storage.standardjdbc.BaseJDBCStorage.DatabaseColumnType;

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
public abstract class StandardJDBCTableFieldCheck
		implements
		StoredFieldSchema.TestVisitor<Integer> {
	private static Logger logger = Logger.getLogger(StandardJDBCTableFieldCheck.class.getName());

	/**
	 * @return the type for varchar as given by JDCB Metadata (please do not use a
	 *         synonym)
	 */
	public abstract String getStringDBType();

	/**
	 * @return the type for timestamp as given by JDCB Metadata (please do not use a
	 *         synonym)
	 */
	public abstract String getTimestampDBType();

	/**
	 * @return the type for decimal as given by JDCB Metadata (please do not use a
	 *         synonym)
	 */
	public abstract String getDecimalDBType();

	/**
	 * @return the type for Integer as given by JDCB Metadata (please do not use a
	 *         synonym)
	 */
	public abstract String getIntegerDBType();

	/**
	 * @return the type for large binary (storage of up to ~1GB file) as given by
	 *         JDCB Metadata (please do not use a synonym)
	 */
	public abstract String getBinaryDBType();

	/**
	 * @return if true, the JDBC metadata field default value column escapes string
	 *         inside single quote (e.g. 'Y'), if false, the metadata gives just the
	 *         string
	 */
	public abstract boolean MetaDataEscapesDefaultString();

	private DatabaseColumnType columntype;

	/**
	 * Creates a TableFieldCheck object (to be used only once)
	 * 
	 * @param columntype type of column to perform the check on
	 */
	public StandardJDBCTableFieldCheck(BaseJDBCStorage.DatabaseColumnType columntype) {
		this.columntype = columntype;
	}

	@Override
	public Integer visit(StringStoredField stringfield) {
		if (!getStringDBType().equals(columntype.getType())) {
			logger.warning("      Incompatible field type for " + stringfield.getName() + ", in db = "
					+ columntype.getType() + ", in model = " + getStringDBType());
			return PersistentStorage.FIELD_INCOMPATIBLE;
		}
		if (columntype.getLength() > stringfield.getMaximumLength()) {
			logger.warning("for field " + stringfield.getName() + "length in db = " + columntype.getLength()
					+ ", length in model = " + stringfield.getMaximumLength());
			return PersistentStorage.FIELD_UPDATABLE;
		}

		if (columntype.getLength() < stringfield.getMaximumLength()) {
			logger.warning("for field " + stringfield.getName() + "length in db = " + columntype.getLength()
					+ ", length in model = " + stringfield.getMaximumLength());
			return PersistentStorage.FIELD_UPDATABLE;
		}

		if (columntype.getDefaultvalue() == null)
			if (stringfield.defaultValue() == null)
				return PersistentStorage.FIELD_OK;
		if (columntype.getDefaultvalue() == null)
			if (stringfield.defaultValue().length() == 0)
				return PersistentStorage.FIELD_OK;
		if (stringfield.defaultValue() == null)
			if (columntype.getDefaultvalue().length() == 0)
				return PersistentStorage.FIELD_OK;
		String defaultvalueinmodel = stringfield.defaultValueAtColumnCreation();
		if (this.MetaDataEscapesDefaultString())  defaultvalueinmodel = "'" + defaultvalueinmodel.replace("'", "''") + "'";
		if (columntype.getDefaultvalue() != null)
			if (stringfield.defaultValue() != null)
				if (columntype.getDefaultvalue().equals(defaultvalueinmodel)) {
					return PersistentStorage.FIELD_OK;
				}
		logger.warning("Incompatible default value for field " + stringfield.getName() + ", default value in db = "
				+ columntype.getDefaultvalue() + ", in model = " + defaultvalueinmodel);

		return PersistentStorage.FIELD_UPDATABLE;

	}

	@Override
	public Integer visit(TimestampStoredField timestampfield) {

		if (!getTimestampDBType().equals(columntype.getType()))
			return PersistentStorage.FIELD_INCOMPATIBLE;
		return PersistentStorage.FIELD_OK;
	}

	@Override
	public Integer visit(DecimalStoredField decimalStoredField) {
		if (!getDecimalDBType().equals(columntype.getType())) {
			logger.warning("      Incompatible field type for " + decimalStoredField.getName() + ", in db = "
					+ columntype.getType() + ", in model = " + getDecimalDBType());
			return PersistentStorage.FIELD_INCOMPATIBLE;
		}
		int precision = decimalStoredField.getPrecision();
		int scale = decimalStoredField.getScale();
		if (precision < columntype.getLength()) {
			logger.warning("Incompatible format for field " + decimalStoredField.getName()
					+ ", for precision default value in db = " + columntype.getLength() + ", in model = "
					+ precision);
			return PersistentStorage.FIELD_UPDATABLE;
		}

		if (scale < columntype.getScale()) {
			logger.warning("Incompatible format for field " + decimalStoredField.getName()
					+ ", for precision default value in db = " + columntype.getScale() + ", in model = " + scale);

			return PersistentStorage.FIELD_UPDATABLE;
		}

		if (precision > columntype.getLength()) {
			logger.warning("Incompatible format for field " + decimalStoredField.getName()
					+ ", for precision default value in db = " + columntype.getDefaultvalue() + ", in model = "
					+ precision);
			return PersistentStorage.FIELD_UPDATABLE;
		}

		if (scale > columntype.getScale()) {
			logger.warning("Incompatible format for field " + decimalStoredField.getName()
					+ ", for precision default value in db = " + columntype.getScale() + ", in model = " + scale);

			return PersistentStorage.FIELD_UPDATABLE;
		}

		return PersistentStorage.FIELD_OK;
	}

	@Override
	public Integer visit(IntegerStoredField integerStoredField) {

		if (!getIntegerDBType().equals(columntype.getType())) {
			logger.warning("      Incompatible field type for " + integerStoredField.getName() + ", in db = "
					+ columntype.getType() + ", in model = " + getIntegerDBType());
			return PersistentStorage.FIELD_INCOMPATIBLE;
		}

		Integer defaultvalue = integerStoredField.defaultValueAtColumnCreation();
		if (defaultvalue == null)
			if (columntype.getDefaultvalue() == null)
				return PersistentStorage.FIELD_OK;
		if (defaultvalue != null)
			if (columntype.getDefaultvalue() != null)
				if (("" + defaultvalue.intValue()).equals(columntype.getDefaultvalue()))
					return PersistentStorage.FIELD_OK;
		logger.warning(
				"Incompatible default value for field " + integerStoredField.getName() + ", default value in db = "
						+ defaultvalue.intValue() + ", in model = " + columntype.getDefaultvalue());
		return PersistentStorage.FIELD_UPDATABLE;
	}

	@Override
	public Integer visit(LargeBinaryStoredField largebinarystoredfield) {
		if (!getBinaryDBType().equals(columntype.getType())) {
			logger.warning("      Incompatible field type for " + largebinarystoredfield.getName() + ", in db = "
					+ columntype.getType() + ", in model = " + getBinaryDBType());
			return PersistentStorage.FIELD_INCOMPATIBLE;
		}
		int length = largebinarystoredfield.getMaxFileSize();
		// if length at zero uses the default database value, and so no need to check
		if (length > 0) {
			if (length < columntype.getLength()) {
				logger.warning("      Incompatible field length for " + largebinarystoredfield.getName() + ", in db = "
						+ columntype.getLength() + ", in model = " + length);
				return PersistentStorage.FIELD_UPDATABLE;
			}
			if (length > columntype.getLength()) {
				logger.warning("      Smaller length for " + largebinarystoredfield.getName() + ", in db = "
						+ columntype.getLength() + ", in model = " + length);
				return PersistentStorage.FIELD_UPDATABLE;
			}
		}
		return PersistentStorage.FIELD_OK;
	}

}
