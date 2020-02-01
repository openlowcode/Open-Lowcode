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
import org.openlowcode.design.data.stringpattern.PatternElement;
import org.openlowcode.design.data.stringpattern.StringPattern;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * this data migrator generates a number according to the defined pattern. This
 * should be used if the 'Numbered' property was added after some objects were
 * created in the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataMigratorGenerateNumber
		extends
		DataMigrator {

	private DataObjectDefinition object;
	private StringPattern pattern;

	/**
	 * Creates a data migrator to generate number
	 * 
	 * @param object  data object the migrator is defined on
	 * @param pattern defined pattern (could be different from the auto-number
	 *                pattern or similar)
	 */
	public DataMigratorGenerateNumber(DataObjectDefinition object, StringPattern pattern) {
		super(object.getOwnermodule().getCode() + "." + object.getName() + "GENNUMBER");
		this.object = object;
		this.pattern = pattern;
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
		sg.wl("import " + object.getOwnermodule().getPath() + ".data." + objectclass + ";");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			PatternElement thiselement = pattern.getElement(i);
			for (int j = 0; j < thiselement.generateImport().length; j++) {
				sg.wl(thiselement.generateImport()[j]);
			}
		}
		sg.wl("");
		sg.wl("");
		sg.wl("public class " + objectclass + "GenerateNumberMigrator extends SDataMigrator {");
		sg.wl("	private static String NAME = \"" + object.getOwnermodule().getName().toUpperCase() + "."
				+ object.getName().toUpperCase() + ".GENERATENUMBER\";");
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
		sg.wl("		" + objectclass + "[] array = " + objectclass + ".getallactive(null);");
		sg.wl("		long counter=0;");
		sg.wl("		for (int i=0;i<array.length;i++) {");
		sg.wl("");
		sg.wl("			" + objectclass + " object = array[i];");
		sg.wl("			boolean numberpresent=false;");
		sg.wl("			if (object.getNr()!=null) if (object.getNr().length()>0) numberpresent=true;");
		sg.wl("			if (!numberpresent) {");
		sg.wl("			StringBuffer sequence = new StringBuffer();");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			sg.wl("				sequence.append(" + pattern.getElement(i).generateSource() + ");");
		}
		sg.wl("			object.setobjectnumber(sequence.toString());");
		sg.wl("			counter++;");
		sg.wl("			}");
		sg.wl("		}");
		sg.wl("		return counter;");
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
