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

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.tools.misc.Named;

/**
 * A property business rules precises or enriches the behaviour of a property
 * while keeping the same interfaces
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class PropertyBusinessRule<E extends Property<E>>
		extends
		Named {

	/**
	 * creates a property business rule
	 * 
	 * @param name            name of the business rule (should be unique for the
	 *                        property)
	 * @param userimplemented if a helper has to be written by the user
	 */
	public PropertyBusinessRule(String name, boolean userimplemented) {
		super(name);
		this.userimplemented = userimplemented;
	}

	private boolean userimplemented;

	/**
	 * @return true if the business rule requires a user-implemented helper
	 */
	public boolean isUserimplemented() {
		return userimplemented;
	}

	/**
	 * @param sg source generator
	 * @throws IOException if anything bad happens while writing the file
	 */
	public abstract void writeInitialization(SourceGenerator sg) throws IOException;

	/**
	 * @return the list of import statements for the business rule
	 */
	public abstract String[] getImportstatements();

	/**
	 * This method will be executed before generation to check that everything is
	 * valid for the most complex properties
	 * 
	 * @param parentproperty property
	 * @since 1.14
	 */
	public void checkBeforeGeneration(Property<?> parentproperty) {

	}

}
