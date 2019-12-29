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
 * The interface all data objects with Updatelog property should comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public interface UpdatelogInterface<E extends DataObject<E>>
		extends StoredobjectInterface<E>, UniqueidentifiedInterface<E> {
	/**
	 * @return gets the objectid of the Appuser
	 */
	public DataObjectId<Appuser> getUpdateuserid();

	/**
	 * @return gets the update time
	 */
	public Date getUpdatetime();

}
