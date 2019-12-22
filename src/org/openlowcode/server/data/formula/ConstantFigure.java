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

import java.math.BigDecimal;

import org.openlowcode.server.data.DataObject;

/**
 * A constant figure in a calculation formula
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class ConstantFigure<E extends DataObject<E>> implements FormulaElement<E> {
	private BigDecimal value;

	/**
	 * Creates a ConstantFigure formula element
	 * 
	 * @param value the constant figure
	 */
	public ConstantFigure(BigDecimal value) {
		this.value = value;
	}

	@Override
	public BigDecimal getValueForFormulaInput(E contextobject) {
		return this.value;
	}

}
