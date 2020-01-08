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
 * A trigger condition decides on how a trigger will be executed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the condition is on
 */
public abstract class TriggerCondition<E extends DataObject<E> & UniqueidentifiedInterface<E>> {
	/**
	 * @return true if the trigger should be executed on new object insert
	 */
	public abstract boolean executeOnInsert();

	/**
	 * @return true if the trigger should be executed every time on object update
	 */
	public abstract boolean executeOnUpdate();

	/**
	 * @return true if the trigger should be executed when object is deleted
	 */
	public abstract boolean executeBeforeDelete();

	/**
	 * @param newstate the new state
	 * @return true if the trigger should be executed when state of the object is
	 *         moving to the new state
	 */
	public abstract boolean executeOnStateChange(ChoiceValue<?> newstate);

}
