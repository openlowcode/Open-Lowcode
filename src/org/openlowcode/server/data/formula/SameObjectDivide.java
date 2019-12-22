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
import java.math.RoundingMode;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;

/**
 * A Division of two values on the same object. As any formula element, it should
 * be robust to null values (Product of two values if one is null returns null
 * also). It is also robust to division by zero (returns null)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the DataObject this element applies to
 */
public class SameObjectDivide<E extends DataObject<E>> implements FormulaElement<E> {
	private static Logger logger = Logger.getLogger(SameObjectDivide.class.getName());
	private FormulaElement<E> element1;
	private FormulaElement<E> element2;

	/**
	 * creates a formula element dividing the two elements provided
	 * 
	 * @param element1 an alement on the object
	 * @param element2 another element on the object
	 */
	public SameObjectDivide(FormulaElement<E> element1, FormulaElement<E> element2) {
		this.element1 = element1;
		this.element2 = element2;
	}

	@Override
	public BigDecimal getValueForFormulaInput(E contextobject) {
		if (((element2.getValueForFormulaInput(contextobject) != null)
				&& (element1.getValueForFormulaInput(contextobject) != null))
				&& (element2.getValueForFormulaInput(contextobject).compareTo(BigDecimal.ZERO) != 0)) {
			BigDecimal result = element1.getValueForFormulaInput(contextobject)
					.divide(element2.getValueForFormulaInput(contextobject), RoundingMode.HALF_DOWN);
			logger.fine(" Dividing two elements of same object " + contextobject.getName() + " result = " + result);

			return result;
		} else {
			logger.fine(" Division is null as one of the elements is null for " + contextobject.getName());

			return null;
		}

	}
}
