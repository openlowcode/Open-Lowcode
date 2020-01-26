/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A calculation element is contributing to a formula for all elements in the
 * object tree below. Typically, it is a number on a parent object used to
 * multiply the values on children objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */

public abstract class CalculationElement {

	/**
	 * gets the relevant object for this calculation element
	 * 
	 * @return the parent object for the element, or null if no element defined
	 */
	public abstract DataObjectDefinition getParent();

	/**
	 * writes the multiplier for the calculation element
	 * 
	 * @param sg          source generator
	 * @param extraindent indent to write the source
	 * @param prefix      prefix of the calculation element
	 * @throws IOException if anything bad happens while writing the source code
	 */
	protected abstract void writeMultiplier(SourceGenerator sg, String extraindent, String prefix) throws IOException;

}
