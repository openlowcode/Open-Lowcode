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
 * A conditional element is a pattern element that will be printed in the string
 * pattern based on a condition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConditionalElement
		extends
		PatternElement {
	private PatternElement truepattern;
	private PatternElement falsepattern;
	private ConditionForPattern condition;

	/**
	 * creates a conditional element
	 * 
	 * @param condition    a condition for printing this pattern element
	 * @param truepattern  pattern to use if condition is true (cannot be null)
	 * @param falsepattern pattern to use if condition is false (cannot be null)
	 */
	public ConditionalElement(ConditionForPattern condition, PatternElement truepattern, PatternElement falsepattern) {
		this.truepattern = truepattern;
		this.falsepattern = falsepattern;
		this.condition = condition;
	}

	@Override
	public String generateSource() {
		return "(" + condition.writeCondition() + "?" + truepattern.generateSource() + ":"
				+ falsepattern.generateSource() + ")";
	}

	@Override
	public String[] generateImport() {
		return new String[0];
	}

}
