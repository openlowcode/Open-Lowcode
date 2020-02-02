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
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.Personal;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A personal privilege will be granted only to people who are linked to the
 * specific object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectPersonalPrivilege
		extends
		Privilege {

	private DataObjectDefinition objectforprivilege;
	private Personal personal;
	private String attributename;

	/**
	 * creates an object personal privilege for the given action group, using the
	 * Personal property of the object
	 * 
	 * @param actiongroup action group the privilege gives access to
	 * @param personal    personal property used to check if current user is
	 *                    authorized or not
	 */
	public ObjectPersonalPrivilege(ActionGroup actiongroup, Personal personal) {
		super(actiongroup);
		this.personal = personal;
		this.attributename = StringFormatter.formatForAttribute(personal.getLinkObject().getName())
				+ actiongroup.getName().toLowerCase() + "objectpersonalsecuritymanager";
	}

	/**
	 * validates that the privilege is valid
	 */
	public void validate() {
		// -------------------- control on master object
		ActionDefinition[] actions = this.getActiongroup().getActionsInGroup();
		if (actions != null)
			for (int i = 0; i < actions.length; i++) {
				ActionDefinition action = actions[i];
				if (objectforprivilege == null) {
					if (action.getAccessCriteria() != null) {
						DataObjectDefinition actionobject = action.getAccessCriteria().getMasterObject();
						if (actionobject == null)
							throw new RuntimeException("Cannot add object privilege for an action " + action.getName()
									+ " without object safety criteria , however argument "
									+ action.getAccessCriteria().getName() + " does not have a master object");
						objectforprivilege = actionobject;
					}
				}
				if (action.getAccessCriteria() != null)
					if (objectforprivilege != null)
						if (!objectforprivilege.equals(action.getAccessCriteria().getMasterObject()))
							throw new RuntimeException("inconsistent objects for action group "
									+ objectforprivilege.getName() + " and " + action.getAccessCriteria().getName());
			}
		// --------------------------- control on personal property

		if (personal.getParent() != this.objectforprivilege)
			throw new RuntimeException("Inconsistent object "
					+ (this.objectforprivilege != null ? this.objectforprivilege.getName() : "NULL")
					+ " for action group (" + this.getActiongroup().toString() + ") and personal ("
					+ (personal != null ? personal.getParent().getName() : "NULL") + ") for ObjectPersonalPrivilege");
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		sg.wl("import org.openlowcode.server.security.ActionObjectPersonalSecurityManager;");
		sg.wl("import org.openlowcode.server.data.DataObject;");
		sg.wl("import " + personal.getLinkObject().getOwnermodule().getPath() + ".data."
				+ StringFormatter.formatForJavaClass(personal.getLinkObject().getName()) + ";");
	}

	@Override
	public String getSecurityManagerName() {
		return attributename;
	}

	@Override
	public void writeDefinition(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		validate();
		String objectclass = StringFormatter.formatForJavaClass(objectforprivilege.getName());
		String objectclassid = StringFormatter.formatForAttribute(objectforprivilege.getName()) + "id";
		String linkclass = StringFormatter.formatForJavaClass(personal.getLinkObject().getName());

		sg.wl("		private static ActionObjectPersonalSecurityManager<" + objectclass + "," + linkclass + "> "
				+ attributename);
		sg.wl("			= new ActionObjectPersonalSecurityManager<" + objectclass + "," + linkclass + ">("
				+ linkclass + ".getDefinition(),");
		sg.wl("					(appuserid) -> (" + linkclass + ".getalllinksfromrightid(appuserid,null)), ");
		sg.wl("					(appuserid," + objectclassid + ")->(" + linkclass + ".getalllinksfromleftandrightid("
				+ objectclassid + ",appuserid,null)),"
				+ (contextaction.isAccessCriteriaInput() ? "getInputSecurityDataExtractor()" : "null"));
		sg.wl("				," + objectclass + ".getDefinition().getIdFieldSchema()");
		sg.wl("				," + linkclass + ".getDefinition().getLfidFieldSchema()," + linkclass
				+ ".getDefinition().getRgidFieldSchema());");

	}

}
