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

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 *         This interface sets the value of a field or property on an object
 *         without performing a specific persistence.
 * @param <E> data object
 * @param <F> payload of the field
 */
@FunctionalInterface
public interface DataSetterFromObject<E extends DataObject<E>, F extends Object> {

	/**
	 * sets the given value as payload in a field of the data object
	 * 
	 * @param object data object
	 * @param value  value to insert in payload
	 */
	public abstract void setValueOnObject(E object, F value);
}
