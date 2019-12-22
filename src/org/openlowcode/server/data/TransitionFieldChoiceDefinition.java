/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.HashMap;

import org.openlowcode.tools.misc.NamedList;

/**
 *
 * This class allows to define a choice that depends from the previous value.
 * This is especially useful for lifecycles
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TransitionFieldChoiceDefinition<E extends FieldChoiceDefinition<E>> extends FieldChoiceDefinition<E> {
	private HashMap<ChoiceValue<E>, NamedList<ChoiceValue<E>>> authorizedtransitions;
	private HashMap<ChoiceValue<E>, ChoiceValue<E>> finaltransitionchoices;
	private ChoiceValue<E> defaultworkingchoice;
	private ChoiceValue<E> defaultfinalchoice;
	private boolean allowalltransitions;

	/**
	 * creates a new TransitionFieldChoiceDefinition
	 * 
	 * @param storagesize         persistence storage size for the code of one value
	 * @param allowalltransitions true if all transitions are allowed, false if
	 *                            transitions will be specified
	 */
	public TransitionFieldChoiceDefinition(int storagesize, boolean allowalltransitions) {
		super(storagesize);
		authorizedtransitions = new HashMap<ChoiceValue<E>, NamedList<ChoiceValue<E>>>();
		finaltransitionchoices = new HashMap<ChoiceValue<E>, ChoiceValue<E>>();
		this.allowalltransitions = allowalltransitions;
	}

	/**
	 * copies the transitions authorized in each ChoiceValue
	 */
	protected void finalizeTransitions() {
		ChoiceValue<E>[] allvalues = this.getChoiceValue();
		for (int i = 0; i < allvalues.length; i++)
			setAuthorizedtransitions(allvalues[i]);
	}

	/**
	 * defines what is the final choice
	 * 
	 * @param finalchoice the choice to be defined as final
	 */
	protected void defineFinalTransitionChoice(ChoiceValue<E> finalchoice) {
		finaltransitionchoices.put(finalchoice, finalchoice);
	}

	/**
	 * @return gets the default value for the working element. This is used in
	 *         simple workflows when someone agreed to work on the item
	 */
	public ChoiceValue<E> getDefaultWorkingChoice() {
		return defaultworkingchoice;
	}

	/**
	 * @return the default final choice
	 */
	public ChoiceValue<E> getDefaultFinalChoice() {
		return defaultfinalchoice;
	}

	/**
	 * @param choicevalue defines the default working choice. This is used in simple
	 *                    workflows when someone agreed to work on the item
	 */
	public void setDefaultWorkingChoice(ChoiceValue<E> choicevalue) {
		if (this.parseValueFromStorageCode(choicevalue.getStorageCode()) == null)
			throw new RuntimeException("default working state is not a valid value");
		this.defaultworkingchoice = choicevalue;
	}

	/**
	 * @param choicevalue defines the final choice
	 */
	public void setDefaultFinalChoice(ChoiceValue<E> choicevalue) {
		if (this.parseValueFromStorageCode(choicevalue.getStorageCode()) == null)
			throw new RuntimeException("default final state is not a valid value");
		this.defaultfinalchoice = choicevalue;
	}

	/**
	 * @return true if a final transition is defined
	 */
	public boolean IsFinalTransitionDefined() {
		if (finaltransitionchoices.size() > 0)
			return true;
		return false;
	}

	/**
	 * @param choice a choice that is part of this fieldchoicedefinition
	 * @return true if the choice is final
	 */
	public boolean isChoiceFinal(ChoiceValue<E> choice) {
		if (finaltransitionchoices.get(choice) != null)
			return true;
		return false;
	}

	/**
	 * Defines a valid transition
	 * 
	 * @param startvalue the start value of the authorized transition
	 * @param endvalue   the end value of the authorized transition
	 */
	protected void DefineTransition(ChoiceValue<E> startvalue, ChoiceValue<E> endvalue) {
		NamedList<ChoiceValue<E>> transitionsforstart = authorizedtransitions.get(startvalue);
		if (transitionsforstart == null) {
			transitionsforstart = new NamedList<ChoiceValue<E>>();
			authorizedtransitions.put(startvalue, transitionsforstart);
		}
		transitionsforstart.add(endvalue);
	}

	/**
	 * allows to check if a transition is valid or not
	 * 
	 * @param startvalue start value
	 * @param endvalue   end value
	 * @return true if the transition is valid, false else
	 */
	public boolean isTransitionValid(ChoiceValue<E> startvalue, ChoiceValue<E> endvalue) {
		if (this.allowalltransitions)
			return true;
		NamedList<ChoiceValue<E>> transitionforstart = authorizedtransitions.get(startvalue);
		if (transitionforstart == null)
			return false;
		if (transitionforstart.lookupOnName(endvalue.getName()) != null)
			return true;
		return false;
	}

	/**
	 * copies the authorized transition to the given choice value. This ensures that
	 * transition validities can be checked directly on the choice value
	 * 
	 * @param startvalue the value
	 */
	private void setAuthorizedtransitions(ChoiceValue<E> startvalue) {

		NamedList<ChoiceValue<E>> transitionforstart = authorizedtransitions.get(startvalue);
		startvalue.setTransitionRestrictions();
		if (this.allowalltransitions) {
			ChoiceValue<E>[] allvalues = this.getChoiceValue();
			for (int i = 0; i < allvalues.length; i++) {
				ChoiceValue<E> thisvalue = allvalues[i];
				if (!thisvalue.getStorageCode().equals(startvalue.getStorageCode()))
					startvalue.addTransition(thisvalue);
			}
			return;
		}
		if (transitionforstart == null) {

			return;
		}

		for (int i = 0; i < transitionforstart.getSize(); i++) {

			startvalue.addTransition(transitionforstart.get(i));
		}
		return;
	}

	/**
	 * @param valueasstored
	 * @return
	 */
	public ChoiceValue<E> parseValueFromStorageCode(String valueasstored) {
		return this.parseChoiceValue(valueasstored);
	}
}
