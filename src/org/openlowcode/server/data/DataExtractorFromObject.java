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
 * This interface provides a template to extract in memory information from a
 * data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> a data object
 * @param <F> a field from the object
 */
public interface DataExtractorFromObject<E extends DataObject<E>, F extends Object> {
	/**
	 * @param object the object (may not be persisted)
	 * @return the field
	 */
	public F extract(E object);
}
