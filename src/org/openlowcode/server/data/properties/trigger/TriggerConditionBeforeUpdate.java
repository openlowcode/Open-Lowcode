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
 * This trigger will execute before persistence during an update.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.7
 *
 */
public class TriggerConditionBeforeUpdate<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		TriggerCondition<E> {
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

		return false;
	}

	@Override
	public boolean executeBeforeDelete() {

		return false;
	}
	@Override
	public boolean executeBeforeUpdate() {
		return true;
	}
}
