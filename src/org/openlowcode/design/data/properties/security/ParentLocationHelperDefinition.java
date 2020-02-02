/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.security;

import java.io.IOException;

import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A location helper that takes the location of a parent for linkedtoparent
 * property, and applies it to the current object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ParentLocationHelperDefinition
		extends
		LocationHelperDefinition {
	private LinkedToParent<?> parentlinkforlocation;

	/**
	 * creates a parent location helper
	 * 
	 * @param parentlinkforlocation the linked to parent property on the current
	 *                              object linking to the parent object for link
	 */
	public ParentLocationHelperDefinition(LinkedToParent<?> parentlinkforlocation) {
		super(parentlinkforlocation.getParent());
		this.parentlinkforlocation = parentlinkforlocation;
	}

	@Override
	public void generateLocationHelper(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(this.getParent().getName());
		String locatedparentclass = StringFormatter
				.formatForJavaClass(parentlinkforlocation.getParentObjectForLink().getName());

		sg.wl("package " + this.getParent().getOwnermodule().getPath() + ".data;");
		sg.wl("");
		sg.wl("import org.openlowcode.module.system.data.Domain;");
		sg.wl("import org.openlowcode.server.data.DataObject;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.LocatedInterface;");
		sg.wl("import org.openlowcode.server.data.properties.UniqueidentifiedInterface;");
		sg.wl("import org.openlowcode.server.data.properties.security.LocationHelper;");
		sg.wl("");
		sg.wl("public class " + objectclass + "LocationHelper extends LocationHelper<" + objectclass + "> {");
		sg.wl("	private static " + objectclass + "LocationHelper singleton;");
		sg.wl("	public static " + objectclass + "LocationHelper get() {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclass + "LocationHelper temp = new " + objectclass + "LocationHelper();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<Domain> getObjectLocation(" + objectclass + " object)  {");
		sg.wl("		" + locatedparentclass + " parent = object.getparentfor"
				+ parentlinkforlocation.getInstancename().toLowerCase() + "();");
		sg.wl("		if (parent==null) throw new RuntimeException(\"Parent not set at time of insert or update, so location could not be set\");");
		sg.wl("		return parent.getLocationdomainid();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

	public LinkedToParent<?> getParentLinkForLocation() {
		return this.parentlinkforlocation;
	}

}
