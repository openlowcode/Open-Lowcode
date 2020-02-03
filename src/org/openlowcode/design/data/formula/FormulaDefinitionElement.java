/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.formula;

import org.openlowcode.design.data.DataObjectDefinition;

/**
 * A formula definition element provides a result when a calculation is
 * triggered. This element will generate the appropriate code for run-time
 * 
 * {@Link org.openlowcode.design.data.formula.FormulaDefinition} are made of consistent
 * FormulaDefinitionElement.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface FormulaDefinitionElement {

	/**
	 * @return the DataObjectDefinition who is owning the formula Definition
	 *         Element. A formula definition element can only be put on a calculated
	 *         field of the owning object. If any incorrect configuration happens,
	 *         throws an Exception
	 */
	public DataObjectDefinition getOwnerObject();

	/**
	 * 
	 * @return the source code for the formula element
	 */
	public String generateFormulaElement();

	/**
	 * sets the triggers on the origin fields of the formula so that formula
	 * calculation is triggered when the field value changes. This can be on other
	 * objects
	 * 
	 * @param targetfield the origin field of formula
	 */
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath);

	/**
	 * @return all the trigger pathes for this formula element
	 */
	public CalculatedFieldTriggerPath[] getAllTriggerPaths();
}
