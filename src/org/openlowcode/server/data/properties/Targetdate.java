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
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.storage.StoredField;

/**
 * A property to store a target date for an object with lifecycle. It allows
 * <ul>
 * <li>to specify a target date for the finalization of the object (reaching a
 * final state)</li>
 * <li>to record the actual date the object was finalized</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> the lifecycle of the object
 */
public class Targetdate<E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E,F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectProperty<E> {

	private StoredField<Date> targetdate;
	private Lifecycle<E, F> lifecycle;

	@SuppressWarnings("unchecked")
	public Targetdate(TargetdateDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		targetdate = (StoredField<Date>) this.field.lookupOnName("TARGETDATE");
	}

	/**
	 * specifies the target date without persistence (used for data loading)
	 * 
	 * @param loadedtargetdate the target date to load
	 */
	void loadtargetdate(Date loadedtargetdate) {
		this.targetdate.setPayload(loadedtargetdate);
	}

	/**
	 * gets the current target date
	 * 
	 * @return the target date
	 */
	public Date getTargetdate() {
		return this.targetdate.getPayload();
	}

	/**
	 * sets the target date and persists it
	 * 
	 * @param object     the object
	 * @param targetdate the new target date
	 */
	public void settargetdate(E object, Date targetdate) {

		this.targetdate.setPayload(targetdate);
		Uniqueidentified<E> uniqueidentified = lifecycle.getUniqueidentified();
		if (uniqueidentified.getId() != null)
			if (uniqueidentified.getId().getId() != null)
				if (uniqueidentified.getId().getId().length() > 0)
					object.update();

	}

	/**
	 * sets the dependent property lifecycle
	 * 
	 * @param lifecycle dependent property lifecycle
	 */
	public void setDependentPropertyLifecycle(Lifecycle<E, F> lifecycle) {
		this.lifecycle = lifecycle;

	}
}
