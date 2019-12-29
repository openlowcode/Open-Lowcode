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
 * the interface all Named objects are implementing
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public interface NamedInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
	/**
	 * gets the object name
	 * 
	 * @return the object name
	 */
	public String getObjectname();

	/**
	 * sets the object name
	 * 
	 * @param name the object name
	 */
	public void setobjectname(String name);
}
