/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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
 * This interface is providing a template to extract some data from an object by querying for it in the database
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
  *
 * @param <E> a data object
 * @param <F> a field from the object
 */
@FunctionalInterface
public interface DataExtractor<E extends DataObject<E>,F extends Object> {
	/**
	 * @param object
	 * @return the field
	 * @throws GalliumException
	 */
	public F extract(DataObjectId<E> object);
}
