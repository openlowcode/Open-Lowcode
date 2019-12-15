/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;


/**
 * the field of an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StoredField<E extends Object> extends Field<E> {
	
	
	/**
	 * definition of the stored field
	 * @param fieldschema definition of the field
	 */
	public StoredField(StoredFieldSchema<E> fieldschema) {
		super(fieldschema);
	}
	
	@Override
	public StoredFieldSchema<E> getFieldSchema() {
		return (StoredFieldSchema<E>)this.fieldschema;
	}
}
