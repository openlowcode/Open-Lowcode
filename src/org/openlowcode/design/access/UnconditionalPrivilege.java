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
import org.openlowcode.design.generation.StringFormatter;

/**
 * An unconditional privilege gives unlimited access to an action group to an
 * authority whatever the context.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UnconditionalPrivilege
		extends
		AuthorityPrivilege {
	private String classname;
	private String variablename;

	/**
	 * creates an unconditional privilege for the given action group for the given
	 * authority
	 * 
	 * @param actiongroup action group the privilege gives access to
	 * @param authority   authority the privilege is assigned to
	 */
	public UnconditionalPrivilege(ActionGroup actiongroup, TotalAuthority authority) {
		super(actiongroup, authority);
		if (authority == null)
			throw new RuntimeException(" authority is null ");
		if (authority.getName() == null)
			throw new RuntimeException(" authority name is null ");

		classname = StringFormatter.formatForJavaClass(this.getAuthority().getName()) + "TotalSecurityManager";
		variablename = StringFormatter.formatForAttribute(this.getAuthority().getName()) + "totalsecuritymanager";
	}

	@Override
	public void writeDefinition(SourceGenerator sg, ActionDefinition contextaction) throws IOException {

		sg.wl("		// Security Manager section");
		sg.wl("");
		sg.wl("		private static GalliumActionSecurityManager " + variablename + " = new " + classname + "();");
		sg.wl("		private static class " + classname + " extends GalliumActionTotalSecurityManager {");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public void fillRelevantAuthorities(");
		sg.wl("					ArrayList<String> relevantauthorities) {");
		sg.wl("				relevantauthorities.add(\""
				+ (this.getAuthority().getModule() != null ? this.getAuthority().getModule().getCode() + "_" : "")
				+ this.getAuthority().getName() + "\");");
		sg.wl("");
		sg.wl("			}");
		sg.wl("");
		sg.wl("		}");
		sg.wl("");

	}

	@Override
	public String getSecurityManagerName() {

		return variablename;
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		sg.wl("import gallium.server.security.GalliumActionTotalSecurityManager;");

	}

	@Override
	public boolean equals(Object otherobject) {
		if (otherobject == null)
			return false;
		if (!(otherobject instanceof UnconditionalPrivilege))
			return false;
		UnconditionalPrivilege otherprivilege = (UnconditionalPrivilege) otherobject;
		if (otherprivilege.getAuthority().getName().equals(this.getAuthority().getName()))
			return true;
		return false;
	}
}
