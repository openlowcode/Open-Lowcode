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
 * A formula element is a data object element (field or property) that can be used for calculation. There
 * should be a method that provides a big decimal for calculation.
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
@FunctionalInterface
public interface FormulaElement<E extends DataObject<E>> {
	/**
	 * provides the value as big decimal for formula calculation
	 * @return a big decimal value of the content of the object element
	 */
	public BigDecimal getValueForFormulaInput(E contextobject); 

}
