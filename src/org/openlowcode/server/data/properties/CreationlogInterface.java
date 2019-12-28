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

import java.util.Date;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.data.DataObject;

/**
 * interface any object having a creation log property should implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object this creation log property is set
 */
public interface CreationlogInterface<E extends DataObject<E>> extends StoredobjectInterface<E> {
	/**
	 * gets the id of the AppUser who created the object
	 * @return the userid
	 */

	public DataObjectId<Appuser> getCreateuserid();

	/**
	 * 
	 * @return the date the object cas created
	 */
	public Date getCreatetime();

}
