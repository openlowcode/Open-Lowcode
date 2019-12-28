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

/**
 * A property extractor is providing a property from a given object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
@FunctionalInterface
public interface PropertyExtractor<E extends DataObject<E>> {
	/**
	 * @param dataobject actual data object
	 * @return the property extracted from the object
	 */
	public DataObjectProperty<E> extract(E dataobject);
}
