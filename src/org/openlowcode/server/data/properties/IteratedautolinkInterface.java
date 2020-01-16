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
 * interface all objects with iterated auto-link property implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the auto-link
 * @param <F> data object referenced by the auto-link
 */
public interface IteratedautolinkInterface<
		E extends DataObject<E> & AutolinkobjectInterface<E, F>,
		F extends DataObject<F> & IteratedInterface<F>> {
	/**
	 * archives the link
	 * 
	 * @param leftobjectolditer the last iteration of the left object on which the
	 *                          link is present
	 */
	public void archivethisiteration(Integer leftobjectolditer);
}
