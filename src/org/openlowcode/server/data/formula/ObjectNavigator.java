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



/**
 * an object navigator allows to navigate from one object to another inside the formula
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the input object for the navigation
 * @param <F> the output object for the navigation
 */
@FunctionalInterface
public interface ObjectNavigator<E extends DataObject<E>,F extends DataObject<F>> {
	/**
	 * executes the navigation at the time of calculation of the formula
	 * @param object input object
	 * @return output objects of the navigation
	 */
	public F[] navigate(E object);

}
