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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of parent object
 */
public interface MultidimensionchildInterface<E extends DataObject<E> & UniqueidentifiedInterface<E>,F extends DataObject<F> & UniqueidentifiedInterface<F>> extends UniqueidentifiedInterface<E> {
	public void setmultidimensionparentidwithoutupdate(DataObjectId<F> parentid);
}
