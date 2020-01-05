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

import org.openlowcode.server.data.DataObject;

/**
 * the interface all objects used as schedule dependency implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object used as schedule dependency
 * @param <F> data object
 */
public interface ScheduledependencyInterface<E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & TimeslotInterface<F> & ScheduleInterface<F, E>> {

	/**
	 * @return the value of the split flag. Empty if the link is not a split between
	 *         sessions of the same timeslot, value is 'split' is dependency between
	 *         sessions of the same timeslot
	 */
	public String getSplit();

	/**
	 * sets as split
	 */
	public void setassplit();
}
