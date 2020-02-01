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

import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A migrator changing a link to parent relationshop to a normal link. This is
 * useful when links started using the 1-N 'LinkedToParent' property, but it is
 * then required to move to he N-N 'LinkObject' property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataMigratorLinkToParentToLinkFromLeft
		extends
		DataMigrator {
	private LinkObject<?, ?> newlink;
	private LinkedToParent<?> oldlinktoparent;
	private Module parentmodule;

	/**
	 * creates a data migrator migrating parent-child into normal links
	 * 
	 * @param parentmodule    parent module
	 * @param newlink         new link to create data
	 * @param oldlinktoparent old linked to parent property to get data from
	 */
	public DataMigratorLinkToParentToLinkFromLeft(
			Module parentmodule,
			LinkObject<?, ?> newlink,
			LinkedToParent<?> oldlinktoparent) {
		super(StringFormatter.formatForJavaClass(oldlinktoparent.getParent().getName()) + "ParentTo"
				+ StringFormatter.formatForJavaClass(newlink.getParent().getName()) + "LinkMigrator");
		this.parentmodule = parentmodule;
		this.newlink = newlink;
		this.oldlinktoparent = oldlinktoparent;
		if (newlink.getLeftobjectforlink() != oldlinktoparent.getParent())
			throw new RuntimeException("link object from left ( " + newlink.getLeftobjectforlink().getName()
					+ " ) different from oldlinktoparent ( " + oldlinktoparent.getParent().getName() + " ) ");
		if (newlink.getRightobjectforlink() != oldlinktoparent.getParentObjectForLink())
			throw new RuntimeException("link object from right ( " + newlink.getRightobjectforlink().getName()
					+ " ) different from oldlinktoparent ( " + oldlinktoparent.getParentObjectForLink().getName()
					+ " ) ");
	}

	@Override
	public void generateMigratorToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(oldlinktoparent.getParent().getName());
		String objectargument = StringFormatter.formatForAttribute(oldlinktoparent.getParent().getName());
		String oldparentclass = StringFormatter.formatForJavaClass(oldlinktoparent.getParentObjectForLink().getName());
		String newlinkclass = StringFormatter.formatForJavaClass(newlink.getParent().getName());
		String newlinkattribute = StringFormatter.formatForAttribute(newlink.getParent().getName());
		String oldparentattribute = StringFormatter.formatForAttribute(oldlinktoparent.getInstancename());

		sg.wl("package " + parentmodule.getPath() + ".data.migrator;");
		sg.wl("");
		sg.wl("import java.util.logging.Logger;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.migrator.SDataMigrator;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + oldlinktoparent.getParentObjectForLink().getOwnermodule().getPath() + ".data."
				+ oldparentclass + ";");
		sg.wl("import " + oldlinktoparent.getParent().getOwnermodule().getPath() + ".data." + objectclass + ";");
		sg.wl("import " + newlink.getParent().getOwnermodule().getPath() + ".data." + newlinkclass + ";");
		sg.wl("");
		sg.wl("public class " + objectclass + "ParentTo" + newlinkclass + "LinkMigrator extends SDataMigrator {");
		sg.wl("	private static Logger logger = Logger.getLogger(" + objectclass + "ParentTo" + newlinkclass
				+ "LinkMigrator.class.getName());");
		sg.wl("	public " + objectclass + "ParentTo" + newlinkclass + "LinkMigrator(SModule parent) {");
		sg.wl("		super(\"" + oldlinktoparent.getParent().getName() + oldlinktoparent.getInstancename().toUpperCase()
				+ "TO" + newlink.getName().toUpperCase() + "LINK\", parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String describeMigrator() {");
		sg.wl("");
		sg.wl("		return \"" + oldlinktoparent.getParent().getName() + oldlinktoparent.getInstancename().toUpperCase()
				+ "TO" + newlink.getName().toUpperCase() + "LINK\";");
		sg.wl(" }");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeNormalMigration()  {");
		sg.wl("		int datamigrated=0;");
		sg.wl("		" + objectclass + "[] all" + objectargument + "s = " + objectclass + ".getallactive(null);");
		sg.wl("		logger.warning(\"preparing to migrate \"+all" + objectargument + "s.length+\" " + objectargument
				+ " elements \");");
		sg.wl("		for (int i=0;i<all" + objectargument + "s.length;i++) {");
		sg.wl("			" + objectclass + " this" + objectargument + " = all" + objectargument + "s[i];");
		sg.wl("			DataObjectId<" + oldparentclass + "> legacyparent" + oldparentattribute + "id = this"
				+ objectargument + ".getLinkedtoparentfor" + oldparentattribute + "id();");

		sg.wl("			if (legacyparent" + oldparentattribute + "id!=null) if (legacyparent" + oldparentattribute
				+ "id.getId()!=null) if (legacyparent" + oldparentattribute + "id.getId().length()>0) {");
		sg.wl("				" + newlinkclass + " " + newlinkattribute + " = new " + newlinkclass + "();");
		sg.wl("				" + newlinkattribute + ".setleftobject(this" + objectargument + ".getId());");
		sg.wl("				" + newlinkattribute + ".setrightobject(legacyparent" + oldparentattribute + "id);");
		sg.wl("				" + newlinkattribute + ".insert();");
		sg.wl("				datamigrated++;");
		sg.wl("				logger.fine(\"created a link (\"+datamigrated+\") " + newlink.getName().toUpperCase()
				+ " with id \"+" + newlinkattribute + ".getId()+\" for left object MEETING id = \"+this"
				+ objectargument + ".getId()+\" right object "
				+ oldlinktoparent.getParentObjectForLink().getName().toUpperCase() + " id \"+legacyparent"
				+ oldparentattribute + "id);");
		sg.wl("			}");
		sg.wl("		}");
		sg.wl("		logger.warning(\"Migrated \"+datamigrated+\" " + objectargument + " elements \");");
		sg.wl("		return datamigrated;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public long executeRecoveryMigration()  {");
		sg.wl("		int datamigrated=0;");
		sg.wl("		int datapreviouslymigrated=0;");
		sg.wl("		" + objectclass + "[] all" + objectargument + "s = " + objectclass + ".getallactive(null);");
		sg.wl("		logger.warning(\"preparing to recover migration of \"+all" + objectargument + "s.length+\" "
				+ objectargument + " elements \");");
		sg.wl("		for (int i=0;i<all" + objectargument + "s.length;i++) {");
		sg.wl("			" + objectclass + " this" + objectargument + " = all" + objectargument + "s[i];");
		sg.wl("			DataObjectId<" + oldparentclass + "> legacyparent" + oldparentattribute + "id = this"
				+ objectargument + ".getLinkedtoparentfor" + oldparentattribute + "id();");
		sg.wl("			if (legacyparent" + oldparentattribute + "id!=null) if (legacyparent" + oldparentattribute
				+ "id.getId()!=null) if (legacyparent" + oldparentattribute + "id.getId().length()>0) {");
		sg.wl("				" + newlinkclass + "[] alreadymigratedlinks = " + newlinkclass
				+ ".getalllinksfromleftandrightid(this" + objectargument + ".getId(),legacyparent" + oldparentattribute
				+ "id,null);");
		sg.wl("				if (alreadymigratedlinks.length>0) {");
		sg.wl("					datapreviouslymigrated++;");
		sg.wl("					logger.fine(\"data previously migrated (\"+datapreviouslymigrated+\") "
				+ newlink.getName().toUpperCase()
				+ " with id \"+alreadymigratedlinks[0].getId()+\" for left object MEETING id = \"+this" + objectargument
				+ ".getId()+\" right object DEAL id \"+legacyparent" + oldparentattribute + "id);");
		sg.wl("				} else {");
		sg.wl("				" + newlinkclass + " " + newlinkattribute + " = new " + newlinkclass + "();");
		sg.wl("				" + newlinkattribute + ".setleftobject(this" + objectargument + ".getId());");
		sg.wl("				" + newlinkattribute + ".setrightobject(legacyparent" + oldparentattribute + "id);");
		sg.wl("				" + newlinkattribute + ".insert();");
		sg.wl("				datamigrated++;");
		sg.wl("				logger.fine(\"created a link (\"+datamigrated+\") " + newlink.getName().toUpperCase()
				+ " with id \"+" + newlinkattribute + ".getId()+\" for left object MEETING id = \"+this"
				+ objectargument + ".getId()+\" right object DEAL id \"+legacyparent" + oldparentattribute + "id);");
		sg.wl("				}");
		sg.wl("			}");
		sg.wl("		}");
		sg.wl("		logger.warning(\"Recovery of migration : migrated \"+datamigrated+\" "
				+ newlink.getName().toUpperCase() + ", previously existing \"+datapreviouslymigrated);");
		sg.wl("		return datamigrated;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

	@Override
	public String getClassName() {
		return StringFormatter.formatForJavaClass(oldlinktoparent.getParent().getName()) + "ParentTo"
				+ StringFormatter.formatForJavaClass(newlink.getParent().getName()) + "LinkMigrator";
	}

}
