/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

/**
 * An utility class to gather three objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of the first object
 * @param <F> type of the second object
 * @param <G> type of the third object
 */
public class Triple<E extends Object, F extends Object, G extends Object> {
	private E objectone;
	private F objecttwo;
	private G objectthree;

	/**
	 * creates a three element set
	 * 
	 * @param objectone   first object (of type E)
	 * @param objecttwo   second object (of type F)
	 * @param objectthree third object (of type G)
	 */
	public Triple(E objectone, F objecttwo, G objectthree) {
		super();
		this.objectone = objectone;
		this.objecttwo = objecttwo;
		this.objectthree = objectthree;
	}

	/**
	 * @return the first object
	 */
	public E getFirst() {
		return objectone;
	}

	/**
	 * @return the second object
	 */
	public F getSecond() {
		return objecttwo;
	}

	/**
	 * @return the third object
	 */
	public G getTriple() {
		return objectthree;
	}

}
