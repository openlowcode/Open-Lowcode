/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.design.data.properties.basic.AutolinkObject;
import org.openlowcode.design.data.properties.basic.DataControl;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkObjectToMaster;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * An utility class gathering the generation of code for all delete and update
 * actions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionDeleteAndUpdate {

	/**
	 * generates the delete action for the data object
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteActionToFile(DataObjectDefinition dataobject, SourceGenerator sg, Module module)
			throws IOException {
		String actionname = "Delete" + dataobject.getName().toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + objectvariable + "Page;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)  {");
		sg.wl("		return AtgLaunchsearch" + objectvariable + "Action.get().executeAndShowPage();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate the deletion action for an auto-link object showing the object being
	 * linked
	 * 
	 * @param name     name of the data object
	 * @param autolink autolink property
	 * @param sg       source generator
	 * @param module   parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteAutolinkAndShowObjectActionToFile(
			String name,
			AutolinkObject<?> autolink,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		String autolinkobjectclass = StringFormatter.formatForJavaClass(autolink.getObjectforlink().getName());
		String autolinkobjectattribute = StringFormatter.formatForAttribute(autolink.getObjectforlink().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + autolink.getObjectforlink().getOwnermodule().getPath() + ".data." + autolinkobjectclass
				+ ";");
		sg.wl("import " + autolink.getObjectforlink().getOwnermodule().getPath() + ".action.generated.AtgShow"
				+ autolinkobjectattribute + "Action;");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgDeleteautolink" + objectvariable + "andshowobjectAction extends AbsDeleteautolink"
				+ objectvariable + "andshowobjectAction {");
		sg.wl("");
		sg.wl("	public AtgDeleteautolink" + objectvariable + "andshowobjectAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable + "id,");
		sg.wl("			DataObjectId<" + autolinkobjectclass + "> " + autolinkobjectattribute
				+ "idtoshow,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + autolinkobjectattribute + "idtoshow);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("		return AtgShow" + autolinkobjectattribute + "Action.get().executeAndShowPage(outputdata.get"
				+ StringFormatter.formatForJavaClass(autolinkobjectattribute) + "idtoshowthru());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the delete attachment action for the data object
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteAttachment(String name, SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.module.system.data.Objattachment;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgDeleteattachmentfor" + objectvariable + "Action extends AbsDeleteattachmentfor"
				+ objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgDeleteattachmentfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> objectid,");
		sg.wl("			DataObjectId<Objattachment> objattachmentid,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(objectid);");
		sg.wl("		" + objectvariable + ".deleteattachment(objattachmentid);");
		sg.wl("		return new ActionOutputData(objectid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(logicoutput.getObjectid_thru());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the delete and show parent action for the data object
	 * 
	 * @param dataobject             parent data object
	 * @param relevantlinkedtoparent linked to parent
	 * @param sg                     source generator
	 * @param module                 parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteAndShowParentActionToFile(
			DataObjectDefinition dataobject,
			LinkedToParent<?> relevantlinkedtoparent,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		DataObjectDefinition parent = relevantlinkedtoparent.getParentObjectForLink();
		String parentobjectclass = StringFormatter.formatForJavaClass(parent.getName());
		String parentobjectvariable = StringFormatter.formatForAttribute(parent.getName());
		String linkedtoparentnamevar = StringFormatter.formatForAttribute(relevantlinkedtoparent.getInstancename());
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + parent.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
		sg.wl("import " + parent.getOwnermodule().getPath() + ".action.generated.AtgShow" + parentobjectvariable
				+ "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgDelete" + objectvariable + "andshowparentAction");
		sg.wl("extends AbsDelete" + objectvariable + "andshowparentAction {");
		sg.wl("");
		sg.wl("	public AtgDelete" + objectvariable + "andshowparentAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		DataObjectId<" + parentobjectclass + "> " + parentobjectvariable + "id = " + objectvariable
				+ ".getLinkedtoparentfor" + linkedtoparentnamevar + "id();");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + parentobjectvariable + "id);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("		");
		sg.wl("		return AtgShow" + parentobjectvariable + "Action.get().executeAndShowPage(outputdata.getParent"
				+ parentobjectvariable + "id());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the delete link and show left action for the data object
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteLinkAndShowLeftToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String actionname = "Delete" + dataobject.getName().toLowerCase() + "andshowleft";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) dataobject.getPropertyByName("LINKOBJECT");
		String leftobjectvariable = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectmodulepath = linkobject.getLeftobjectforlink().getOwnermodule().getPath();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + leftobjectmodulepath + ".data." + leftobjectclass + ";");
		sg.wl("import " + leftobjectmodulepath + ".action.generated.AtgShow" + leftobjectvariable + "Action;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + "Action extends Abs" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "id,");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + leftobjectvariable + "id);");
		sg.wl("			");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("	return AtgShow" + leftobjectvariable + "Action.get().executeAndShowPage(outputdata.getParent"
				+ leftobjectvariable + "id());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the delete link and show right action for the data object
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteLinkAndShowRightToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {

		String actionname = "Delete" + dataobject.getName().toLowerCase() + "andshowright";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) dataobject.getPropertyByName("LINKOBJECT");
		String rightobjectvariable = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectmodulepath = linkobject.getRightobjectforlink().getOwnermodule().getPath();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + rightobjectmodulepath + ".data." + rightobjectclass + ";");
		sg.wl("import " + rightobjectmodulepath + ".action.generated.AtgShow" + rightobjectvariable + "Action;");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + "Action extends Abs" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		DataObjectId<" + rightobjectclass + "> " + rightobjectvariable + "id = " + objectvariable
				+ ".getRgid();");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + rightobjectvariable + "id);");
		sg.wl("			");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("	return AtgShow" + rightobjectvariable + "Action.get().executeAndShowPage(outputdata.getParent"
				+ rightobjectvariable + "id());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}


	/**
	 * generates the delete link and show left action for the data object
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteLinkToMasterAndShowLeftToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String actionname = "Delete" + dataobject.getName().toLowerCase() + "andshowleft";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		LinkObjectToMaster<?, ?> linkobjecttomaster = (LinkObjectToMaster<?, ?>) dataobject.getPropertyByName("LINKOBJECTTOMASTER");
		String leftobjectvariable = StringFormatter.formatForAttribute(linkobjecttomaster.getLeftobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobjecttomaster.getLeftobjectforlink().getName());
		String leftobjectmodulepath = linkobjecttomaster.getLeftobjectforlink().getOwnermodule().getPath();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + leftobjectmodulepath + ".data." + leftobjectclass + ";");
		sg.wl("import " + leftobjectmodulepath + ".action.generated.AtgShow" + leftobjectvariable + "Action;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + "Action extends Abs" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "id,");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + leftobjectvariable + "id);");
		sg.wl("			");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("	return AtgShow" + leftobjectvariable + "Action.get().executeAndShowPage(outputdata.getParent"
				+ leftobjectvariable + "id());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the delete link and show right action for the data object
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDeleteLinkToMasterAndShowRightToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {

		String actionname = "Delete" + dataobject.getName().toLowerCase() + "andshowright";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		LinkObjectToMaster<?, ?> linkobjecttomaster = (LinkObjectToMaster<?, ?>) dataobject.getPropertyByName("LINKOBJECTTOMASTER");
		String rightobjectvariable = StringFormatter.formatForAttribute(linkobjecttomaster.getRightobjectforlink().getName());
		String rightobjectclass = StringFormatter.formatForJavaClass(linkobjecttomaster.getRightobjectforlink().getName());
		String rightobjectmodulepath = linkobjecttomaster.getRightobjectforlink().getOwnermodule().getPath();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + rightobjectmodulepath + ".data." + rightobjectclass + ";");
		sg.wl("import " + rightobjectmodulepath + ".action.generated.AtgShow" + rightobjectvariable + "Action;");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + "Action extends Abs" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		DataObjectId<" + rightobjectclass + "> " + rightobjectvariable + "id = " + rightobjectclass + ".getlastversion(" + objectvariable
				+ ".getRgmsid()).getId();");
		sg.wl("		" + objectvariable + ".delete();");
		sg.wl("		return new ActionOutputData(" + rightobjectvariable + "id);");
		sg.wl("			");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("	return AtgShow" + rightobjectvariable + "Action.get().executeAndShowPage(outputdata.getParent"
				+ rightobjectvariable + "id());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	
	
	/**
	 * generate the code for the prepare update action
	 * 
	 * @param dataobject Data Object to generate the file for
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generatePrepareupdateActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String actionname = "Prepareupdate" + dataobject.getName().toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		boolean isextra = false;
		HashMap<String, String> importdeclaration = new HashMap<String, String>();
		StringBuffer extraattributesdeclaration = new StringBuffer();
		boolean isdatacontrol = false;
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof DataControl)
				isdatacontrol = true;
			if (thisproperty.isDataInputUsedForUpdate())
				for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
					isextra = true;

					extraattributesdeclaration
							.append(", " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {
						importdeclaration.put(imports.get(k), imports.get(k));
					}
				}

		}
		isextra = true; // as the action has an address

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		sg.wl("import " + module.getPath() + ".page.generated.AtgUpdate" + objectvariable + "Page;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends");
		sg.wl("		Abs" + actionname + " {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		String returntype = objectclass;
		if (!isextra)
			if (isdatacontrol)
				isextra = true;
		if (isextra)
			returntype = "ActionOutputData";

		sg.wl("	@Override");
		sg.wl("	public " + returntype + " executeActionLogic(DataObjectId<" + objectclass
				+ "> id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		for (int i=0;i<dataobject.fieldlist.getSize();i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield  = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					sg.wl("		String[] suggestionsforfield"+stringfield.getName().toLowerCase()+" = "+objectclass+".getValuesForField"+StringFormatter.formatForJavaClass(stringfield.getName().toLowerCase())+"(null);");
				}
			}
		}
		if (isdatacontrol)
			sg.wl("		String controlstatus = " + objectvariable + ".getvalidationdetail();");
		if (!isextra) {
			sg.wl("		return " + objectvariable + ";");
		} else {
			sg.wl("		return new ActionOutputData(" + objectvariable);
			for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
				Property<?> thisproperty = dataobject.propertylist.get(i);
				if (thisproperty.isDataInputUsedForUpdate()) {
					String[] methodsforupdate = thisproperty.getPropertyExtractMethod();
					for (int j = 0; j < methodsforupdate.length; j++) {
						sg.wl("		," + objectvariable + methodsforupdate[j]);
					}
				}
			}
			if (isdatacontrol)
				sg.wl("		,controlstatus");
			// ------------------------ Attributes for field suggestions ---------------------
			for (int i=0;i<dataobject.fieldlist.getSize();i++) {
				if (dataobject.fieldlist.get(i) instanceof StringField) {
					StringField stringfield  = (StringField) dataobject.fieldlist.get(i);
					if (stringfield.hasListOfValuesHelper()) {

						sg.wl("		, suggestionsforfield" + stringfield.getName().toLowerCase()+ " ");
					}
				}
			}
			sg.wl(");");
		}
		
		
		
		
		
		
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		if (!isextra) {
			sg.wl("	public SPage choosePage(" + objectclass + " " + objectvariable + ")  {");
			sg.wl("		return new AtgUpdate" + objectvariable + "Page(" + objectvariable + ");");
			sg.wl("	}");
		} else {
			sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
			sg.wl("		return new AtgUpdate" + objectvariable + "Page(logicoutput.get" + objectclass + "()");
			for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
				Property<?> thisproperty = dataobject.propertylist.get(i);
				if (thisproperty.isDataInputUsedForUpdate()) {

					for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
						sg.wl("		,logicoutput.get"
								+ StringFormatter.formatForJavaClass(thisproperty.getDataInputForCreation(j).getName())
								+ "()");
					}

				}
			}
			if (isdatacontrol)
				sg.wl("		,logicoutput.getControlstatus()");
			// ------------------------ Attributes for field suggestions ---------------------
			for (int i=0;i<dataobject.fieldlist.getSize();i++) {
				if (dataobject.fieldlist.get(i) instanceof StringField) {
					StringField stringfield  = (StringField) dataobject.fieldlist.get(i);
					if (stringfield.hasListOfValuesHelper()) {

						sg.wl("		, logicoutput.getSuggestionsforfield" + stringfield.getName().toLowerCase()+ "() ");
					}
				}
			}
			sg.wl(");");
			sg.wl("	}");
		}
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the massive delete and show parent action
	 * 
	 * @param sg                     source generator
	 * @param module                 parent module
	 * @param name                   name of the data object
	 * @param relevantlinkedtoparent relevant linked to parent for the generation
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateMassiveDeleteAndShowParentActionToFile(
			SourceGenerator sg,
			Module module,
			String name,
			LinkedToParent<?> relevantlinkedtoparent) throws IOException {

		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		DataObjectDefinition parentforlink = relevantlinkedtoparent.getParentObjectForLink();

		String parentclass = StringFormatter.formatForJavaClass(parentforlink.getName());
		String parentattribute = StringFormatter.formatForAttribute(parentforlink.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + parentforlink.getOwnermodule().getPath() + ".action.generated.AtgShow" + parentattribute
				+ "Action;");
		sg.wl("import " + parentforlink.getOwnermodule().getPath() + ".data." + parentclass + ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgMassivedelete" + objectvariable + "andshowparent"
				+ relevantlinkedtoparent.getInstancename().toLowerCase() + "Action");
		sg.wl("		extends AbsMassivedelete" + objectvariable + "andshowparent"
				+ relevantlinkedtoparent.getInstancename().toLowerCase() + "Action {");
		sg.wl("");
		sg.wl("	public AtgMassivedelete" + objectvariable + "andshowparent"
				+ relevantlinkedtoparent.getInstancename().toLowerCase() + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + parentclass + "> executeActionLogic(DataObjectId<" + objectclass + ">[] "
				+ objectvariable + "id,");
		sg.wl("			DataObjectId<" + parentclass + "> parent" + parentattribute
				+ "id, Function<TableAlias, QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + "[] allsessions = " + objectclass + ".readseveral(" + objectvariable + "id);");
		sg.wl("		" + objectclass + ".delete(allsessions);");
		sg.wl("		return parent" + parentattribute + "id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + parentclass + "> parent" + parentattribute + "id_thru)  {");
		sg.wl("");
		sg.wl("		return AtgShow" + parentattribute + "Action.get().executeAndShowPage(parent" + parentattribute
				+ "id_thru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate a mass update of link objects and show the left object after the
	 * action
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @param linkobject link object
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateMassUpdateAndShowLeftToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module,
			LinkObject<?, ?> linkobject) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectvariable = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + linkobject.getLeftobjectforlink().getOwnermodule().getPath() + ".data." + leftobjectclass
				+ ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgMassupdate" + objectvariable + "andshowleftAction extends AbsMassupdate" + objectvariable
				+ "andshowleftAction {");
		sg.wl("");
		sg.wl("	public AtgMassupdate" + objectvariable + "andshowleftAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String updatenoteargument = "";
		if (dataobject.IsIterated())
			updatenoteargument = ",String updatenote";
		sg.wl("	public DataObjectId<" + leftobjectclass + "> executeActionLogic(" + objectclass + "[] " + objectvariable
				+ updatenoteargument + ", DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		for (int i=0;i<" + objectvariable + ".length;i++) {");
		sg.wl("			" + objectclass + " this" + objectvariable + " = " + objectvariable + "[i];");
		if (dataobject.IsIterated())
			sg.wl("			this" + objectvariable + ".setupdatenote(updatenote);");
		sg.wl("			this" + objectvariable + ".update();");
		sg.wl("		}");
		sg.wl("		return " + leftobjectvariable + "id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "idthru)  {");
		sg.wl("		return AtgShow" + leftobjectvariable + "Action.get().executeAndShowPage(" + leftobjectvariable
				+ "idthru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();
	}

	/**
	 * generate a mass update of link tomasterobjects and show the left object after the
	 * action
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @param linkobjecttomaster link object
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateMassUpdateLinkToMasterAndShowLeftToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module,
			LinkObjectToMaster<?, ?> linkobjecttomaster) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String leftobjectclass = StringFormatter.formatForJavaClass(linkobjecttomaster.getLeftobjectforlink().getName());
		String leftobjectvariable = StringFormatter.formatForAttribute(linkobjecttomaster.getLeftobjectforlink().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + linkobjecttomaster.getLeftobjectforlink().getOwnermodule().getPath() + ".data." + leftobjectclass
				+ ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgMassupdate" + objectvariable + "andshowleftAction extends AbsMassupdate" + objectvariable
				+ "andshowleftAction {");
		sg.wl("");
		sg.wl("	public AtgMassupdate" + objectvariable + "andshowleftAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String updatenoteargument = "";
		if (dataobject.IsIterated())
			updatenoteargument = ",String updatenote";
		sg.wl("	public DataObjectId<" + leftobjectclass + "> executeActionLogic(" + objectclass + "[] " + objectvariable
				+ updatenoteargument + ", DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		for (int i=0;i<" + objectvariable + ".length;i++) {");
		sg.wl("			" + objectclass + " this" + objectvariable + " = " + objectvariable + "[i];");
		if (dataobject.IsIterated())
			sg.wl("			this" + objectvariable + ".setupdatenote(updatenote);");
		sg.wl("			this" + objectvariable + ".update();");
		sg.wl("		}");
		sg.wl("		return " + leftobjectvariable + "id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + leftobjectclass + "> " + leftobjectvariable + "idthru)  {");
		sg.wl("		return AtgShow" + leftobjectvariable + "Action.get().executeAndShowPage(" + leftobjectvariable
				+ "idthru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();
	}
	
	/**
	 * generate a mass update of children objects and show the parent object after
	 * the action
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @param linkobject link object
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateMassUpdateAndShowParentToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module,
			LinkedToParent<?> linkedtoparent) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String parentobjectclass = StringFormatter
				.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());
		String parentobjectvariable = StringFormatter
				.formatForAttribute(linkedtoparent.getParentObjectForLink().getName());
		String linkedtoparentinstance = linkedtoparent.getInstancename().toLowerCase();

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + linkedtoparent.getParentObjectForLink().getOwnermodule().getPath() + ".data."
				+ parentobjectclass + ";");
		sg.wl("import " + linkedtoparent.getParentObjectForLink().getOwnermodule().getPath()
				+ ".action.generated.AtgShow" + parentobjectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgMassupdate" + objectvariable + "andshowparent" + linkedtoparentinstance
				+ "Action extends AbsMassupdate" + objectvariable + "andshowparent" + linkedtoparentinstance
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgMassupdate" + objectvariable + "andshowparent" + linkedtoparentinstance
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String updatenoteargument = "";
		if (dataobject.IsIterated())
			updatenoteargument = ",String updatenote";
		sg.wl("	public DataObjectId<" + parentobjectclass + "> executeActionLogic(" + objectclass + "[] "
				+ objectvariable + updatenoteargument + ",DataObjectId<" + parentobjectclass + "> "
				+ parentobjectvariable + "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		for (int i=0;i<" + objectvariable + " .length;i++) {");
		sg.wl("			" + objectclass + " this" + objectvariable + " = " + objectvariable + "[i];");
		if (dataobject.IsIterated())
			sg.wl("			this" + objectvariable + ".setupdatenote(updatenote);");
		sg.wl("			this" + objectvariable + ".update();");
		sg.wl("		}");
		sg.wl("		return " + parentobjectvariable + "id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + parentobjectclass + "> " + parentobjectvariable
				+ "idthru)  {");
		sg.wl("		return AtgShow" + parentobjectvariable + "Action.get().executeAndShowPage(" + parentobjectvariable
				+ "idthru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate a mass update action
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */

	public static void generateMassUpdateActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import " + module.getPath() + ".action.generated.AbsMassupdate" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("");
		sg.wl("public class AtgMassupdate" + objectvariable + "Action extends AbsMassupdate" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgMassupdate" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String updatenoteargument = "";
		if (dataobject.IsIterated())
			updatenoteargument = ",String updatenote";
		sg.wl("	public " + objectclass + "[] executeActionLogic(" + objectclass + "[] updateinput" + updatenoteargument
				+ ",Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		ArrayList<" + objectclass + "> output = new ArrayList<" + objectclass + ">();");
		sg.wl("		for (int i=0;i<updateinput.length;i++) {");
		sg.wl("			" + objectclass + " this" + objectvariable + " = updateinput[i];");
		if (dataobject.IsIterated())
			sg.wl("			this" + objectvariable + ".setupdatenote(updatenote);");
		sg.wl("			this" + objectvariable + ".update();");
		sg.wl("			output.add(" + objectclass + ".readone(this" + objectvariable + ".getId()));");
		sg.wl("		}");
		sg.wl("		return output.toArray(new " + objectclass + "[0]);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + objectclass + "[] inputafterupdate)  {");
		sg.wl("		// TODO Auto-generated method stub");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate the unitary update action to file
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateUpdateActionToFile(DataObjectDefinition dataobject, SourceGenerator sg, Module module)
			throws IOException {
		String actionname = "Update" + dataobject.getName().toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		LinkedToParent<?> subobject = dataobject.isSubObject();
		HashMap<String, String> importdeclaration = new HashMap<String, String>();
		StringBuffer extraattributesdeclaration = new StringBuffer();
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty.isDataInputUsedForUpdate())
				for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);

					extraattributesdeclaration
							.append(", " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {
						importdeclaration.put(imports.get(k), imports.get(k));
					}
				}

		}

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		if (subobject != null) {
			sg.wl("import " + subobject.getParentObjectForLink().getOwnermodule().getPath() + ".data."
					+ StringFormatter.formatForJavaClass(subobject.getParentObjectForLink().getName()) + ";");
		}
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		sg.wl("import org.openlowcode.server.action.SecurityInDataMethod;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(" + objectclass + " " + objectvariable
				+ extraattributesdeclaration.toString() + ",Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty.isDataInputUsedForUpdate()) {
				String[] methodsforcreation = thisproperty.getPropertyInitMethod();
				if (methodsforcreation != null)
					for (int j = 0; j < methodsforcreation.length; j++) {
						sg.wl("		" + objectvariable + methodsforcreation[j]);
					}
			}
		}
		sg.wl("		" + objectvariable + ".update(this,SecurityInDataMethod.FAIL_IF_NOT_AUTHORIZED);");
		sg.wl("		return " + objectvariable + ".getId();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> id)  {");

		if (subobject != null) {
			String parentclass = StringFormatter.formatForJavaClass(subobject.getParentObjectForLink().getName());
			String parentattribute = StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName());

			sg.wl("		DataObjectId<" + parentclass + "> " + parentattribute + "id = " + objectclass
					+ ".readone(id).get" + StringFormatter.formatForJavaClass(subobject.getName()) + "id();");
			sg.wl("		AtgShow" + parentattribute + "Action show" + parentattribute + "action = new AtgShow"
					+ parentattribute + "Action(this.getParent());");
			sg.wl("		return show" + parentattribute + "action.executeAndShowPage(" + parentattribute + "id);	");

		} else {
			sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow"
					+ objectvariable + "Action(this.getParent());");
			sg.wl("		return show" + objectvariable + "action.executeAndShowPage(id);");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

}
