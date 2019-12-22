/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

/**
 * A class wrapping two data objects together. This is typically used for the
 * result of queries bringing array of pair of objects, such as a pair of links
 * and right objects.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of the first dataobject
 * @param <F> type of the second dataobject
 */
public class TwoDataObjects<E extends DataObject<E>, F extends DataObject<F>> {
	private E objectone;
	private F objecttwo;

	/**
	 * @return first object
	 */
	public E getObjectOne() {
		return objectone;
	}

	/**
	 * @return second object
	 */
	public F getObjectTwo() {
		return objecttwo;
	}

	/**
	 * creates a TwoDataObject
	 * @param objectone first object
	 * @param objecttwo second object
	 */
	public TwoDataObjects(E objectone, F objecttwo) {
		this.objectone = objectone;
		this.objecttwo = objecttwo;
	}
}
