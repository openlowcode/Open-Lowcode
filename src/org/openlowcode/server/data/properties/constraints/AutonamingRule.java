/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import org.openlowcode.server.data.DataObject;

/**
 * an auto-numbering rule generates a string automatically from an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object
 */
public abstract class AutonamingRule<E extends DataObject<E>> {
	/**
	 * @param object the object to generate the name on. Typically, auto-generated
	 *               names take some info from object fields
	 * @return the name as generated
	 */
	public abstract String generateName(E object);
}