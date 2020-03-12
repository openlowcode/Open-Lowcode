/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A data object definition implements this interface if it manages conditions
 * on alias based on values on parent object but no choice of export type based
 * on parameter selected by the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent object on which filter will happen
 */
public interface SpecificAliasListWithParentWithoutParameter<E extends DataObject<E>> {
	/**
	 * @param parent parent object
	 * @return the validated alias list
	 */
	public String[] getSpecificAliasList(DataObjectId<E> parent);

}
