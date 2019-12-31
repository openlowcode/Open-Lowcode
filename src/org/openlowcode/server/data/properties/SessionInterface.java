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

/**
 * The interface all objects with the session property implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object of the session
 * @param <F> data object of the parent timeslot
 */
public interface SessionInterface<E extends DataObject<E>, F extends DataObject<F>> {
	/**
	 * gets the start time
	 * 
	 * @return the start time of the session
	 */
	public Date getStarttime();

	/**
	 * gets the end time
	 * 
	 * @return the end time of the session
	 */
	public Date getEndtime();

	/**
	 * A sequence starting with zero. This field is made to be generated from the
	 * parent timeslot / schedule.
	 * 
	 * @return the sequence
	 */
	public Integer getSequence();

	/**
	 * 
	 * @return gets the storage code of the valid attribute
	 */
	public String getValid();

	/**
	 * Note : this method does not perform persistence. This should be performed by
	 * an update. This is to allow batching of operations, or precising the fields
	 * during creation
	 * 
	 * @param starttime
	 * @param endtime
	 * @param sequence
	 * @param valid
	 */

	public void setsessiontime(/* discarded - OBJECT */Date starttime, Date endtime, Integer sequence,
			ChoiceValue<BooleanChoiceDefinition> valid);

	/**
	 * sets the parent timeslot
	 * @param object object with session property
	 * @param parentfortimeslot id of the parent timeslot
	 */
	public void setparenttimeslot(DataObjectId<F> parentfortimeslot);
}
