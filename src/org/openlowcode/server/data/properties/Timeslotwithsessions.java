/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.schedule.WorkCalendarHelper.CalendarTimeSlot;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * This property is added on the timeslot object to mean it has detailed
 * sessions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> The timeslot object (total calendar period for an activity
 * @param <F> the session object. A child of the timeslot, it stores any precise
 *        session
 */

public class Timeslotwithsessions<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E, ?>, F extends DataObject<F> & UniqueidentifiedInterface<F> & SessionInterface<F, E>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Timeslotwithsessions.class.getName());
	private TimeslotwithsessionsDefinition<E, F> parseddefinition;
	private Schedule<E, ?> schedule;
	private Uniqueidentified<E> uniqueidentified;

	/**
	 * creates the timeslot with session
	 * 
	 * @param definition    definition of the property
	 * @param parentpayload payload of the parent object
	 */
	public Timeslotwithsessions(TimeslotwithsessionsDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.parseddefinition = definition;
	}

	/**
	 * adds the dependent property schedule to this object
	 * 
	 * @param schedule the dependent property object
	 */
	public void setDependentPropertySchedule(Schedule<E, ?> schedule) {
		this.schedule = schedule;
	}

	private void repairTimeslots(E object, Date starttime, Date endtime) {
		logger.info("--------------- Launch repair timeslot for object below ------------");
		logger.info(" * " + object.dropToString());

		// ------------ get all sessions
		F[] sessions = this.getallsessions(object, null);
		HashMap<Integer, F> sessionsbyindex = new HashMap<Integer, F>();
		// ------------ order sessions by index, remove duplicates
		for (int i = 0; i < sessions.length; i++) {
			F thissession = sessions[i];
			Integer thissessionindex = thissession.getSequence();
			if (sessionsbyindex.containsKey(thissessionindex)) {
				// this is a duplicate, we will kill the second session encountered
				thissession.delete();
			} else {
				// this is a new one, just add it to the ordered map
				sessionsbyindex.put(thissessionindex, thissession);
			}
		}
		// ----------- get timeslot new starttime and endtime
		Schedule<E, ?> schedule = this.schedule;
		CalendarTimeSlot[] newsessions = schedule.getSessionsForTimeSlot(object);

		for (int i = 0; i < newsessions.length; i++) {
			CalendarTimeSlot thiscalendartimeslot = newsessions[i];
			Date thissessionstarttime = thiscalendartimeslot.getStartTime();
			Date thissessionendtime = thiscalendartimeslot.getEndTime();
			ChoiceValue<BooleanChoiceDefinition> valid = BooleanChoiceDefinition.get().YES;
			if (thiscalendartimeslot.isOutOfSchedule())
				valid = BooleanChoiceDefinition.get().NO;
			Integer session = new Integer(i);
			F existingsession = sessionsbyindex.get(session);
			if (existingsession != null) {
				// treated, remove it from the hashmap
				sessionsbyindex.remove(session);
				existingsession.setsessiontime(thissessionstarttime, thissessionendtime, session, valid);
				existingsession.update();
			} else {
				F newsession = parseddefinition.getChildrenSessionDefinition().getParentObject().generateBlank();
				newsession.setsessiontime(thissessionstarttime, thissessionendtime, session, valid);
				newsession.setparenttimeslot(object.getId());
				newsession.insert();
			}

		}
		// finally, delete sessions present but not used;
		Iterator<F> unusedsessioniterator = sessionsbyindex.values().iterator();
		while (unusedsessioniterator.hasNext()) {
			F thisunusedsession = unusedsessioniterator.next();
			thisunusedsession.delete();
		}

	}

	/**
	 * repairs the schedule (in practice reschedule with the same dates as before)
	 * 
	 * @param object the object to start the reschedule on
	 */
	public void postprocTimeslotRepairschedule(E object) {
		if (object.getId() != null)
			if (object.getId().getId() != null)
				if (object.getId().getId().length() > 0)
					repairTimeslots(object, object.getStarttime(), object.getEndtime());
	}

	/**
	 * a procedure to force reschedule without retriggering all scheduling of
	 * further timeslots (this is called by the rescheduling process
	 * 
	 * @param object    the object to force reschedule
	 * @param starttime start time of the timeslot
	 * @param endtime   end time of the timeslot
	 */
	public void postprocTimeslotForcereschedule(E object, Date starttime, Date endtime) {
		// this is to ensure the timeslot reschedule is only triggered after insertion,
		// as force reschedule is
		// also triggered at slot creation
		if (object.getId() != null)
			if (object.getId().getId() != null)
				if (object.getId().getId().length() > 0)
					repairTimeslots(object, starttime, endtime);
	}

	/**
	 * reschedule this timeslot
	 * 
	 * @param object    the object to process
	 * @param starttime new start time for the timeslot
	 * @param endtime   new end time for the timeslot
	 */
	public void postprocTimeslotReschedule(E object, Date starttime, Date endtime) {
		repairTimeslots(object, starttime, endtime);
	}

	/**
	 * after object is inserted, repairs the timeslot
	 * 
	 * @param object the object to process
	 */
	public void postprocStoredobjectInsert(E object) {
		repairTimeslots(object, object.getStarttime(), object.getEndtime());
	}

	/**
	 * massive postprocessing for the objects after insertion
	 * 
	 * @param objectbatch         batch of objects
	 * @param timeslotwithsession batch of timeslotwithsessions
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E, ?>, F extends DataObject<F> & UniqueidentifiedInterface<F> & SessionInterface<F, E>> void postprocStoredobjectInsert(
			E[] objectbatch, Timeslotwithsessions<E, F>[] timeslotwithsession) {
		logger.warning("Method postprocStoredobjectInsert not implemented for massive");
		for (int i = 0; i < timeslotwithsession.length; i++) {
			Timeslotwithsessions<E, F> thistimeslotwithsession = timeslotwithsession[i];
			thistimeslotwithsession.postprocStoredobjectInsert(objectbatch[i]);
		}
	}

	/**
	 * get all sessions for this timeslot
	 * 
	 * @param object                   the current object with timeslot property
	 * @param additionalquerycondition additional query condition
	 * @return get all sessions
	 */
	public F[] getallsessions(E object, QueryFilter additionalquerycondition) {
		// Note: this is a duplicate to the code in linkedfromchildren as there is no
		// way so far in the framework
		// to have reference to linkedfromchildren inside timeslotwithsession
		LinkedtoparentDefinition<F, E> linkedtoparentdefinitionforsession = parseddefinition
				.getChildrenSessionDefinition().getLinkedToParentDefinition();
		return LinkedtoparentQueryHelper.get(linkedtoparentdefinitionforsession.getName()).getallchildren(
				uniqueidentified.getRelatedHasid().getId(), additionalquerycondition,
				parseddefinition.getChildrenSessionDefinition().getParentObject(), parseddefinition.getParentObject(),
				linkedtoparentdefinitionforsession);
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}
}
