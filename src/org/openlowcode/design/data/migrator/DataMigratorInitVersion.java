/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
import org.openlowcode.server.data.properties.StoredobjectQueryHelper;
import org.openlowcode.server.data.properties.VersionedQueryHelper;
import org.openlowcode.server.data.storage.QueryFilter;


/**
 * This data migration will initiate to the first version of the version number
 * all objects where version is null.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataMigratorInitVersion
		extends
		DataMigrator {
	private DataObjectDefinition object;

	/**
	 * Creates the init version migrator
	 * 
	 * @param object data object
	 */
	public DataMigratorInitVersion(DataObjectDefinition object) {
		super(object.getOwnermodule().getCode() + "." + object.getName() + ".INITVERSION");
		this.object = object;
	}

	@Override
	public String getClassName() {
		return StringFormatter.formatForJavaClass(object.getName()) + "GenerateNumberMigrator";
	}

	@Override
	public void generateMigratorToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		sg.wl("package " + object.getOwnermodule().getPath() + ".data.migrator;");
		sg.wl("import org.openlowcode.server.data.migrator.SDataMigrator;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.data.properties.VersionedQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import " + object.getOwnermodule().getPath() + ".data." + objectclass + ";");
		sg.wl("public class " + objectclass + "GenerateNumberMigrator extends SDataMigrator {");
		sg.wl("	private static String NAME = \"" + object.getOwnermodule().getName().toUpperCase() + "."
				+ object.getName().toUpperCase() + ".INITVERSION\";");
		sg.wl("	public " + objectclass + "GenerateNumberMigrator(SModule parent) {");
		sg.wl("		super(NAME, parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String describeMigrator() {");
		sg.wl("		return NAME;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeNormalMigration() {");
		
		
sg.wl("		"+objectclass+"[] allactivitieswithblankversion = "+objectclass+"");
sg.wl("				.getallactive(");
sg.wl("						QueryFilter.get(VersionedQueryHelper.getVersionQueryCondition(");
sg.wl("								"+objectclass+".getDefinition()");
sg.wl("										.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive),");
sg.wl("								\"\", "+objectclass+".getDefinition())));");
sg.wl("");
sg.wl("		if (allactivitieswithblankversion != null) {");
sg.wl("			"+objectclass+".initversion(allactivitieswithblankversion);");
sg.wl("			return allactivitieswithblankversion.length;");
sg.wl("		}");
sg.wl("		return 0;");
				
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeRecoveryMigration() {");
		sg.wl("		return executeNormalMigration();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

}
