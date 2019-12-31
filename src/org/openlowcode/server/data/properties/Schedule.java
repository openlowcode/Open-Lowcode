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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Weeklyslot;
import org.openlowcode.module.system.data.Workcalendar;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;

import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.schedule.ScheduleHelper;
import org.openlowcode.server.data.properties.schedule.WorkCalendarHelper;
import org.openlowcode.server.data.properties.schedule.WorkCalendarHelper.CalendarTimeSlot;

/**
 * The schedule property indicates that the data object is an element of
 * schedule. Another data object is used to indicate dependency links. This
 * property includes rescheduling algorithms
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the timeslot object
 * @param <F> the schedule dependency object
 */
public class Schedule<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E, F>, F extends DataObject<F> & AutolinkobjectInterface<F, E> & ScheduledependencyInterface<F, E>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Schedule.class.getName());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.M.dd HH:mm:ss.SSS");
	private Timeslot<E> timeslot;
	private DataObjectDefinition<F> dependencylinkobjectdefinition;
	private ScheduleHelper<E> schedulehelper;
	private ScheduleDefinition<E, F> scheduledefinition;
	private WorkCalendarHelper workcalendarhelperforsession;

	/**
	 * get the related Timeslot property of this schedule element
	 * 
	 * @return return timeslot property
	 */
	public Timeslot<E> getTimeslot() {
		return this.timeslot;
	}

	/**
	 * get all the valid timeslots (time only, not the data object) for the given
	 * data object start time and end time
	 * 
	 * @param object
	 * @return
	 */
	public CalendarTimeSlot[] getSessionsForTimeSlot(E object) {
		if (this.workcalendarhelperforsession == null)
			this.setWorkCalendarHelper(object);
		return this.workcalendarhelperforsession.getAllSlotsForTimeSlot(object.getStarttime(), object.getEndtime());

	}

	/**
	 * sets the work calendar. Work calendar provides valid working time and
	 * holidays (days without work)
	 * 
	 * @param object
	 */
	private void setWorkCalendarHelper(E object) {
		Workcalendar calendar = schedulehelper.getWorkCalendar(object);
		this.workcalendarhelperforsession = new WorkCalendarHelper(calendar);
	}

	/**
	 * creates a new schedule property
	 * 
	 * @param definition                     definition of the schedule property
	 * @param parentpayload                  parent payload of the data object
	 * @param dependencylinkobjectdefinition dependent object definition of the link
	 *                                       representing the schedule dependencies
	 */
	public Schedule(ScheduleDefinition<E, F> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> dependencylinkobjectdefinition) {
		super(definition, parentpayload);
		this.scheduledefinition = definition;
		this.dependencylinkobjectdefinition = dependencylinkobjectdefinition;
		this.schedulehelper = scheduledefinition.getScheduleHelper();
	}

	/**
	 * reschedules all objects after the specified object
	 * 
	 * @param object
	 */
	public void rescheduleafter(E object) {
		setWorkCalendarHelper(object);
		changeMeetingsAfter(object, this.workcalendarhelperforsession, 0);
	}

	/**
	 * repairs the schedule after the current schedule element
	 * 
	 * @param object current schedule element
	 */
	public void postprocTimeslotRepairschedule(E object) {
		Workcalendar calendar = schedulehelper.getWorkCalendar(object);
		changeMeetingsAfter(object, new WorkCalendarHelper(calendar), 0);
	}

	/**
	 * post treatment after the schedule element has been rescheduled
	 * 
	 * @param object    current schedule element
	 * @param starttime new start time
	 * @param endtime   new end time
	 */
	public void postprocTimeslotReschedule(E object, Date starttime, Date endtime) {
		logger.fine(" ---- Reschedule object with new date " + sdf.format(starttime) + "-" + sdf.format(endtime)
				+ "  ---- ");
		logger.fine(" --- object drop for reschedule = " + (object != null ? object.dropToString() : "null"));

		Workcalendar calendar = schedulehelper.getWorkCalendar(object);
		changeMeetingsAfter(object, new WorkCalendarHelper(calendar), 0);
		logger.fine(" ----- Reschedule end for new date " + sdf.format(starttime) + "-" + sdf.format(endtime));
	}

	/**
	 * sets the dependent timeslot property. Timeslot stores the start and end date,
	 * and schedule has the dependency and rescheduling logic
	 * 
	 * @param timeslot dependent timeslot property
	 */
	public void setDependentPropertyTimeslot(Timeslot<E> timeslot) {
		this.timeslot = timeslot;
	}

	/**
	 * gets the calendar start hour (earliest slot in the week)
	 * 
	 * @param object (current object - not used)
	 * @return the earliest start hour as an integer
	 */
	public Integer getcalendarstarthour(E object) {
		Workcalendar workcalendar = schedulehelper.getWorkCalendar(object);
		int starthour = 23;
		int endhour = 0;
		Weeklyslot[] slots = workcalendar.getallchildrenforworkcalendarforweeklyslot(null);
		for (int i = 0; i < slots.length; i++) {
			Weeklyslot slot = slots[i];
			if (slot.getHourstart().intValue() < starthour)
				starthour = slot.getHourstart().intValue();
			int thisendhour = slot.getHourend().intValue();
			if (slot.getMinuteend().intValue() > 0)
				thisendhour++;
			if (thisendhour > endhour)
				endhour = thisendhour;
		}
		return starthour;
	}

	/**
	 * gets the calendar end hour (latest slot in the week)
	 * 
	 * @param object (current object - not used)
	 * @return the latest end hour as an integer
	 */
	public Integer getcalendarendhour(E object) {
		Workcalendar workcalendar = schedulehelper.getWorkCalendar(object);
		int starthour = 23;
		int endhour = 0;
		Weeklyslot[] slots = workcalendar.getallchildrenforworkcalendarforweeklyslot(null);
		for (int i = 0; i < slots.length; i++) {
			Weeklyslot slot = slots[i];
			if (slot.getHourstart().intValue() < starthour)
				starthour = slot.getHourstart().intValue();
			int thisendhour = slot.getHourend().intValue();
			if (slot.getMinuteend().intValue() > 0)
				thisendhour++;
			if (thisendhour > endhour)
				endhour = thisendhour;
		}
		return endhour;
	}

	/**
	 * gets the next start date in the calendar after the end of the current slot
	 * 
	 * @param object current slot
	 * @return next valid start date
	 */
	public Date getnextstarthour(E object) {
		Workcalendar calendar = schedulehelper.getWorkCalendar(object);
		WorkCalendarHelper workcalendarhelper = new WorkCalendarHelper(calendar);
		return workcalendarhelper.getNextStartDate(object.getEndtime());
	}

	/**
	 * inserts this task between two tasks.
	 * 
	 * @param previousobject task to insert after
	 * @param nextobject     task to insert before
	 */
	public void insertafter(E previousobject, E nextobject) {
		transferDependencies(previousobject, nextobject);
		F linktonext = dependencylinkobjectdefinition.generateBlank();
		linktonext.setleftobject(previousobject.getId());
		linktonext.setrightobject(nextobject.getId());
		linktonext.insert();
		nextobject.rescheduleafter();
	}

	private TwoDataObjects<F, E>[] getAutolinkAndChildren(E timeslot) {
		return AutolinkobjectQueryHelper.get().getlinksandrightobject(timeslot.getId(), null,
				dependencylinkobjectdefinition, definition.getParentObject(),
				scheduledefinition.getRelatedScheduleDependency().getAutoLinkObjectDefinition());
	}

	/**
	 * transfer dependencies from the first timeslot to the second timeslot
	 * 
	 * @param firsttimeslot  first timeslot
	 * @param secondtimeslot second timeslot
	 */
	private void transferDependencies(E firsttimeslot, E secondtimeslot) {
		logger.fine(" ---      Transfer dependencies");

		TwoDataObjects<F, E>[] firstobjectdependents = getAutolinkAndChildren(firsttimeslot);
		for (int i = 0; i < firstobjectdependents.length; i++) {
			F link = firstobjectdependents[i].getObjectOne();
			link.setleftobject(secondtimeslot.getId());
			link.update();
		}
	}

	/**
	 * change all meetings according to dependencies after the original slot. This
	 * is a recursive method with a breaker.
	 * 
	 * @param originslot     origin slot to perform the rescheduling affter
	 * @param calendarhelper calendar
	 * @param breaker        recursive breaker
	 */
	public void changeMeetingsAfter(E originslot, WorkCalendarHelper calendarhelper, int breaker) {
		if (breaker > 1024)
			throw new RuntimeException("Infinite loop on reschdule on object" + originslot.getId());
		logger.fine(" ---      start changeMeethodAfter -----, breaker = " + breaker);
		if (originslot != null) {
			TwoDataObjects<F, E>[] dependents = getAutolinkAndChildren(originslot);
			for (int i = 0; i < dependents.length; i++) {
				logger.fine(" ---      changeMeethodAfter, managing child (" + i + "/" + dependents.length
						+ ") -----, breaker = " + breaker);
				@SuppressWarnings("unused")
				F link = dependents[i].getObjectOne();
				E successor = dependents[i].getObjectTwo();

				if (successor.getStarttime().compareTo(originslot.getEndtime()) < 0) {
					Date newstarttime = calendarhelper.getNextStartDate(originslot.getEndtime());

					long successorlengthinminute = calendarhelper.getActiveDurationInMinutes(successor.getStarttime(),
							successor.getEndtime());
					logger.severe("    -+- requesting next end date from " + sdf.format(newstarttime)
							+ " with duration " + successorlengthinminute);

					Date newendtime = calendarhelper.getEndDate(newstarttime, successorlengthinminute);
					// this puts the calendar helper for each son, so that if the timeslot /
					// schedule object has sessions
					// the calendar does not have to be queried for each treatment
					Schedule<E, F> successorschedule = successor.getPropertyForObject(this);
					successorschedule.workcalendarhelperforsession = calendarhelper;
					logger.severe("    -+- force reschedule from " + sdf.format(newstarttime) + " to "
							+ sdf.format(newendtime));
					successor.forcereschedule(newstarttime, newendtime);

					changeMeetingsAfter(successor, calendarhelper, breaker + 1);
				}
			}
		}
	}
}
