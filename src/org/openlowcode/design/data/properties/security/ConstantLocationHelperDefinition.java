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

import org.openlowcode.design.access.ModuleDomain;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * a location helper that always put data objects in the same location
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstantLocationHelperDefinition
		extends
		LocationHelperDefinition {

	private ModuleDomain moduledomain;

	/**
	 * creates a constant location helper
	 * 
	 * @param parent       parent data object
	 * @param moduledomain the module domain to always map the data objects to
	 */
	public ConstantLocationHelperDefinition(DataObjectDefinition parent, ModuleDomain moduledomain) {
		super(parent);
		this.moduledomain = moduledomain;
	}

	@Override
	public void generateLocationHelper(SourceGenerator sg, Module module) throws IOException {
		String objectclassname = StringFormatter.formatForJavaClass(this.getParent().getName());
		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("import gallium.server.data.properties.security.SameLocationHelper;");
		sg.wl("");
		sg.wl("");
		sg.wl("public class " + objectclassname + "LocationHelper extends SameLocationHelper<" + objectclassname
				+ "> {");
		sg.wl("	private static " + objectclassname + "LocationHelper singleton;");
		sg.wl("	public static " + objectclassname + "LocationHelper get() {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclassname + "LocationHelper temp = new " + objectclassname + "LocationHelper();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	public " + objectclassname + "LocationHelper() {");
		sg.wl("		super(\"" + module.getCode() + "_" + moduledomain.getName() + "\");");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

}
