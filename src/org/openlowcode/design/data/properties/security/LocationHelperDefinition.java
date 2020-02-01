/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.security;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A location helper defines how objects that are located are put in a domain at
 * creation and update time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class LocationHelperDefinition {
	private DataObjectDefinition parent;

	/**
	 * @return the data object the helper is working on
	 */
	public DataObjectDefinition getParent() {
		return this.parent;
	}

	/**
	 * create a location helper for the data object
	 * 
	 * @param parent data object
	 */
	public LocationHelperDefinition(DataObjectDefinition parent) {
		this.parent = parent;
	}

	/**
	 * generates the java code to configure the location helper
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public abstract void generateLocationHelper(SourceGenerator sg, Module module) throws IOException;
}
