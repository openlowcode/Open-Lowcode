/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.formula;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * a data update trigger will be triggered inside a formula when a calculated
 * field is updated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object this trigger applies to
 */
public abstract class DataUpdateTrigger<E extends DataObject<E>> extends Named {

	public DataUpdateTrigger(String name) {
		super(name);

	}

	/**
	 * computes the new value and raises triggers
	 * 
	 * @param contextobject the object to execute the trigger
	 * @return the list of triggers that when raised
	 */
	public NamedList<TriggerToExecute<E>> compute(E contextobject) {
		return compute(contextobject, false);
	}

	/**
	 * computes the new value and raises triggers
	 * 
	 * @param contextobject    the object to execute the trigger
	 * @param forcelocalupdate true to raise other triggers even if the update is
	 *                         local
	 * @return the list of triggers that when raised
	 */
	public abstract NamedList<TriggerToExecute<E>> compute(E contextobject, boolean forcelocalupdate);

	/**
	 * @return true if trigger is local
	 */
	public abstract boolean isLocal();

	/**
	 * @return true if the trigger is custom (implemented by the user)
	 */
	public abstract boolean isCustomTrigger();

}