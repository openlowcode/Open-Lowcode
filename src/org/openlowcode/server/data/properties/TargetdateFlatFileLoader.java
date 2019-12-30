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


import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * flat file loader for target date
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 */
public class TargetdateFlatFileLoader<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	private DateTimeFormatter format;
	private PropertyExtractor<E> propertyextractor;
	private DataObjectDefinition<E> dataobjectdefinition;
	private String dateformat;
	private CellStyle cellStyle;

	/**
	 * Creates a new Target date flat-file loader
	 * 
	 * @param dataobjectdefinition definition of the parent object
	 * @param dateformat           format of the date
	 * @param propertyextractor    extractor to get targetdate property from the
	 *                             data object
	 */
	public TargetdateFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition, String dateformat,
			PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;

		this.propertyextractor = propertyextractor;
		format = FlatFileLoader.generateFormat(dateformat, "Targetdate for object " + dataobjectdefinition.getName());
		this.dateformat = dateformat;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore)
			 {
		Date date = FlatFileLoader.parseDate(value,
				"Targetdate for object " + dataobjectdefinition.getName() + " for format " + dateformat, true, format);

		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting targetdate: property not found");
		if (!(property instanceof Targetdate))
			throw new RuntimeException("Technical error in inserting target date: property not of correct class: "
					+ property.getClass().getName());
		@SuppressWarnings("rawtypes")
		Targetdate targetdate = (Targetdate) property;
		Date olddate = targetdate.getTargetdate();
		if (FlatFileLoader.isTheSame(olddate, date)) {
			return false;
		} else {
			targetdate.loadtargetdate(date);
			return true;
		}

	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context)  {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property == null)
			throw new RuntimeException( "Technical error in inserting targetdate: property not found");
		if (!(property instanceof Targetdate))
			throw new RuntimeException( "Technical error in inserting target date: property not of correct class: "
					+ property.getClass().getName());
		@SuppressWarnings("rawtypes")
		Targetdate targetdate = (Targetdate) property;
		if (cellStyle != null)
			cellStyle = FlatFileExtractor.createDateStyle(cell.getSheet().getWorkbook(), this.dateformat);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(targetdate.getTargetdate());

		return true;
	}

}
