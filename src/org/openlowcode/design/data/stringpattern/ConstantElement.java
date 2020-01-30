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

/**
 * creates a constant element with pre-defined string content that will be the
 * same on all objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstantElement
		extends
		PatternElement {
	private String constant;

	/**
	 * creates a constant pattern element with the given text as constant
	 * 
	 * @param constant constant text
	 */
	public ConstantElement(String constant) {
		if (constant == null)
			throw new RuntimeException("null constant not allowed");
		this.constant = constant;
	}

	/**
	 * @return the length of the constant text in this pattern element
	 */
	public int getLength() {
		return this.constant.length();
	}

	@Override
	public String generateSource() {

		return "\"" + constant + "\"";
	}

	@Override
	public String[] generateImport() {
		return new String[0];
	}

}
