/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.access;

import java.io.IOException;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A privilege gives rights to execute all actions in an action group
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Privilege {
	private ActionGroup actiongroup;

	/**
	 * @return the action group this privilege applies to
	 */
	public ActionGroup getActiongroup() {
		return this.actiongroup;
	}

	/**
	 * creates a privilege for the given action group
	 * 
	 * @param actiongroup action group the privilege is giving privileges on
	 */
	public Privilege(ActionGroup actiongroup) {
		this.actiongroup = actiongroup;
	}

	/**
	 * method used to write all imports necessary for the privilege
	 * 
	 * @param sg            source generator
	 * @param contextaction context action of the privilege
	 * @throws IOException if anything bad happens in the writing of the imports
	 */
	public abstract void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException;

	/**
	 * @return the name of the class of the security manager
	 */
	public abstract String getSecurityManagerName();

	/**
	 * writes the definition of the privilege on the action
	 * 
	 * @param sg            source generator
	 * @param contextaction context action
	 * @throws IOException thrown if anything bad happens while writing the file
	 */
	public abstract void writeDefinition(SourceGenerator sg, ActionDefinition contextaction) throws IOException;

}
