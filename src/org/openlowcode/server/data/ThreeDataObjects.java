/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

/**
 * a class gathering three data objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> first data object type
 * @param <F> second data object type
 * @param <G> third data object type
 */
public class ThreeDataObjects<E extends DataObject<E>, F extends DataObject<F>, G extends DataObject<G>> {
	private E objectone;
	private F objecttwo;
	private G objectthree;

	/**
	 * @return first data object
	 */
	public E getObjectOne() {
		return objectone;
	}

	/**
	 * @return second data object
	 */
	public F getObjectTwo() {
		return objecttwo;
	}

	/**
	 * @return third data object
	 */
	public G getObjectThree() {
		return objectthree;
	}

	/**
	 * create a three data object
	 * 
	 * @param objectone   first data object
	 * @param objecttwo   second data object
	 * @param objectthree third data object
	 */
	public ThreeDataObjects(E objectone, F objecttwo, G objectthree) {
		this.objectone = objectone;
		this.objecttwo = objecttwo;
		this.objectthree = objectthree;
	}
}
