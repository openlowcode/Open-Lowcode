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
import org.openlowcode.server.data.properties.LinkobjectInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * This navigates from the link object to the unique left objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 * @param <E> left object for link
 * @param <F> link object
 * @param <G> right object for link
 */
@FunctionalInterface
public interface LinkToLeftReverseNavigator<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends ObjectNavigator<F, E> {
	public E[] navigate(F rightobject);
}
