/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;
/**
 * An interface for function taking 3 arguments as parameters
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <T> first argument type
 * @param <U> second argument type
 * @param <V> third argument type
 * @since 1.11
 */
@FunctionalInterface
public interface TriConsumer<T,U,V> {
	/**
	 * @param first first argument
	 * @param second second argument
	 * @param third third argument
	 */
	public void apply(T first,U second,V third);

}
