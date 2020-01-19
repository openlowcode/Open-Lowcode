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

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A list of elements ordered by an integer index. Objects with the same integer
 * index are ordered by the sequence of insertion
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of object in the list
 */
public class OrderedList<E extends Object> {
	private TreeMap<Integer, ArrayList<E>> orderedlist;

	/**
	 * creates an empty ordered list
	 */
	public OrderedList() {
		orderedlist = new TreeMap<Integer, ArrayList<E>>();
	}

	/**
	 * insert an object with the given index
	 * 
	 * @param index  index
	 * @param object object to insert
	 */
	public void insertWithIndex(int index, E object) {
		Integer objectindex = new Integer(index);
		ArrayList<E> objectsatindex = orderedlist.get(objectindex);
		if (objectsatindex == null) {
			objectsatindex = new ArrayList<E>();
			orderedlist.put(objectindex, objectsatindex);
		}
		objectsatindex.add(object);
	}

	/**
	 * Returns, by order of greater to low, all the elements that have an index
	 * higher to or equal to the lowwatermark
	 * 
	 * @param lowwatermark
	 * @return
	 */
	public ArrayList<E> getOrderedList(int lowwatermark) {
		ArrayList<E> orderedanswer = new ArrayList<E>();
		Integer lastkey = orderedlist.lastKey();
		while (lastkey != null) {
			if (lastkey.intValue() >= lowwatermark) {
				ArrayList<E> elements = orderedlist.get(lastkey);
				orderedanswer.addAll(elements);
			} else {
				break;
			}
			lastkey = orderedlist.lowerKey(lastkey);
		}
		return orderedanswer;
	}

	/**
	 * gets an ordered list of all the elements
	 * 
	 * @return a full ordered list
	 */
	public ArrayList<E> getOrderedList() {
		ArrayList<E> orderedanswer = new ArrayList<E>();
		if (!orderedlist.isEmpty()) {
			Integer lastkey = orderedlist.lastKey();
			while (lastkey != null) {

				ArrayList<E> elements = orderedlist.get(lastkey);
				orderedanswer.addAll(elements);

				lastkey = orderedlist.lowerKey(lastkey);
			}
		}
		return orderedanswer;
	}
}
