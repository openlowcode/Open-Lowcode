/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

/**
 * A simple query condition of the form 'FIELD = VALUE' 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.14
 */
public class SimpleEqualQueryCondition<E extends Object> extends SimpleQueryCondition<E> {

	/**
	 * @param alias    tableschema alias
	 * @param field    the field in the query condition
	 * @param payload  payload to compare the content to
	 */
	public SimpleEqualQueryCondition(
			TableAlias alias,
			StoredFieldSchema<E> field,
			E payload) {
		super(alias, field, new QueryOperatorEqual<E>(), payload);
	}

}
