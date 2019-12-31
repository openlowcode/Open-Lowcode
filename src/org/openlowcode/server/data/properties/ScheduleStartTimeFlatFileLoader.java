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
import org.openlowcode.module.system.data.Workcalendar;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.properties.schedule.WorkCalendarHelper;


/**
 * loader for the start time field of the schedule property
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the schedule elements
 * @param <F> data object holding the schedule dependencies
 */
public class ScheduleStartTimeFlatFileLoader<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E,F>,F extends DataObject<F> & UniqueidentifiedInterface<F> & ScheduledependencyInterface<F,E> & AutolinkobjectInterface<F,E>> extends FlatFileLoaderColumn<E> {
	private DateTimeFormatter format;
	private DataObjectDefinition<E> dataobjectdefinition;
	private ScheduleDefinition<E,F> scheduledefinition;
	private PropertyExtractor<E> propertyextractor;
	private String dateformat;
	private CellStyle cellStyle;
	/**
	 * creates a loader for the start time of a session property
	 * 
	 * @param dataobjectdefinition parent object definition
	 * @param sessiondefinition    definition of the session object
	 * @param dateformat           format of the date for the loader
	 * @param propertyextractor    a property extractor
	 */
	public ScheduleStartTimeFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,ScheduleDefinition<E,F> scheduledefinition,String dateformat,PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.scheduledefinition = scheduledefinition;
		this.propertyextractor=propertyextractor;
		this.dateformat = dateformat;
		format = FlatFileLoader.generateFormat(dateformat,"Schedule Start Time Loader for object "+dataobjectdefinition.getName());
	}
	@Override
	public boolean load(E object, Object value,PostUpdateProcessingStore<E> postupdateprocessingstore)  {
		Date date=FlatFileLoader.parseDate(value, "Parsing Schedule Start Tile for object "+dataobjectdefinition.getName()+" for format = "+dateformat, true, format);
		
		
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Schedule)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings("unchecked")
		Schedule<E,F> schedule = (Schedule<E,F>) property;
		Timeslot<E> parenttimeslot = schedule.getTimeslot();
		
		Date olddate = parenttimeslot.getStarttime();
		
		if (parenttimeslot.getStarttime()==null) parenttimeslot.SetStarttime(date);
		
		if (FlatFileLoader.isTheSame(olddate,date)) {
			return false;
		} else {
			Workcalendar calendar = scheduledefinition.getScheduleHelper().getWorkCalendar(object);
			WorkCalendarHelper workcalendarhelper = new WorkCalendarHelper(calendar);
			Date realstarttime = workcalendarhelper.getNextStartDate(date);
			parenttimeslot.SetStarttime(realstarttime);
			postupdateprocessingstore.addPostUpdateProcessing("TIMESLOT",(obj)->obj.reschedule(obj.getStarttime(),obj.getEndtime()));
			
			return true;
		}
		
	}
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Schedule)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings("unchecked")
		Schedule<E,F> schedule = (Schedule<E,F>) property;
		Timeslot<E> parenttimeslot = schedule.getTimeslot();
		if (cellStyle==null) 
			cellStyle = FlatFileExtractor.createDateStyle(cell.getSheet().getWorkbook(),this.dateformat);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(parenttimeslot.getStarttime());
		
		return true;
	}


}
