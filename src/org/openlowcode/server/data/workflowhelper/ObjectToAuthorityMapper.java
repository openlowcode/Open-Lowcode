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
 * A common class for utilities that allow a mapping from a data object to an
 * authority. Typically, different objects in workflows are assigned to
 * different authorities (say, a helpdesk ticket assigned to specialist of
 * product A or B). This is done using a helper subclass of this class
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object that is subject to the workflow
 */
public abstract class ObjectToAuthorityMapper<E extends DataObject<E>> {
	/**
	 * gets the authority for a data object, according to data object content
	 * 
	 * @param object the data object
	 * @return the authority for this data object
	 */
	public abstract DataObjectId<Authority> getAuthority(E object);
}
