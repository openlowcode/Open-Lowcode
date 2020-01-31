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

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * a switch condition to be used in a complex workflow switch
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SwitchCondition {

	/**
	 * write the imports for the switch condition
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during the code generation
	 */
	public abstract void writeimport(SourceGenerator sg, Module module) throws IOException;

	/**
	 * 
	 * 
	 * @param sg             source generator
	 * @param module         parent module
	 * @param parentclass    parent workflow class
	 * @param lifecycleclass parent object lifecycle class
	 * @param taskvariable   the name of the task variable field in the workflow
	 *                       code
	 * @throws IOException if anything bad happens during the code generation
	 */
	public abstract void writedeclaration(
			SourceGenerator sg,
			Module module,
			String parentclass,
			String lifecycleclass,
			String taskvariable) throws IOException;

	/**
	 * @param sg             source generator
	 * @param module         parent module
	 * @param switchtaskcode unique code of the task
	 * @throws IOException if anything bad happens during the code generation
	 */
	public abstract void writeNextSteps(SourceGenerator sg, Module module, String switchtaskcode) throws IOException;

}
