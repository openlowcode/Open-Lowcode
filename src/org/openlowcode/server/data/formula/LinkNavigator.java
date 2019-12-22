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
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.LinkobjectInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * A method allowing to navigate a link. It starts from the left object, and
 * returns a series of two data objects with the link and right object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left object for the link
 * @param <F> the link object
 * @param <G> right object for the link
 */
@FunctionalInterface
public interface LinkNavigator<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> {
	/**
	 * @param leftobject the input left object
	 * @return an array of sets of link object and right object
	 */
	public TwoDataObjects<F, G>[] getLinksAndRightObjects(E leftobject);
}
