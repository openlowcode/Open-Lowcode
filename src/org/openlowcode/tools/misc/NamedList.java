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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class to store objects identified by a unique name. This class
 * offers both the capability to access the data as orderedlist, and to lookup
 * the data by name.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class NamedList<E extends NamedInterface> {
	private HashMap<String, E> namedlist;
	private ArrayList<E> sequencelist;

	public List<E> getFullList() {
		ArrayList<E> copy = new ArrayList<E>();
		copy.addAll(sequencelist);
		return copy;
	}
	
	
	/**
	 * @return a string that will include all the names of the objects in the
	 *         NamedList
	 */
	public String dropNameList() {
		if (namedlist == null)
			return "[null]";
		StringBuffer dropname = new StringBuffer("[");
		Iterator<String> keylist = namedlist.keySet().iterator();
		while (keylist.hasNext()) {
			dropname.append(keylist.next());
			dropname.append('|');
		}
		dropname.append(']');
		return dropname.toString();
	}

	/**
	 * @param name name to look at. The name should be the cleaned version
	 * @return the object if it exists, or null if no object with that name exists
	 */
	public E lookupOnName(String name) {
		return namedlist.get(name);
	}

	/**
	 * @return the number of elements in the list
	 */
	public int getSize() {
		return sequencelist.size();
	}

	/**
	 * @param index the index in the list, from 0 (included) to the list size
	 *              (excluded)
	 * @return the object at the position. The object can be null
	 */
	public E get(int index) {
		return sequencelist.get(index);
	}

	/**
	 * This method adds the element if it does not exist yet. Else, it executes
	 * gracefully without throwing an exception
	 * 
	 * @param element
	 * @return true if the element was added, false else
	 */
	public boolean addIfNew(E element) {
		String elementname = (element != null ? element.getName() : null);
		if (namedlist.get(elementname) == null) {
			add(element);
			return true;
		}
		return false;
	}

	/**
	 * adds the element if it does not exist yet, else throws an exception
	 * 
	 * @param element the element to add
	 */
	public void add(E element) {
		if (element == null) { // to allow partly empty lists, only added in sequence list
			sequencelist.add(null);
			return;
		}
		String elementname = element.getName();
		if (namedlist.get(elementname) != null) {
			throw new RuntimeException("Name is not unique " + elementname + ", full list : " + dropNameList());
		}
		namedlist.put(elementname, element);
		sequencelist.add(element);
	}

	/**
	 * Creates a new empty NamedList
	 */
	public NamedList() {
		namedlist = new HashMap<String, E>();
		sequencelist = new ArrayList<E>();
	}

	/**
	 * Merges two lists. The operation will complete if the two lists have no
	 * conflicting elements. Else, an exception will be thrown at some point during
	 * the merge
	 * 
	 * @param otherlist the list to merge with this one
	 */
	public void mergeWithNamedList(NamedList<E> otherlist) {
		if (otherlist != null) {
			for (int i = 0; i < otherlist.getSize(); i++) {
				this.add(otherlist.get(i));
			}
		}
	}

	/**
	 * Merges with the current list, while removing duplicates. The elements kept
	 * are the one of the current list
	 * 
	 * @param otherlist the list to merge with this one
	 */
	public void mergeWithNamedListIfNotExist(NamedList<E> otherlist) {
		if (otherlist != null) {
			for (int i = 0; i < otherlist.getSize(); i++) {
				this.addIfNew(otherlist.get(i));
			}
		}
	}

	/**
	 * generates in one line a one element list.
	 * 
	 * @param singleelt the element
	 * @return a one element list with the element provided
	 */
	public <D extends Named> NamedList<D> OneEltList(D singleelt) {
		NamedList<D> returnlist = new NamedList<D>();
		returnlist.add(singleelt);
		return returnlist;
	}
}
