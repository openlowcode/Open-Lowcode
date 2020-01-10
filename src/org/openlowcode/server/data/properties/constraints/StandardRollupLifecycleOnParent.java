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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataExtractor;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * this will roll-up lifecycle according to the following principles
 * <ul>
 * <li>when one object moves to a state different than default, parent moves to
 * default working state</li>
 * <li>when all objects move to a final state, parents move to default final
 * state</li>
 * <li>if there are different final states possible, and if all children reach a
 * non default final state (typically canceled), the parent will get this
 * default final state if it exists. If there are various final states amongst
 * children, the default final is used on parent</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> Child object
 * @param <F> Parent object
 * @param <G> Child object lifecycle
 * @param <H> Parent object lifecycle
 */
public class StandardRollupLifecycleOnParent<E extends DataObject<E> & LifecycleInterface<E, G>, F extends DataObject<F> & LifecycleInterface<F, H>, G extends TransitionFieldChoiceDefinition<G>, H extends TransitionFieldChoiceDefinition<H>>
		extends RollupLifecycleOnParent<E, F, G, H> {

	/**
	 * Creates a roll-up class on parent
	 * 
	 * @param parentextractor      extractor of the parent from the child
	 * @param allchildrenextractor extractor of all children from the parent
	 * @param childlifecycle       lifecycle of the child object
	 * @param parentlifecycle      lifecycle of the parent object
	 */
	public StandardRollupLifecycleOnParent(DataExtractor<E, F> parentextractor,
			DataExtractor<F, E[]> allchildrenextractor, G childlifecycle, H parentlifecycle) {
		super(parentextractor, allchildrenextractor, childlifecycle, parentlifecycle);

	}

	@Override
	public ChoiceValue<H> getParentConsolidatedState(ArrayList<ChoiceValue<G>> otherchildrenstates, G childlifecycle,
			H parentlifecycle) {
		ChoiceValue<G> defaultchildstate = childlifecycle.getDefaultChoice();
		ChoiceValue<G> defaultfinalstate = childlifecycle.getDefaultFinalChoice();
		boolean oneworking = false;
		boolean allfinal = true;
		ChoiceValue<G> allchildreninnondefaultfinalstate = null;
		boolean started = false;
		for (int i = 0; i < otherchildrenstates.size(); i++) {
			ChoiceValue<G> otherchildstate = otherchildrenstates.get(i);
			// as long as one element is not in default state, parent will be in work
			if (!otherchildstate.equals(defaultchildstate))
				oneworking = true;
			// if at least one is not in final state, parent will not be in final state
			if (!childlifecycle.isChoiceFinal(otherchildstate))
				allfinal = false;
			// inits the non-default choice if valid
			if (!started) {
				if ((childlifecycle.isChoiceFinal(otherchildstate)) && (!otherchildstate.equals(defaultfinalstate)))
					allchildreninnondefaultfinalstate = otherchildstate;
			}
			// if they are not all equal later, we do not have an unanimous alternative
			// children
			if (started) {
				if (!otherchildstate.equals(allchildreninnondefaultfinalstate))
					allchildreninnondefaultfinalstate = null;
			}
			started = true;
		}
		// final judgement
		if (allchildreninnondefaultfinalstate != null) {
			if (parentlifecycle.parseValueFromStorageCode(allchildreninnondefaultfinalstate.getStorageCode()) != null)
				return parentlifecycle.parseValueFromStorageCode(allchildreninnondefaultfinalstate.getStorageCode());

		}
		if (allfinal)
			return parentlifecycle.getDefaultFinalChoice();
		if (oneworking)
			return parentlifecycle.getDefaultWorkingChoice();
		return null;
	}

}
