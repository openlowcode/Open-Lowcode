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

import java.util.Date;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.storage.StoredField;

/**
 * An object that has the timeslot property has a start date and an end date
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class Timeslot<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E>>
		extends DataObjectProperty<E> {
	private StoredField<Date> starttime;
	private StoredField<Date> endtime;
	private Uniqueidentified<E> uniqueidentified;

	/**
	 * Creation of the timeslot property
	 * 
	 * @param definition    definition of the timeslot for this object
	 * @param parentpayload payload of the object
	 */
	@SuppressWarnings("unchecked")
	public Timeslot(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		starttime = (StoredField<Date>) this.field.lookupOnName("STARTTIME");
		endtime = (StoredField<Date>) this.field.lookupOnName("ENDTIME");

	}

	/**
	 * @return get the start time
	 */
	public Date getStarttime() {
		return this.starttime.getPayload();
	}

	/**
	 * @param starttime sets the start time
	 */
	void SetStarttime(Date starttime) {
		this.starttime.setPayload(starttime);
	}

	/**
	 * repair the schedule (note: this is not used anymore)
	 * 
	 * @param object input object
	 * @obsolete
	 */
	public void repairschedule(E object) {
		// do nothing, data has already been changed in an update for file loading
	}

	/**
	 * reschedule the object
	 * 
	 * @param object    object
	 * @param starttime new start time
	 * @param endtime   new end time
	 */
	public void reschedule(E object, Date starttime, Date endtime) {
		forcereschedule(object, starttime, endtime);
	}

	/**
	 * forces the reschedule of the object. It is a distinct action from reschedule
	 * to allow differenciated triggers. Force reschedule is supposed to be used by
	 * complex algorithms
	 * 
	 * @param object    object
	 * @param starttime new start time
	 * @param endtime   new end time
	 */
	public void forcereschedule(E object, Date starttime, Date endtime) {
		this.starttime.setPayload(starttime);
		this.endtime.setPayload(endtime);
		if (uniqueidentified.getRelatedHasid().getId().getId() != null)
			if (uniqueidentified.getRelatedHasid().getId().getId().length() > 0)
				object.update();
	}

	/**
	 * gets the end time
	 * 
	 * @return the end time (as a java date)
	 */
	public Date getEndtime() {
		return this.endtime.getPayload();
	}

	/**
	 * sets the end time
	 * 
	 * @param endtime new end time
	 */
	void SetEndtime(Date endtime) {
		this.endtime.setPayload(endtime);
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified unique identified property for the object
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {

		this.uniqueidentified = uniqueidentified;
	}
}
