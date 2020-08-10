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


import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.openlowcode.server.data.loader.FlatFileExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * A field to load dates. It has two options:
 * <ul>
 * <li>if lenient, will not blow up the line if the format is wrong</li>
 * <li>timeedit: if true, will feed data and time information, if false, only
 * date</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class DateDataObjectFieldFlatFileLoader<E extends DataObject<E>>
		extends
		FlatFileLoaderColumn<E> {
	private final static Logger logger = Logger.getLogger(DateDataObjectFieldFlatFileLoader.class.getName());
	private String name;
	private DateTimeFormatter format;
	private String formatasstring;
	private boolean timeedit;
	private CellStyle cellStyle;

	/**
	 * creates a date data object field flat file loader
	 * 
	 * @param definition definition of the data object
	 * @param arguments  arguments of the flat file loader
	 * @param name       name of the field
	 * @param timeedit   true if time edit
	 */
	public DateDataObjectFieldFlatFileLoader(
			DataObjectDefinition<E> definition,
			String[] arguments,
			String name,
			boolean timeedit) {
		this.name = name;
		if (arguments.length > 1)
			throw new RuntimeException("For Date, only one extra argument should be provided for element " + name
					+ " in object " + definition.getName());
		this.formatasstring = (arguments.length == 1 ? arguments[0] : null);
		this.format = FlatFileLoader.generateFormat(formatasstring,
				"element " + name + " for object " + definition.getName());

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		DataObjectField<?, E> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof DateDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type DateDataObjectField but in reality, it is " + field.getClass().toString());
		DateDataObjectField<E> datefield = (DateDataObjectField<E>) object.payload.lookupSimpleFieldOnName(name);
		Date olddate = datefield.getValue();
		try {
			Date newdate = FlatFileLoader.parseDate(value,
					"Field '" + field.getName() + "- with format " + formatasstring, timeedit, format);

			if (FlatFileLoader.isTheSame(olddate, newdate)) {
				return false;
			} else {
				logger.info("  *** dates are different " + olddate + " " + newdate);
				datefield.setValue(newdate);
				return true;
			}

		} catch (DateTimeParseException e) {
			throw new RuntimeException("data is supposed to be date of format " + formatasstring + " for field '"
					+ this.name + "' but received the following error when parsing '" + value + "'. Exception "
					+ e.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectField<?, E> field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof DateDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type DateDataObjectField but in reality, it is " + field.getClass().toString());
		DateDataObjectField<E> datefield = (DateDataObjectField<E>) currentobject.payload.lookupSimpleFieldOnName(name);

		if (cellStyle == null)
			cellStyle = FlatFileExtractor.createDateStyle(cell.getSheet().getWorkbook(), this.formatasstring);

		cell.setCellValue((datefield.getValue()));
		cell.setCellStyle(cellStyle);
		return true;
	}
	
	/**
	 * put a date in a cell
	 * 
	 * @param cell spreadsheet cell
	 * @param value date value
	 * @param formatasstring format if the date is encoded as String
	 */
	public static <F extends FieldChoiceDefinition<F>> void putContentInCell(Cell cell, Date value,String formatasstring) {
		cell.setCellValue(value);
		cell.setCellStyle(FlatFileExtractor.createDateStyle(cell.getSheet().getWorkbook(), formatasstring));
	}
	
	/**
	 * get value from the cell
	 * 
	 * @param value a date or String object
	 * @param timeedit if true, time value is kept, else, date is normalized to noon GMT
	 * @param formatasstring format of the date if encoded as String
	 * @param contextforerror a context for error handling
	 * @return
	 */
	public static <F extends FieldChoiceDefinition<F>> Date getContentFromCell(
			Object value,
			boolean timeedit,
			String formatasstring,
			String contextforerror) {
		return FlatFileLoader.parseDate(value,
				contextforerror, timeedit, FlatFileLoader.generateFormat(formatasstring,
						contextforerror));
	}
}
