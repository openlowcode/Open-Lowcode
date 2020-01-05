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

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.storage.StoredField;

/**
 * a property to use the data object as a schedule dependency. It should be an
 * auto-link to an object implementing the timeslot and schedule properties
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object used as schedule dependency
 * @param <F> the data object used as timeslot and schedule
 */
public class Scheduledependency<E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & TimeslotInterface<F> & ScheduleInterface<F, E>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Scheduledependency.class.getName());
	public static String SPLIT = "split";
	@SuppressWarnings("unused")
	private Autolinkobject<E, F> autolink;
	private DataObjectDefinition<F> taskobjectdefinition;
	private StoredField<String> split;

	/**
	 * Creates the schedule dependency property for a data object
	 * 
	 * @param definition           definition of the data object
	 * @param parentpayload        parent data object payload
	 * @param taskobjectdefinition
	 */
	@SuppressWarnings("unchecked")
	public Scheduledependency(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> taskobjectdefinition) {
		super(definition, parentpayload);
		this.taskobjectdefinition = taskobjectdefinition;
		split = (StoredField<String>) this.field.lookupOnName("SPLIT");
	}

	/**
	 * batch post processing after insert. Not optimized for performance
	 * 
	 * @param objectbatch             batch of objects.
	 * @param scheduledependencybatch corresponding batch of schedule dependency
	 *                                properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & TimeslotInterface<F> & ScheduleInterface<F, E>> void postprocStoredobjectInsert(
			E[] objectbatch, Scheduledependency<E, F>[] scheduledependencybatch) {
		logger.warning("----------- The procedure is called in masive, but this is not optimized------------");
		for (int i = 0; i < scheduledependencybatch.length; i++)
			scheduledependencybatch[i].postprocStoredobjectInsert(objectbatch[i]);
	}

	/**
	 * post processing after the link was created. It basically reschedules the
	 * tasks after the link
	 * 
	 * @param object data object
	 */
	public void postprocStoredobjectInsert(E object) {
		DataObjectId<F> originobjectfornewlinkid = object.getLfid();
		F parent = UniqueidentifiedQueryHelper.get().readone(originobjectfornewlinkid, taskobjectdefinition,
				(UniqueidentifiedDefinition<F>) taskobjectdefinition.getProperty("UNIQUEIDENTIFIED"));
		parent.rescheduleafter();
	}

	/**
	 * @return the split indicator
	 */
	public String getSplit() {
		return this.split.getPayload();
	}

	/**
	 * Split indicator indicates if the dependency is between two different
	 * timeslots (not split) or two different sessions of the same timeslot (split)
	 * 
	 * @param split split indicator. value is 'split' or empty
	 */
	void SetSplit(String split) {
		this.split.setPayload(split);
	}

	/**
	 * object sets the object as split
	 * 
	 * @param
	 */
	public void setassplit(E object) {
		this.split.setPayload(SPLIT);
	}

	/**
	 * set dependent property auto-link
	 * 
	 * @param autolink dependent property autolink
	 */
	public void setDependentPropertyAutolinkobject(Autolinkobject<E, F> autolink) {
		this.autolink = autolink;
	}
}
