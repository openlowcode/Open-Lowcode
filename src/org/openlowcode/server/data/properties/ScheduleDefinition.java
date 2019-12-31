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

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.schedule.ScheduleHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of the schedule property. The schedule property indicates that the
 * data object is an element of schedule. Another data object is used to
 * indicate dependency links. This property includes rescheduling algorithms
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the timeslot object
 * @param <F> the schedule dependency object
 */
public class ScheduleDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F> & ScheduledependencyInterface<F, E> & AutolinkobjectInterface<F, E>>
		extends DataObjectPropertyDefinition<E> {
	@SuppressWarnings("unused")
	private TimeslotDefinition<E> timeslotdefinition;
	private ScheduledependencyDefinition<F, E> relatedscheduledependency;
	private DataObjectDefinition<F> linkobject;
	private ScheduleHelper<E> schedulehelper;

	/**
	 * creates the schedule property definition for a data object
	 * 
	 * @param parentobject   parent object definition
	 * @param linkobject     definition of the linked object (schedule dependency)
	 * @param schedulehelper the schedule helper specifying the working calendar
	 *                       used for scheduling
	 */
	public ScheduleDefinition(DataObjectDefinition<E> parentobject, DataObjectDefinition<F> linkobject,
			ScheduleHelper<E> schedulehelper) {
		super(parentobject, "SCHEDULE");
		this.linkobject = linkobject;
		this.schedulehelper = schedulehelper;

	}

	/**
	 * @return the definition of the related property related schedule dependency
	 */
	public ScheduledependencyDefinition<F, E> getRelatedScheduleDependency() {
		return this.relatedscheduledependency;
	}

	/**
	 * @return the schedue helper
	 */
	public ScheduleHelper<E> getScheduleHelper() {
		return this.schedulehelper;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return new FieldSchemaForDisplay[0];
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columnattributes == null)
			throw new RuntimeException("At least one attribute required for Schedule: STARTTIME or DURATION");
		if (columnattributes != null)
			if (columnattributes.length == 0)
				throw new RuntimeException("At least one attribute required for creationlog: DATE or USER");
		if ("STARTTIME".equals(columnattributes[0])) {
			String format = null;
			if (columnattributes.length > 1) {
				format = columnattributes[1];
			}
			return new ScheduleStartTimeFlatFileLoader<E, F>(objectdefinition, this, format, propertyextractor);
		}
		if ("DURATION".equals(columnattributes[0])) {
			if (columnattributes.length > 1) {
			}
			return new ScheduleDurationFlatFileLoader<E, F>(objectdefinition, this, propertyextractor, locale);

		}
		throw new RuntimeException(
				"First attribute for schedule should be  SCHEDULE or DURATION, not " + columnattributes[0]);
	}

	@Override
	public String[] getLoaderFieldList() {
		String[] fields = new String[2];
		fields[0] = "STARTTIME";
		fields[1] = "DURATION";
		return fields;

	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		if (name.equals("STARTTIME")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&STARTTIME";
			returntable[1] = "OPTIONAL";
			returntable[2] = "2017.02.28";
			returntable[3] = "Note: will take the first time during opening hours. Optional parameter: specific java simpledateformat\n (e.g. \"yyyy.MM.dd G 'at' HH:mm:ss z\" for 2001.07.04 AD at 12:08:56 PDT ) ,\n definition at https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
			return returntable;
		}
		if (name.equals("DURATION")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&DURATION";
			returntable[1] = "OPTIONAL";
			returntable[2] = "600";
			returntable[3] = "Active duration of the slot in minues";
			return returntable;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Schedule<E, F>(this, parentpayload, linkobject);
	}

	public void setDependentDefinitionTimeslot(TimeslotDefinition<E> timeslotdefinition) {
		this.timeslotdefinition = timeslotdefinition;
	}

	public void setGenericsScheduleProperty(ScheduledependencyDefinition<F, E> relatedscheduledependency) {
		this.relatedscheduledependency = relatedscheduledependency;
	}
}
