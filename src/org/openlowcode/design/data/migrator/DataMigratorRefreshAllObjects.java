/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.migrator;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * This migrator will refresh all computed fields on the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a> *
 *
 */
public class DataMigratorRefreshAllObjects
		extends
		DataMigrator {
	private DataObjectDefinition object;

	/**
	 * creaets a migrator that refreshes all calculated fields on an object
	 * 
	 * @param object data object the migrator will run on
	 */
	public DataMigratorRefreshAllObjects(DataObjectDefinition object) {
		super(object.getOwnermodule().getName().toUpperCase() + "." + object.getName().toUpperCase()
				+ ".COMPUTEDFIELDSFULLREFRESH");
		this.object = object;
	}

	@Override
	public String getClassName() {
		return StringFormatter.formatForJavaClass(object.getName()) + "ComputedFieldsFullRefresh";
	}

	@Override
	public void generateMigratorToFile(SourceGenerator sg, Module module) throws IOException {
		String objectvariable = StringFormatter.formatForAttribute(object.getName());
		String objectclass = StringFormatter.formatForJavaClass(object.getName());

		sg.wl("package " + object.getOwnermodule().getPath() + ".data.migrator;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.migrator.SDataMigrator;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("");
		sg.wl("public class " + objectclass + "ComputedFieldsFullRefresh extends SDataMigrator {");
		sg.wl("	private static String NAME = \"" + object.getOwnermodule().getName().toUpperCase() + "."
				+ object.getName().toUpperCase() + ".COMPUTEDFIELDSFULLREFRESH\";");
		sg.wl("	public " + objectclass + "ComputedFieldsFullRefresh(SModule parent) {");
		sg.wl("		super(NAME, parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String describeMigrator() {");
		sg.wl("		");
		sg.wl("		return NAME;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeNormalMigration()  {");
		sg.wl("		int counter = 0;");
		sg.wl("		OLcServer.getServer().resetTriggersList();");
		sg.wl("		" + objectclass + "[] " + objectvariable + "array = " + objectclass + ".getallactive(null);");
		sg.wl("		for (int i=0;i<" + objectvariable + "array.length;i++) {");
		sg.wl("			" + objectclass + " " + objectvariable + " = " + objectvariable + "array[i];");
		if (object.IsIterated()) {
			sg.wl("			" + objectvariable + ".setupdatenote(\"Massive Computed Fields refresh from migrator\");");
		}
		sg.wl("			" + objectvariable + ".refresh();");
		sg.wl("			counter++;");
		sg.wl("			");
		sg.wl("		}");
		sg.wl("		OLcServer.getServer().resetTriggersList();");
		sg.wl("		return counter;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeRecoveryMigration()  {");
		sg.wl("		return executeNormalMigration();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

}
