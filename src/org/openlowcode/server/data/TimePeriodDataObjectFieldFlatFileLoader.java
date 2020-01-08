/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.data.TimePeriod.PeriodType;

/**
 * a flat file loader to load the content of a time period field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object for the loader
 */
public class TimePeriodDataObjectFieldFlatFileLoader<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> objectdefinition;
	private String name;
	@SuppressWarnings("unused")
	private PeriodType periodtype;

	/**
	 * Creates a loader for this attribute
	 * 
	 * @param objectdefinition definition of the object
	 * @param columnattributes additional attributes for the loader (not used)
	 * @param name             name of the loader
	 * @param periodtype       type of Time Period expected
	 */
	public TimePeriodDataObjectFieldFlatFileLoader(DataObjectDefinition<E> objectdefinition, String[] columnattributes,
			String name, TimePeriod.PeriodType periodtype) {
		this.objectdefinition = objectdefinition;
		this.name = name;
		this.periodtype = periodtype;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		@SuppressWarnings("unchecked")
		DataObjectField<?, E> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof TimePeriodDataObjectField))
			throw new RuntimeException(
					"Expected field " + name + " would be of type TimePeriodDataObjectField but in reality, it is "
							+ field.getClass().toString());
		@SuppressWarnings("unchecked")
		TimePeriodDataObjectField<E> timeperiodfield = (TimePeriodDataObjectField<E>) field;
		TimePeriod oldtimeperiod = timeperiodfield.getValue();
		// ---------------------- process null ---------------------
		if (value == null) {
			if (FlatFileLoader.isTheSame(oldtimeperiod, null)) {
				return false;
			} else {
				timeperiodfield.setValue((TimePeriod) null);
				return true;
			}
		}
		if (value instanceof String) {
			String stringvalue = (String) value;
			TimePeriod parsedperiod = TimePeriod.generateFromString(stringvalue);
			if (stringvalue.length() > 0)
				if (parsedperiod == null)
					throw new RuntimeException(
							"Invalid value " + value + " for field " + name + " , consult TimePeriod field ");
			if (FlatFileLoader.isTheSame(oldtimeperiod, parsedperiod)) {
				return false;
			} else {
				timeperiodfield.setValue(parsedperiod);
				return true;
			}
		}
		throw new RuntimeException("For field '" + this.name + "', received an object of unsupported type = "
				+ value.getClass() + " value = " + value);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectField<?, E> field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof TimePeriodDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type ChoiceDataObjectField but in reality, it is " + field.getClass().toString());
		@SuppressWarnings("rawtypes")
		TimePeriodDataObjectField<E> timeperiodfield = (TimePeriodDataObjectField) field;
		cell.setCellValue((timeperiodfield.getValue() == null ? "" : timeperiodfield.getValue().encode()));
		return false;
	}

}
