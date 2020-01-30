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
 * A condition for printing a pattern. It can be based on the object content
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ConditionForPattern {
	/**
	 * writes the source code for a statement returning a boolean
	 * 
	 * @return the source code to be inserted in automatically generated class
	 */
	public abstract String writeCondition();
}
