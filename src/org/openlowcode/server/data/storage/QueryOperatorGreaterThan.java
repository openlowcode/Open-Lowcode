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
 * 'Greater ' Query Operator
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> payload class in the comparison
 */
public class QueryOperatorGreaterThan<E extends Object> extends QueryOperator<E> {

	@Override
	public boolean supportsNullPayload() {
		
		return false;
	}
	@Override
	public String toString() {
		return " '>' ";
	}
}
