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
 * An element of a pattern to generate an automatic name and number
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public abstract class PatternElement {
	/**
	 * @return the source for the pattern element generation
	 */
	public abstract String generateSource();

	/**
	 * @return the class imports that are required for this pattern element
	 */
	public abstract String[] generateImport();
}
