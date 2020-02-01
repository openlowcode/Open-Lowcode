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

import java.math.BigDecimal;

import org.openlowcode.design.data.DataObjectDefinition;

/**
 * creates a constant figure in the formula
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstantFigure
		implements
		FormulaDefinitionElement {
	private BigDecimal constant;

	/**
	 * creates a constant figure formula element with the given Big Decimal
	 * 
	 * @param constant constant
	 */
	public ConstantFigure(BigDecimal constant) {
		this.constant = constant;
	}

	@Override
	public DataObjectDefinition getOwnerObject() {

		return null;
	}

	@Override
	public String generateFormulaElement() {
		return "new ConstantFigure(new BigDecimal(" + constant.doubleValue() + "))";
	}

	@Override
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath) {
		// Do nothing

	}

	@Override
	public CalculatedFieldTriggerPath[] getAllTriggerPaths() {
		return new CalculatedFieldTriggerPath[0];
	}

}
