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
 * The interface any object implementing the Numbered property complies to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object type
 */
public interface NumberedInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
	/**
	 * @return the number of this object
	 */
	public String getNr();

	/**
	 * change the number of the object and persists the change
	 * 
	 * @param nr the new number
	 */
	public void setobjectnumber(String nr);
}
