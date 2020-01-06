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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;

/**
 * interface all objects with a lifecycle comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object holding the lifecycle
 * @param <F> the transition choice definition
 */
public interface LifecycleInterface<E extends DataObject<E>, F extends TransitionFieldChoiceDefinition<F>>
		extends UniqueidentifiedInterface<E> {
	/**
	 * get the state (storage code) of the boejct
	 * 
	 * @return state (storage code)
	 */
	public abstract String getState();

	/**
	 * change the state of the object (and persists the change)
	 * 
	 * @param newstate new state for the object (must be a valid transition from
	 *                 current state)
	 */
	public void changestate(ChoiceValue<F> newstate);

	/**
	 * @return a function allowing massive revise of the type
	 */
	public MassiveChangestate<E, F> getMassiveChangestate();

	/**
	 * This function allows the revise of a group of objects of the type,
	 * considering all business rules. This is used for other property massive
	 * updates that may depend on it
	 * 
     * @param <E> parent data object holding the lifecycle
     * @param <F> the transition choice definition
	 */
	public interface MassiveChangestate<E extends DataObject<E>, F extends TransitionFieldChoiceDefinition<F>> {
		public void changestate(E[] objectbatch, ChoiceValue<F>[] newstatebatch);
	}
}
