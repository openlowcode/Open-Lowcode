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

import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * A session is a portion of a Timeslot. Typical use is to plan, say, 16 hours
 * of work inside opening hours of a shop (say 8am to 4pm), the 16 hours of work
 * in the timeslot may be made of 3 sessions: monday from noon to 4PM, tuesday
 * from 8AM to 4PM, wednesday from 8AM to noon.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object of the session
 * @param <F> data object of the 'parent' Timeslot
 */
public class Session<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & TimeslotInterface<F>>
		extends DataObjectProperty<E> {
	private StoredField<Date> starttime;
	private StoredField<Date> endtime;
	private StoredField<Integer> sequence;
	private StoredField<String> valid;
	@SuppressWarnings("unused")
	private SessionDefinition<E, F> parseddefinition;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private Linkedtoparent<E, F> linkedtoparent;

	/**
	 * creates a session property
	 * 
	 * @param definition    definition of the session property
	 * @param parentpayload payload of the parent object
	 */
	@SuppressWarnings("unchecked")
	public Session(SessionDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		starttime = (StoredField<Date>) this.field.lookupOnName("STARTTIME");
		endtime = (StoredField<Date>) this.field.lookupOnName("ENDTIME");
		sequence = (StoredField<Integer>) this.field.lookupOnName("SEQUENCE");
		valid = (StoredField<String>) this.field.lookupOnName("VALID");
		this.parseddefinition = definition;
	}

	/**
	 * start time of this session
	 * 
	 * @return gets the start time of the session
	 */
	public Date getStarttime() {
		return starttime.getPayload();
	}

	/**
	 * end time of this session
	 * 
	 * @return gets the end time of the session
	 */
	public Date getEndtime() {
		return endtime.getPayload();
	}

	/**
	 * @return the sequence of this session inside the timeslot
	 */
	public Integer getSequence() {
		return sequence.getPayload();
	}

	/**
	 * @return true if the session is valid,
	 */
	public String getValid() {
		return this.valid.getPayload();
	}

	/**
	 * sets the parent timeslot
	 * 
	 * @param object            object
	 * @param parentfortimeslot id of the parent session
	 */
	public void setparenttimeslot(E object, DataObjectId<F> parentfortimeslot) {
		this.linkedtoparent.setparentwithoutupdate(object, parentfortimeslot);
	}

	/**
	 * sets the session time
	 * 
	 * @param object
	 * @param starttime start of the session
	 * @param endtime   end of the session
	 * @param sequence  sequence of the timeslot
	 * @param valid     true if the sequence is fully inside an opening slot
	 */
	public void setsessiontime(E object, Date starttime, Date endtime, Integer sequence,
			ChoiceValue<BooleanChoiceDefinition> valid) {
		this.starttime.setPayload(starttime);
		this.endtime.setPayload(endtime);
		this.sequence.setPayload(sequence);
		this.valid.setPayload((valid != null ? valid.getStorageCode() : ""));
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified unique identified property
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	/**
	 * sets the dependent property linked to parent (parent is the session for
	 * scheduling purposes)
	 * 
	 * @param linkedtoparent the linkedtoparent property
	 */
	public void setDependentPropertyLinkedtoparent(Linkedtoparent<E, F> linkedtoparent) {
		this.linkedtoparent = linkedtoparent;
	}

	/**
	 * sets the start time
	 * 
	 * @param date new start time
	 */
	protected void SetStarttime(Date date) {
		this.starttime.setPayload(date);

	}

	/**
	 * sets the end time
	 * 
	 * @param date new end time
	 */
	protected void SetEndtime(Date date) {
		this.endtime.setPayload(date);
	}

	/**
	 * @param sequence sets the sequence
	 */
	protected void setSequence(Integer sequence) {
		this.sequence.setPayload(sequence);
	}

	/**
	 * @param valid a String representing the storage code of the system module
	 */
	protected void setValid(String valid) {
		this.valid.setPayload(valid);
	}
}
