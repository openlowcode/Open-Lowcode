/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.schedule;

import org.openlowcode.module.system.data.Workcalendar;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.TimeslotInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * This helper needs to be implemented for each Schedule object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 * @param <E> the schedule object the loader is treating
 */
public abstract class ScheduleHelper<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E>> {

	/**
	 * gets the work calendar
	 * 
	 * @param object
	 * @return
	 */
	public abstract Workcalendar getWorkCalendar(E object);

	/**
	 * gets all objects of the planning
	 * 
	 * @param object
	 * @return all objects of the full planning
	 */
	public abstract E[] getFullPlanning(E object);

}
