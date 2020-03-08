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
 * A utility class to manage pair of objects with a given type
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.1
 * @param <E> first object type
 * @param <F> second object type
 */
public class Pair<E extends Object, F extends Object> {
	private E firstobject;
	private F secondobject;

	/**
	 * create a pair with the two objects as specified
	 * 
	 * @param firstobject  first object
	 * @param secondobject second object
	 */
	public Pair(E firstobject, F secondobject) {
		this.firstobject = firstobject;
		this.secondobject = secondobject;
	}

	/**
	 * @return the first object
	 */
	public E getFirstobject() {
		return firstobject;
	}

	/**
	 * @return the second object
	 */
	public F getSecondobject() {
		return secondobject;
	}

}
