/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.specificstorage;

import org.openlowcode.server.data.storage.Field;

/**
 * An external field is a field that is deduced from linked entities (data
 * objects) from the main object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of payload
 */
public class ExternalField<E extends Object> extends Field<E> {

	/**
	 * creates an external field given the provided schema
	 * @param schema external field schema
	 */
	public ExternalField(ExternalFieldSchema<E> schema) {
		super(schema);

	}
	
	@Override
	public ExternalFieldSchema<E> getFieldSchema() {
		return (ExternalFieldSchema<E>) this.fieldschema;
	}
}
