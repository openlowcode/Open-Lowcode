/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.security;

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LocatedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * A location helper will provide the domain for a given object/ Typically, the
 * domain is generated from some fields entered by the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object the helper is providing service on
 */
public abstract class LocationHelper<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>> {
	/**
	 * gets the id of the domain for the given object
	 * 
	 * @param object data object
	 * @return the object id of the domain
	 */
	public abstract DataObjectId<Domain> getObjectLocation(E object);
}
