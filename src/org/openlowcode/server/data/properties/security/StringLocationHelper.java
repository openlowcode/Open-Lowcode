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
import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * A common class that will translate the number of Domains (as string) into
 * Object Ids
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object of the location helper
 */
public abstract class StringLocationHelper<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>>
		extends LocationHelper<E> {
	/**
	 * This method is used by string location helpers. In this method, the domain
	 * number is given. Then, it is transformed by the string location helper into a
	 * Domain id
	 * 
	 * @param object parent object
	 * @return the domain number as a Sttring
	 */
	public abstract String getObjectLocationNumber(E object);

	public DataObjectId<Domain> getObjectLocation(E object) {
		String domainnr = this.getObjectLocationNumber(object);
		Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerNr(domainnr);
		if (domain == null)
			throw new RuntimeException("Domain with nr " + domainnr + " not found");
		return domain.getId();
	}

}
