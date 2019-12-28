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
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;

/**
 * A formula element that sums some values on all children of the parent objecs
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent object
 * @param <F> child object
 */
public class SumOnChildren<E extends DataObject<E>, F extends DataObject<F>> implements FormulaElement<E> {
	private static Logger logger = Logger.getLogger(SumOnChildren.class.getName());
	private LinkedToChildrenNavigator<E, F> linkedtochildrennavigator;
	private FormulaElement<F> childobjectelement;

	/**
	 * creates a sum of children formula element
	 * 
	 * @param linkedtochildrennavigator navigator from a parent to all the relevant
	 *                                  children
	 * @param childobjectelement        element on the child object to sum
	 */
	public SumOnChildren(LinkedToChildrenNavigator<E, F> linkedtochildrennavigator,
			FormulaElement<F> childobjectelement) {
		this.linkedtochildrennavigator = linkedtochildrennavigator;
		this.childobjectelement = childobjectelement;
	}

	@Override
	public BigDecimal getValueForFormulaInput(E contextobject) {
		F[] childobjects = linkedtochildrennavigator.navigate(contextobject);
		BigDecimal result = new BigDecimal(0);
		if (childobjects != null)
			for (int i = 0; i < childobjects.length; i++) {
				F thischildobject = childobjects[i];
				BigDecimal element = childobjectelement.getValueForFormulaInput(thischildobject);
				if (element != null) {
					logger.fine(" --- *** --- summing element " + element.intValue());
					result = result.add(element);
				}
			}
		return result;
	}

}
