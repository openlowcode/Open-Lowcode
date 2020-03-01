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
 * An object state privilege will provide privilege on the given action group on
 * specific object states (for the lifecycle property)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectStatePrivilege
		extends
		ObjectPrivilege {
	private Lifecycle objectlifecycle;
	private ChoiceValue[] allowedstates;

	@Override
	public String generateAttributeName() {

		return StringFormatter.formatForAttribute(this.getAuthority().getName())
				+ StringFormatter.formatForAttribute(allowedstates[0].getName()) + "objectstatesecuritymanager";
	}

	@Override
	public String generateClassName() {
		return StringFormatter.formatForJavaClass(this.getAuthority().getName())
				+ StringFormatter.formatForJavaClass(allowedstates[0].getName()) + "ObjectStateSecurityManager";
	}

	@Override
	public String getSecurityManagerClassName() {
		return "ActionObjectStateSecurityManager";
	}

	/**
	 * create an object state privilege for the given authority for the provided
	 * states
	 * 
	 * @param actiongroup   action group privilege is given on
	 * @param authority     authority privilege is given to
	 * @param allowedstates list of states of the object for which the privilege is
	 *                      given
	 */
	public ObjectStatePrivilege(ActionGroup actiongroup, TotalAuthority authority, ChoiceValue[] allowedstates) {
		super(actiongroup, authority);
		this.allowedstates = allowedstates;
		if (this.allowedstates == null)
			throw new RuntimeException("null state array for ObjectStatePrivilege is not allowed.");
		if (this.allowedstates.length == 0)
			throw new RuntimeException(" state array with no element for ObjectStatePrivilege is not allowed.");

	}

	@Override
	public void validate() {
		super.validate();
		if (!this.getObjectForPrivilege().hasLifecycle())
			throw new RuntimeException("can only create an object state privilege for object "
					+ this.getObjectForPrivilege().getName() + " as it has no lifecycle");
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
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		validate(); // as it is the first to be called, it is quite dirty;
		sg.wl("import org.openlowcode.server.security.ActionObjectStateSecurityManager;");
		sg.wl("import org.openlowcode.server.data.DataObject;");
	}

	@Override
	public void generateSecurityManagerSuperStatement(SourceGenerator sg) throws IOException {
		sg.wl("                         super(\""
				+ (this.getAuthority().getModule() != null ? this.getAuthority().getModule().getCode() + "_" : "")
				+ this.getAuthority().getName().toUpperCase() + "\",new String[]{");
		for (int i = 0; i < allowedstates.length; i++) {
			sg.wl("						" + (i > 0 ? "," : "") + "\"" + allowedstates[i].getName().toUpperCase()
					+ "\"");
		}

		sg.wl("						});");

	}

	/**
	 * @return the list of states that privilege is allowed separated by column ':'
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

	@Override
	public boolean equals(Object otherobject) {
		if (otherobject == null)
			return false;
		if (!(otherobject instanceof ObjectStatePrivilege))
			return false;
		ObjectStatePrivilege otherprivilege = (ObjectStatePrivilege) otherobject;
		if (!(otherprivilege.getAuthority().getName().equals(this.getAuthority().getName())))
			return false;
		if (!(otherprivilege.getStatesSummary().equals(this.getStatesSummary())))
			return false;
		return true;
	}

}
