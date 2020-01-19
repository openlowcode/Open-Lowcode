/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A simple mapper providing a constant authority whatever the data object is
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the workflow is running on
 */
public class SimpleObjectToAuthorityMapper<E extends DataObject<E>>
		extends
		ObjectToAuthorityMapper<E> {
	private DataObjectId<Authority> constantauthority;

	/**
	 * creates a simple authority mapper
	 * 
	 * @param constantauthority authority to return for any object
	 */
	public SimpleObjectToAuthorityMapper(DataObjectId<Authority> constantauthority) {
		this.constantauthority = constantauthority;
	}

	@Override
	public DataObjectId<Authority> getAuthority(E object) {
		return constantauthority;
	}

}
