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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.LocatedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * This location helper allows to systematically define the location as a
 * constant domain
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object for which this location helper is created
 */
public class SameLocationHelper<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>>
		extends StringLocationHelper<E> {
	private String domainnr;

	/**
	 * creates a same location helper with the given domain number
	 * 
	 * @param domainnr domain number
	 */
	public SameLocationHelper(String domainnr) {
		this.domainnr = domainnr;
	}

	@Override
	public String getObjectLocationNumber(E object) {
		return this.domainnr;
	}

}
