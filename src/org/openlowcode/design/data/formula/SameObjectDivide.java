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
 * a formula dividing two elements in the same object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SameObjectDivide
		implements
		FormulaDefinitionElement {
	private FormulaDefinitionElement element1;
	private FormulaDefinitionElement element2;

	/**
	 * creates a formula element dividing two fields on the same object
	 * 
	 * @param element1 the element to be divised (e.g. 5 in " 5 / 2 = 2.5")
	 * @param element2 the number to divide into (e.g. 2 in " 5 / 2 = 2.5")
	 */
	public SameObjectDivide(FormulaDefinitionElement element1, FormulaDefinitionElement element2) {
		this.element1 = element1;
		this.element2 = element2;
		if (this.element1.getOwnerObject() != null)
			if (this.element2.getOwnerObject() != null)
				if (!this.element1.getOwnerObject().equals(this.element2.getOwnerObject()))
					throw new RuntimeException(
							"The formula includes two elements from incompatible objects , element1 = "
									+ element1.toString() + ", element2 = " + element2.toString());

	}

	@Override
	public DataObjectDefinition getOwnerObject() {
		if (this.element1.getOwnerObject() != null)
			return this.element1.getOwnerObject();
		if (this.element2.getOwnerObject() != null)
			return this.element2.getOwnerObject();
		return null;
	}

	@Override
	public String generateFormulaElement() {
		return "new SameObjectDivide(" + element1.generateFormulaElement() + "\n\t\t,"
				+ element2.generateFormulaElement() + ")";

	}

	@Override
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath) {
		element1.setTriggersOnSourceFields(triggerpath);
		element2.setTriggersOnSourceFields(triggerpath);

	}

	@Override
	public CalculatedFieldTriggerPath[] getAllTriggerPaths() {
		return new CalculatedFieldTriggerPath[0];
	}

}
