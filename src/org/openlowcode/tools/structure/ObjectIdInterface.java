/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

/**
 * Defines interface for business object id. This contains the actualy id, and
 * the type of the business object (called object id)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface ObjectIdInterface {
	/**
	 * @return the id (e.g. your passport number)
	 */
	public String getId();

	/**
	 * @return the id of the type of object (e.g. French citizen)
	 */
	public String getObjectId();

}
