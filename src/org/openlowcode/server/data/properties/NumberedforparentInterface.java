/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.server.data.DataObject;

/**
 * The interface all objects with a numbered for parent implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object
 * @param <F> parent data object of the current data object
 */
public interface NumberedforparentInterface<E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E> & NumberedforparentInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F>> {
	/**
	 * gets id of the parent object to be considered for number unicity
	 * 
	 * @return the parent id 
	 */
	public DataObjectId<F> getparentidfornumber();
}
