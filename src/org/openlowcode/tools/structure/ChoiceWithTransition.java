/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;
/**
 * An extension to the choice interface, with the choice object
 * also providing the authorized transitions to the next values
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public interface ChoiceWithTransition extends Choice{
	
	/**
	 * @return true if there are restrictions, false if
	 * no restriction is set 
	 */
	public boolean isTransitionrestrictions();
	
	/**
	 * @return the list of choices authorized for this data
	 * as transition (next value in the application)
	 */
	public ChoiceWithTransition[] getAuthorizedTransitions();
}
