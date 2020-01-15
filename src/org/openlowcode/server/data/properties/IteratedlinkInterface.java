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
 * the interface all objects with an iterated link property comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the link object
 * @param <F> the "left" object of the link
 * @param <G> the "right" object of the link
 */
public interface IteratedlinkInterface<
		E extends DataObject<E> & LinkobjectInterface<E, F, G>,
		F extends DataObject<F> & IteratedInterface<F>,
		G extends DataObject<G> & UniqueidentifiedInterface<G>> {
	/**
	 * @param leftobjectolditer archives the link with the given iteration of the
	 *                          left object. This is the iteration before the update
	 */
	public void archivethisiteration(Integer leftobjectolditer);
}
