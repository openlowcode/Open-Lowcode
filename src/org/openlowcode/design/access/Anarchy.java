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
 * Anarchy is a type of privilege that gives access to all connected valid users
 * to the action. This is typically recommended for actions that are part of the
 * framework, such as task page.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Anarchy
		extends
		AuthorityPrivilege {

	/**
	 * creates an anarchy for the given action group. Everybody will have access
	 * 
	 * @param actiongroup the action group anarchy access is given to
	 */
	public Anarchy(ActionGroup actiongroup) {
		super(actiongroup, null);

	}

	@Override
	public void writeDefinition(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		sg.wl("		private static ActionSecurityManager anarchy = new ActionAnarchySecurityManager();");

	}

	@Override
	public String getSecurityManagerName() {
		return "anarchy";
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		sg.wl("import org.openlowcode.server.security.ActionAnarchySecurityManager;");

	}

	@Override
	public boolean equals(Object otherobject) {
		if (otherobject == null)
			return false;
		if (!(otherobject instanceof Anarchy))
			return false;
		return true;
	}

}
