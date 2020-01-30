/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.stringpattern;

import java.util.ArrayList;

/**
 * A string pattern allows to generate automatically a string based on an
 * object. It is typically used to generate unique identifiers
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringPattern {
	private ArrayList<PatternElement> composition;

	/**
	 * creates an empty string pattern
	 */
	public StringPattern() {
		composition = new ArrayList<PatternElement>();
	}

	/**
	 * @param element adds an element to a String pattern
	 */
	public void addPatternElement(PatternElement element) {
		composition.add(element);
	}

	/**
	 * @return the number of pattern elements in this string pattern
	 */
	public int getElementNumber() {
		return composition.size();
	}

	/**
	 * get the pattern element at the given index
	 * 
	 * @param index a number betweeen 0 (included) and getElementNumber (excluded)
	 * @return the pattern element at the given index
	 */
	public PatternElement getElement(int index) {
		return composition.get(index);
	}
}
