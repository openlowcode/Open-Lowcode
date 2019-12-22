/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.formula;

import org.openlowcode.server.data.DataObject;

/**
 * an interface to extract a calculated field from an object. This is used when
 * setting-up formulas
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of object this element is part of
 */
@FunctionalInterface
public interface CalculatedFieldExtractor<E extends DataObject<E>> {
	/**
	 * @param object the parent data object
	 * @return the calculated field
	 */
	@SuppressWarnings("rawtypes")
	public CalculatedField extractField(E object);

}
