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

import org.openlowcode.server.data.DataObject;

/**
 * interface all data objects with schedule property should comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the schedule elements
 * @param <F> data object holding the schedule dependencies
 */
public interface ScheduleInterface<E extends DataObject<E>, F extends DataObject<F>> {
	/**
	 * This method will reschedule all tasks in the planning after this task in an
	 * efficient way
	 */
	public void rescheduleafter();
}
