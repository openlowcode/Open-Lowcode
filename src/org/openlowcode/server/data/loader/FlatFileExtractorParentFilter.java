/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;

/**
 * a filter for parents during a loading
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> main loading data object
 * @param <F> parent data object
 */
public interface FlatFileExtractorParentFilter<E extends DataObject<E>, F extends DataObject<F>> {

	/**
	 * checks if the given parent is valid
	 * 
	 * @param loadedobjectdefinition definition of the object to load
	 * @param alias                  alias
	 * @param parent                 parent
	 * @return true if the parent is valid for the loading context
	 */
	public boolean isvalid(DataObjectDefinition<E> loadedobjectdefinition, String alias, F parent);

}
