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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.module.system.data.Workcalendar;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;

import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.properties.schedule.WorkCalendarHelper;



/**
 * loader for the duration field of the schedule property
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the schedule elements
 * @param <F> data object holding the schedule dependencies
 */
public class ScheduleDurationFlatFileLoader<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E,F>,F extends DataObject<F> & UniqueidentifiedInterface<F> & ScheduledependencyInterface<F,E> & AutolinkobjectInterface<F,E>> extends FlatFileLoaderColumn<E> {

	@SuppressWarnings("unused")
	private DataObjectDefinition<E> dataobjectdefinition;
	private ScheduleDefinition<E,F> scheduledefinition;
	private PropertyExtractor<E> propertyextractor;
	private SimpleDateFormat detailedformat;
	private ChoiceValue<ApplocaleChoiceDefinition> locale;
	private DecimalFormat decimalformat;
	public ScheduleDurationFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,ScheduleDefinition<E,F> scheduledefinition,PropertyExtractor<E> propertyextractor,ChoiceValue<ApplocaleChoiceDefinition> locale)  {
		this.dataobjectdefinition = dataobjectdefinition;
		this.scheduledefinition = scheduledefinition;
		this.propertyextractor=propertyextractor;
		detailedformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		this.locale = locale;
		decimalformat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		if (this.locale.getStorageCode().equals(ApplocaleChoiceDefinition.get().FR.getStorageCode()));
			decimalformat = (DecimalFormat) NumberFormat.getInstance(Locale.FRENCH);
		decimalformat.setParseBigDecimal(true);
	}
	@Override
	public boolean load(E object, Object value,PostUpdateProcessingStore<E> postupdateprocessingstore) {
		BigDecimal decimal = null;
		
			try {
				if (value instanceof String) {
					String stringvalue = (String) value;
			if (stringvalue!=null) if (stringvalue.length()>0) {
				Number number = decimalformat.parse(stringvalue);
				decimal = (BigDecimal) number;
			}
				}
			if (value instanceof Double) {
				Double doublevalue = (Double) value;
				decimal = new BigDecimal(doublevalue);
			}
			if (decimal==null) throw new RuntimeException("Object "+value+" could not be parsed into a decimal");
		} catch (ParseException e) {
			throw  new RuntimeException("Integer parsing error for value '"+value+"'. Exception "+e.getMessage());
		}
		
		int minutesrounded = decimal.intValue();
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Schedule)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings("unchecked")
		Schedule<E,F> schedule = (Schedule<E,F>) property;
		Timeslot<E> parenttimeslot = schedule.getTimeslot();
		Date startdate = parenttimeslot.getStarttime();
		Workcalendar calendar = scheduledefinition.getScheduleHelper().getWorkCalendar(object);
		WorkCalendarHelper workcalendarhelper = new WorkCalendarHelper(calendar);
		Date realendtime = workcalendarhelper.getEndDate(startdate,minutesrounded);
		Date oldendtime = parenttimeslot.getEndtime();
		
		
		if (FlatFileLoader.isTheSame((oldendtime!=null?detailedformat.format(oldendtime):null),(realendtime!=null?detailedformat.format(realendtime):null))) {
			return false;
		} else {
			parenttimeslot.SetEndtime(realendtime);
			postupdateprocessingstore.addPostUpdateProcessing("TIMESLOT",(obj)->obj.reschedule(obj.getStarttime(),obj.getEndtime()));
			
			return true;
		}
		
	}
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context)  {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Schedule)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings({ "unchecked", "unused" })
		Schedule<E,F> schedule = (Schedule<E,F>) property;
		cell.setCellValue("NOT YET IMPLEMENTED");
		return false;
	}
}
