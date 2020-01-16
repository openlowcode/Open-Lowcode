/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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
 * Interface all objects with an iterated sub object interface comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> sub object
 * @param <F> parent of the subobject
 */
public interface IteratedsubobjectInterface<
		E extends DataObject<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & IteratedInterface<F>> {
	/**
	 * archives this version of the subobject before an update or delete. Also
	 * creates an iteration of the parent object
	 * 
	 * @param leftobjectolditer last iteration of the parent object for which the
	 *                          current subobject is valid
	 */
	public void archivethisiteration(Integer leftobjectolditer);
}
