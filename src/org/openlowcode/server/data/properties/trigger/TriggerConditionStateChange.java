/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.trigger;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * a trigger condition that is triggered when object changes state to a number
 * of provides states
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the condition is created on
 */
public class TriggerConditionStateChange<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends TriggerCondition<E> {
	private ChoiceValue<?>[] validstates;

	/**
	 * creates a trigger condition that will be executed when the object changes
	 * states to a list of provided states
	 * 
	 * @param validstates the valid states for launching the triggers
	 */
	public TriggerConditionStateChange(ChoiceValue<?>[] validstates) {
		this.validstates = validstates;
	}

	@Override
	public boolean executeOnInsert() {

		return false;
	}

	@Override
	public boolean executeOnUpdate() {

		return false;
	}

	@Override
	public boolean executeOnStateChange(ChoiceValue<?> newstate) {
		if (validstates != null)
			for (int i = 0; i < validstates.length; i++) {
				ChoiceValue<?> thisstate = validstates[i];
				if (thisstate.getStorageCode().compareTo(newstate.getStorageCode()) == 0)
					return true;
			}
		return false;
	}

	@Override
	public boolean executeBeforeDelete() {

		return false;
	}
	@Override
	public boolean executeBeforeUpdate() {
		return false;
	}
}
