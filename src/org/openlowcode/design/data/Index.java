/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.util.ArrayList;

import org.openlowcode.tools.misc.Named;

/**
 * A binary search index on a stored element. The indexes can be unique, though
 * this is not recommended, as it may have side effects in many special
 * situations
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Index
		extends
		Named {
	private ArrayList<StoredElement> fields;
	private boolean unique;

	/**
	 * creates an index with one stored element
	 * 
	 * @param name          unique name of the index (should be short and valid sql
	 *                      and java element)
	 * @param simpleelement the stored element that is indexed
	 * @param unique        true if index is unique
	 */
	public Index(String name, StoredElement simpleelement, boolean unique) {
		super(name);
		this.fields = new ArrayList<StoredElement>();
		this.fields.add(simpleelement);
	}

	/**
	 * creates an index with two stored elements
	 * 
	 * @param name     unique name of the index (should be short and valid sql and
	 *                 java element)
	 * @param element1 first element for the index (should be the most discriminant
	 *                 and / or used)
	 * @param element2 second element for the index
	 * @param unique   true if index is unique
	 */
	public Index(String name, StoredElement element1, StoredElement element2, boolean unique) {
		super(name);
		this.fields = new ArrayList<StoredElement>();
		this.fields.add(element1);
		this.fields.add(element2);
	}

	/**
	 * creates an index with several fields
	 * 
	 * @param name   unique name of the index (should be short and valid sql and
	 *               java element)
	 * @param fields a list of fields (will be indexed in the order proided
	 * @param unique true if index is unique
	 */
	public Index(String name, ArrayList<StoredElement> fields, boolean unique) {
		super(name);
		this.fields = new ArrayList<StoredElement>(fields);
		this.unique = unique;
	}

	/**
	 * @return the number of stored elements
	 */
	public int getStoredElementNumber() {
		return fields.size();
	}

	/**
	 * return the stored element at the given index
	 * 
	 * @param index an index between 0 (included) and getStoredElementNumber
	 *              (excluded)
	 * @return the StoredElement at the given index
	 */
	public StoredElement getFieldAtIndex(int index) {
		return fields.get(index);
	}

	/**
	 * @return true if the index will enforce unicity
	 */
	public boolean isUnique() {
		return this.unique;
	}

}
