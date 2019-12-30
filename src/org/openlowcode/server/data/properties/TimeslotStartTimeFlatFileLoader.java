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
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * the loader to load the start time of an object with the timeslot property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class TimeslotStartTimeFlatFileLoader<E extends DataObject<E> & TimeslotInterface<E>>
		extends FlatFileLoaderColumn<E> {
	private DateTimeFormatter format;

	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private TimeslotDefinition<E> timeslotdefinition;
	private PropertyExtractor<E> propertyextractor;

	private String dateformat;

	/**
	 * creates the loader for the start time of the timeslot property
	 * @param dataobjectdefinition definition of the object
	 * @param timeslotdefinition definition of the property
	 * @param dateformat specified date format
	 * @param propertyextractor extractor to get the property from a data object
	 */
	public TimeslotStartTimeFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,
			TimeslotDefinition<E> timeslotdefinition, String dateformat, PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.timeslotdefinition = timeslotdefinition;
		this.propertyextractor = propertyextractor;
		format = FlatFileLoader.generateFormat(dateformat,
				"TimeslotStartTime for object " + dataobjectdefinition.getName());
		this.dateformat = dateformat;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		Date date = FlatFileLoader.parseDate(value,
				"TimeslotStartTime for object " + dataobjectdefinition.getName() + " for format " + dateformat, true,
				format);
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Timeslot))
			throw new RuntimeException("Technical error in inserting start date: property not of correct class: "
					+ property.getClass().getName());
		Timeslot<E> timeslot = (Timeslot<E>) property;
		Date olddate = timeslot.getStarttime();
		if (FlatFileLoader.isTheSame(olddate, date)) {
			return false;
		} else {
			timeslot.SetStarttime(date);
			postupdateprocessingstore.addPostUpdateProcessing("TIMESLOT",
					(obj) -> obj.reschedule(obj.getStarttime(), obj.getEndtime()));
			return true;
		}

	}

	@Override
	public boolean finalpostprocessing() {
		return true;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property == null)
			throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Timeslot))
			throw new RuntimeException("Technical error in inserting start date: property not of correct class: "
					+ property.getClass().getName());
		Timeslot<E> timeslot = (Timeslot<E>) property;
		cell.setCellValue(timeslot.getStarttime());
		return false;

	}

}
