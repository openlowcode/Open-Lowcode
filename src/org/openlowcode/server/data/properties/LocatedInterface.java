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

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.server.data.DataObject;

/**
 * The interface implemented by all objects having the Located property. It
 * allows access to the location (Domain). Location is used mostly for user &
 * access rights
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the dataobject this property applies to
 */
public interface LocatedInterface<E extends DataObject<E> & UniqueidentifiedInterface<E>> {

	/**
	 * sets the location of this object to the provided location id.
	 * 
	 * @param locationid id of the domain (location)
	 */
	public void setlocation(DataObjectId<Domain> locationid);

	/**
	 * get the location of the object
	 * 
	 * @return the id of the domain (location)
	 */
	public DataObjectId<Domain> getLocationdomainid();

}
