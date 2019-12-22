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

import org.openlowcode.server.data.DataObject;

import org.openlowcode.tools.misc.NamedList;

/**
 * A formula element is a data object element (field or property) that can be
 * used for calculation. There should be a method that provides a big decimal
 * for calculation.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class Formula<E extends DataObject<E>> {
	private FormulaElement<E> headelement;
	private CalculatedFieldExtractor<E> extractor;

	public Formula(CalculatedFieldExtractor<E> extractor, FormulaElement<E> headelement) {
		this.headelement = headelement;
		this.extractor = extractor;
	}

	/**
	 * compute method for a formula.
	 * 
	 * @param contextobject the object to execute the calculation on
	 * @return the triggers that were raised by this calculation
	 */
	@SuppressWarnings("unchecked")
	public NamedList<DataUpdateTrigger<E>> compute(E contextobject) {
		return extractor.extractField(contextobject)
				.UpdateValueWithCalculationResult(headelement.getValueForFormulaInput(contextobject));

	}
}
