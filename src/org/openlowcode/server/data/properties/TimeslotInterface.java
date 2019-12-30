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

/**
 * The interface an object having the timeslot property implements
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public interface TimeslotInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
	/**
	 * gets the start time of this timeslot
	 * 
	 * @return the start time
	 */
	public Date getStarttime();

	/**
	 * gets the end time of this timeslot
	 * 
	 * @return the end time
	 */
	public Date getEndtime();

	/**
	 * reschedule this object timeslot. This method is to be used when the
	 * reschedule is triggered by the user
	 * 
	 * @param starttime new start time of the timeslot
	 * @param endtime   new end time of the timeslot
	 */
	public void reschedule(Date starttime, Date endtime);

	/**
	 * reschedule this object timeslot. This method is to be used by automatic
	 * algorithms that will take care of rescheduling of dependents
	 * 
	 * @param starttime new start time of the timeslot
	 * @param endtime   new end time of the timeslot
	 */
	public void forcereschedule(Date starttime, Date endtime);

	/**
	 * repair the schedule
	 * 
	 * @obsolete
	 */
	public void repairschedule();
}
