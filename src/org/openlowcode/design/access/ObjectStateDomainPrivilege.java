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
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * An object state domain privilege provides a privilege to the given action
 * group to the authority depending on the domain and state
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectStateDomainPrivilege
		extends
		ObjectPrivilege {
	private Lifecycle objectlifecycle;

	private ChoiceValue[] allowedstates;
	private String classname;
	private String attributename;

	/**
	 * create an object state domain privilege
	 * 
	 * @param actiongroup   action group the privilege gives access to
	 * @param authority     authority for the domain
	 * @param allowedstates states of the object for which the privilege is given
	 */
	public ObjectStateDomainPrivilege(
			ActionGroup actiongroup,
			ModuleDomainAuthority authority,
			ChoiceValue[] allowedstates) {
		super(actiongroup, authority);

		this.allowedstates = allowedstates;
		this.classname = StringFormatter.formatForJavaClass(authority.getName())
				+ StringFormatter.formatForJavaClass(allowedstates[0].getName()) + "ObjectStateDomainSecurityManager";
		this.attributename = StringFormatter.formatForAttribute(authority.getName())
				+ StringFormatter.formatForAttribute(allowedstates[0].getName()) + "objectstatedomainsecuritymanager";

	}

	@Override
	public void validate() {
		super.validate();
		if (!this.getObjectForPrivilege().hasLifecycle())
			throw new RuntimeException("can only create an object state domain privilege for object "
					+ this.getObjectForPrivilege() + " as it has no lifecycle");
		objectlifecycle = (Lifecycle) (this.getObjectForPrivilege().getPropertyByName("LIFECYCLE"));
		if (allowedstates == null)
			throw new RuntimeException("cannot create an objectstateprivilege with a null array of allowed states");
		if (allowedstates.length == 0)
			throw new RuntimeException("cannot create an objectstateprivilege with an array of zero element");
		this.allowedstates = allowedstates.clone();
		for (int i = 0; i < this.allowedstates.length; i++) {
			ChoiceValue thischoicevalue = this.allowedstates[i];
			if (!objectlifecycle.hasChoiceValue(thischoicevalue))
				throw new RuntimeException("choice value " + thischoicevalue.getName()
						+ " does not belong to lifecycle of object  " + this.getObjectForPrivilege().getName());
		}

		if (!this.getObjectForPrivilege().isLocated())
			throw new RuntimeException("can only create an object state domain privilege for object "
					+ this.getObjectForPrivilege() + " as it is not located");
	}

	@Override
	public String getSecurityManagerName() {
		return attributename;
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		validate(); // as it is the first to be called
		sg.wl("import gallium.server.security.GalliumActionObjectStateDomainSecurityManager;");
		sg.wl("import gallium.server.data.DataObject;");
	}

	@Override
	public String generateAttributeName() {
		return this.attributename;
	}

	@Override
	public String generateClassName() {
		return this.classname;
	}

	@Override
	public String getSecurityManagerClassName() {
		return "GalliumActionObjectStateDomainSecurityManager";
	}

	@Override
	public void generateSecurityManagerSuperStatement(SourceGenerator sg) throws IOException {
		sg.wl("				super(\"" + this.getAuthority().getName() + "\",new String[]{");
		for (int i = 0; i < allowedstates.length; i++) {
			sg.wl("						" + (i > 0 ? "," : "") + "\"" + allowedstates[i].getName().toUpperCase()
					+ "\"");
		}

		sg.wl("						});");

	}

	@Override
	public boolean equals(Object otherobject) {
		if (otherobject == null)
			return false;
		if (!(otherobject instanceof ObjectStateDomainPrivilege))
			return false;
		ObjectStateDomainPrivilege otherprivilege = (ObjectStateDomainPrivilege) otherobject;
		if (!(otherprivilege.getAuthority().getName().equals(this.getAuthority().getName())))
			return false;
		if (!(otherprivilege.getStatesSummary().equals(this.getStatesSummary())))
			return false;
		return true;
	}

	/**
	 * @return a summary for logging purposes of the states this privilege applies
	 *         to
	 */
	public String getStatesSummary() {
		StringBuffer statessummary = new StringBuffer("");
		for (int i = 0; i < this.allowedstates.length; i++) {
			if (i > 0)
				statessummary.append(":");
			statessummary.append(allowedstates[i].getName());
		}
		return statessummary.toString();
	}

}
