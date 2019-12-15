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

import java.util.ArrayList;

/**
 * A common class for implementation of an external field schema. This allows to
 * add on a business object values from a joint table.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public abstract class ExternalFieldSchemaTemplate<E extends Object> extends FieldSchema<E> {

	/**
	 * @param name name of the external field schema used in the query performed
	 */
	public ExternalFieldSchemaTemplate(String name) {
		super(name);

	}

	/**
	 * @return all the stored fields in this external field schema (it can be
	 *         several)
	 */
	public abstract ArrayList<StoredFieldSchema<E>> getExternalTableField();

}
