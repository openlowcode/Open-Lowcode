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
public abstract class AutonumberingRule<E extends DataObject<E>> {
	/**
	 * generates automatically a number
	 * 
	 * @param object object to generate number on
	 * @return the number as generated
	 */
	public abstract String generateNumber(E object);

	/**
	 * if number is ordered as a number (digit), then, order follows the number
	 * rank, not alphabetical (e.g. N-1...,N-9,N-10,N-11). In the example, the
	 * offset is 2 characters ( 'N-' ).
	 * 
	 * @return true if ordered as numbered
	 */
	public abstract boolean orderedAsNumber();

	/**
	 * 
	 * @return the number of characters to discard when ordering as a number
	 */
	public abstract int getNumberOffset();
}
