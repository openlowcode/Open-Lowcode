/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.control;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A data control element will check the content of a data object and blows an
 * error if the condition is not met
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class DataControlElement {
	private DataObjectDefinition mainobject;

	/**
	 * creates a data control for the data object
	 * 
	 * @param mainobject data object subkect to control
	 */
	public DataControlElement(DataObjectDefinition mainobject) {
		this.mainobject = mainobject;
	}

	/**
	 * writes the rule in source code of the object
	 * 
	 * @param sg          source generation
	 * @param module      parent module
	 * @param objectclass the class of the data object
	 * @throws IOException if anything bad happens during writing the file
	 */
	public abstract void writeRule(SourceGenerator sg, Module module, String objectclass) throws IOException;

	/**
	 * @return get the data object the control is running on
	 */
	public DataObjectDefinition getMainobject() {
		return mainobject;
	}

}
