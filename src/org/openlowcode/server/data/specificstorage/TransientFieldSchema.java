/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.specificstorage;

import org.openlowcode.server.data.storage.FieldSchema;

/**
 * field schema for a transient field that is not persisted into the database
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of payload
 */
public abstract class TransientFieldSchema<E extends Object>
		extends
		FieldSchema<E> {

	/**
	 * create a transient field schema
	 * 
	 * @param name name of the field schema (should be unique for the object)
	 */
	public TransientFieldSchema(String name) {
		super(name);
	}

}
