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
import java.util.logging.Logger;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * a privilege given for an object on a given domain
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectDomainPrivilege
		extends
		ObjectPrivilege {
	private static Logger logger = Logger.getLogger(ObjectDomainPrivilege.class.getName());
	private String classname;
	private String attributename;

	/**
	 * creates a privilege for the given action group for the given module domain
	 * authority
	 * 
	 * @param actiongroup the action group privilege is given to
	 * @param authority   the module domain authority that benefits from the
	 *                    privilege
	 */
	public ObjectDomainPrivilege(ActionGroup actiongroup, ModuleDomainAuthority authority) {
		super(actiongroup, authority);
		this.classname = StringFormatter.formatForJavaClass(authority.getName()) + "ObjectDomainSecurityManager";
		this.attributename = StringFormatter.formatForAttribute(authority.getName()) + "objectdomainsecuritymanager";

	}

	@Override
	public void validate() {
		super.validate();
		if (this.getObjectForPrivilege() == null) {
			logger.severe("ACCESS RIGHT WARNING: No object for privilege for action group "
					+ this.getActiongroup().toString() + " for authority " + this.getAuthority().getName()
					+ " this is normal for some actions like prepare create.");
		} else {
			if (!this.getObjectForPrivilege().isLocated())
				throw new RuntimeException("cannot create an Object Domain privilege for object "
						+ this.getObjectForPrivilege().getName() + " as it is noy located");

		}
	}

	@Override
	public String getSecurityManagerName() {
		return attributename;
	}

	@Override
	public void writeImport(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		validate(); // as it is the first to be called
		sg.wl("import gallium.server.security.GalliumActionObjectDomainSecurityManager;");
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
		return "GalliumActionObjectDomainSecurityManager";
	}

	@Override
	public void generateSecurityManagerSuperStatement(SourceGenerator sg) throws IOException {
		sg.wl("				super(\"" + this.getAuthority().getName() + "\");");

	}

	@Override
	public boolean equals(Object otherobject) {
		if (otherobject == null)
			return false;
		if (!(otherobject instanceof ObjectDomainPrivilege))
			return false;
		ObjectDomainPrivilege otherprivilege = (ObjectDomainPrivilege) otherobject;
		if (otherprivilege.getAuthority().getName().equals(this.getAuthority().getName()))
			return true;
		return false;
	}
}
