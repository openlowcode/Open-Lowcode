/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import java.util.function.Function;

/**
 * An interface to define a column or line criteria for an object in editable
 * tree table, with all functions included
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 * @param <F>
 * @since 1.7
 */
public interface ObjectDataElementKeyExtractor<E extends Object, F extends Object> {
	/**
	 * @return the object used as column or line criteria
	 */
	public Function<E, F> fieldExtractor();

	/**
	 * @return the key to use for uniqueness for the object
	 */
	public Function<F, String> keyExtractor();

	/**
	 * @return the display to use for line or column labels.
	 */
	public Function<F, String> labelExtractor();

	/**
	 * A function to define if a column should be an exception for horizontal
	 * exception
	 * 
	 * @return a function that tells if the key should be an exception for
	 *         horizontal exception (returns FALSE by default)
	 * @since 1.8
	 */
	public default Function<F, Boolean> HorizontalSumException() {
		return ((a) -> (new Boolean(false)));
	}

}
