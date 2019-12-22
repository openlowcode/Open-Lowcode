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
import org.openlowcode.tools.misc.NamedList;

/**
 * An interface for a calculated field.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */

public interface CalculatedField<E extends DataObject<E>> {
	/**
	 * when this method is triggered, a calculated field will update its own value,
	 * and trigger calculation of other fields
	 * 
	 * @param value the new value
	 * @return the list of triggers that were triggered by the calculation of this
	 *         field;
	 */
	public NamedList<DataUpdateTrigger<E>> UpdateValueWithCalculationResult(BigDecimal value);
}
