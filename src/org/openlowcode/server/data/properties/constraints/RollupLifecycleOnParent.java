/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataExtractor;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * A utility abstract class to roll-up the lifecycle from children objects to
 * parent object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> child data object
 * @param <F> parent data object
 * @param <G> lifecycle of the children
 * @param <H> lifecycle of the parent
 */
public abstract class RollupLifecycleOnParent<E extends DataObject<E> & LifecycleInterface<E, G>, F extends DataObject<F> & LifecycleInterface<F, H>, G extends TransitionFieldChoiceDefinition<G>, H extends TransitionFieldChoiceDefinition<H>> {
	private static Logger logger = Logger.getLogger(RollupLifecycleOnParent.class.getName());
	private DataExtractor<E, F> parentextractor;
	private DataExtractor<F, E[]> allchildrenextractor;
	private G childlifecycle;
	private H parentlifecycle;

	/**
	 * creates a Rollup Lifecycle on parent utility class
	 * 
	 * @param parentextractor      extractor to get parent from one child
	 * @param allchildrenextractor extractor to get all childen from a parent
	 * @param childlifecycle       child lifecycle transition choice
	 * @param parentlifecycle      parent lifecycle transition choice
	 */
	public RollupLifecycleOnParent(DataExtractor<E, F> parentextractor, DataExtractor<F, E[]> allchildrenextractor,
			G childlifecycle, H parentlifecycle) {
		this.parentextractor = parentextractor;
		this.allchildrenextractor = allchildrenextractor;
		this.childlifecycle = childlifecycle;
		this.parentlifecycle = parentlifecycle;
	}

	/**
	 * rolls-up the state on parent
	 * 
	 * @param child child that was just updated
	 */
	public void rollupStateOnParent(E child) {
		logger.info("managing rollup state on parent for object " + child);
		F parent = (F) this.parentextractor.extract(child.getId());
		logger.info("lookup parent " + child);
		E[] allchildren = this.allchildrenextractor.extract(parent.getId());
		ArrayList<ChoiceValue<G>> allchildrenstates = new ArrayList<ChoiceValue<G>>();
		logger.info("found children " + allchildren.length);
		for (int i = 0; i < allchildren.length; i++) {
			allchildrenstates.add(childlifecycle.parseValueFromStorageCode(allchildren[i].getState()));
		}
		ChoiceValue<H> newstate = getParentConsolidatedState(allchildrenstates, childlifecycle, parentlifecycle);
		if (newstate != null)
			if (!(parent.getState().equals(newstate.getStorageCode())))
				parent.changestate(newstate);
	}

	/**
	 * calculates the consolidated state for the parent.
	 * 
	 * @param otherchildrenstates all the states of children
	 * @param childlifecycle      lifecycle of the child
	 * @param parentlifecycle     lifecycle of the parent
	 * @return
	 */
	public abstract ChoiceValue<H> getParentConsolidatedState(ArrayList<ChoiceValue<G>> otherchildrenstates,
			G childlifecycle, H parentlifecycle);

}
