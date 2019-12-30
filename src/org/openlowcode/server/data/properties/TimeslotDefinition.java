/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;
import java.util.Date;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.TimestampStoredField;

/**
 * The definition of the timeslot property of an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class TimeslotDefinition<E extends DataObject<E> & TimeslotInterface<E>>
		extends DataObjectPropertyDefinition<E> {
	private TimestampStoredField starttime;
	private TimestampStoredField endtime;
	private UniqueidentifiedDefinition<E> dependentdefinitionuniqueidentified;

	public TimeslotDefinition(DataObjectDefinition<E> parentobject) {
		super(parentobject, "TIMESLOT");
		starttime = new TimestampStoredField("STARTTIME", null, new Date());
		this.addFieldSchema(starttime);
		endtime = new TimestampStoredField("ENDTIME", null, new Date());
		this.addFieldSchema(endtime);
		StoredTableIndex startdateindex = new StoredTableIndex("STARTTIME");
		startdateindex.addStoredFieldSchame(starttime);
		this.addIndex(startdateindex);
		StoredTableIndex enddateindex = new StoredTableIndex("ENDTIME");
		enddateindex.addStoredFieldSchame(endtime);
		this.addIndex(enddateindex);

	}

	/**
	 * sets the definition of the dependent property unique identified
	 * 
	 * @param dependentdefinitionuniqueidentified dependent property unique
	 *                                            identified definition
	 */
	public void setDependentDefinitionUniqueidentified(
			UniqueidentifiedDefinition<E> dependentdefinitionuniqueidentified) {
		this.dependentdefinitionuniqueidentified = dependentdefinitionuniqueidentified;
	}

	/**
	 * gets the definition of the dependent property unique identified
	 * 
	 * @return the dependent property definition unique identified
	 */
	public UniqueidentifiedDefinition<E> getDependentDefinitionUniqueidentified() {
		return this.dependentdefinitionuniqueidentified;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalues = new FieldSchemaForDisplay[2];

		returnvalues[0] = new FieldSchemaForDisplay<E>("Start Time", "Start of the timeslot period", starttime, false,
				false, 605, 30, this.parentobject);
		returnvalues[1] = new FieldSchemaForDisplay<E>("End Time", "End of the timeslot period", endtime, false, false,
				600, 30, this.parentobject);

		return returnvalues;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columnattributes == null)
			throw new RuntimeException("At least one attribute required for Timeslot: STARTTIME or ENDTIME");
		if (columnattributes != null)
			if (columnattributes.length == 0)
				throw new RuntimeException("At least one attribute required for creationlog: DATE or USER");

		if ("STARTTIME".equals(columnattributes[0])) {

			String format = null;
			if (columnattributes.length > 1) {
				format = columnattributes[1];
			}
			return new TimeslotStartTimeFlatFileLoader<E>(objectdefinition, this, format, propertyextractor);

		}
		if ("ENDTIME".equals(columnattributes[0])) {

			String format = null;
			if (columnattributes.length > 1) {
				format = columnattributes[1];
			}
			return new TimeslotEndTimeFlatFileLoader<E>(objectdefinition, this, format, propertyextractor);

		}
		throw new RuntimeException(
				"First attribute for timeslot should be  STARTIME or ENDTIME, not " + columnattributes[0]);
	}

	@Override
	public String[] getLoaderFieldList() {
		String[] fields = new String[2];
		fields[0] = "STARTTIME";
		fields[1] = "ENDTIME";
		return fields;

	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		if (name.equals("STARTTIME")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&STARTTIME";
			returntable[1] = "OPTIONAL";
			returntable[2] = "2017.02.28";
			returntable[3] = "Optional parameter: specific java simpledateformat\n (e.g. \"yyyy.MM.dd G 'at' HH:mm:ss z\" for 2001.07.04 AD at 12:08:56 PDT ) ,\n definition at https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
			return returntable;
		}
		if (name.equals("ENDTIME")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&ENDTIME";
			returntable[1] = "OPTIONAL";
			returntable[2] = "2017.02.28";
			returntable[3] = "Optional parameter: specific java simpledateformat\n (e.g. \"yyyy.MM.dd G 'at' HH:mm:ss z\" for 2001.07.04 AD at 12:08:56 PDT ) ,\n definition at https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
			return returntable;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Timeslot<E>(this, parentpayload);
	}

}
