/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A selector of users for the task (depending on object attributes)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class TaskUserSelector {
	/**
	 * @return the object to query
	 */
	public abstract DataObjectDefinition getObjectForQuery();

	/**
	 * writes the code to define the user selector
	 * 
	 * @param sg                source generation
	 * @param module            parent module
	 * @param parentobjectclass class of the parent data object
	 * @param lifecycleclass    class of the lifecycle of the parent data object
	 * @param taskcodelowercase the task code in lower case
	 * @throws IOException if anything bad happens while generating the code
	 */
	public abstract void writeSelectorDeclaration(
			SourceGenerator sg,
			Module module,
			String parentobjectclass,
			String lifecycleclass,
			String taskcodelowercase) throws IOException;
}
