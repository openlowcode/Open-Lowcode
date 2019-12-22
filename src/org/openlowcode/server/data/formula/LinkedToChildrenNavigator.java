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
 * A method allowing to navigate from an object to all of its children, to be
 * used in calculation formulas
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent object
 * @param <F> the children of the parent
 */
@FunctionalInterface
public interface LinkedToChildrenNavigator<E extends DataObject<E>,F extends DataObject<F>> extends ObjectNavigator<E,F> {
	@Override
	public F[] navigate(E parent);
}
