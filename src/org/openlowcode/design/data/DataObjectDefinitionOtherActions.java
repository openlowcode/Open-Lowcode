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
import java.util.Iterator;

import org.openlowcode.design.data.properties.basic.AutolinkObject;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameChoiceFieldValue;
import org.openlowcode.design.data.properties.basic.HasAutolink;
import org.openlowcode.design.data.properties.basic.HasMultiDimensionalChild;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkObjectToMaster;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.PrintOut;
import org.openlowcode.design.data.properties.basic.Typed;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * This class gathers automatically generated actions for a data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionOtherActions {

	/**
	 * generate the code for the set target date action
	 * 
	 * @param name   name of the data object in java
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generateSetTargetDateActionToFile(String name, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgSettargetdatefor" + objectvariable + "Action extends AbsSettargetdatefor"
				+ objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgSettargetdatefor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass
				+ "> id, Date newtargetdate,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		sg.wl("		" + objectvariable + ".settargetdate(newtargetdate);");
		sg.wl("		return id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> outputid)  {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(outputid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * Generates the change state action for a data object
	 * 
	 * @param name      valid java name of the data object
	 * @param lifecycle lifecycle property
	 * @param sg        source generator
	 * @param module    parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generateChangeStateActionToFile(
			String name,
			Lifecycle lifecycle,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		String lifecycleclass = StringFormatter.formatForJavaClass(lifecycle.getTransitionChoiceCategory().getName());
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + lifecycle.getTransitionChoiceCategory().getParentModule().getPath() + ".data.choice."
				+ lifecycleclass + "ChoiceDefinition;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgChangestate" + objectvariable + "Action extends AbsChangestate" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgChangestate" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass + "> id,");
		sg.wl("			ChoiceValue<" + lifecycleclass
				+ "ChoiceDefinition> newstate,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		sg.wl("		" + objectvariable + ".changestate(newstate);");
		sg.wl("		return id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> outputid)  {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(outputid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();

	}

	/**
	 * generate the show history action to file
	 * 
	 * @param name        name of the data object
	 * @param isiterated  true if object is iterated
	 * @param isversioned true if object is versioned
	 * @param sg          source generator
	 * @param module      parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateShowHistoryActionToFile(
			String name,
			boolean isiterated,
			boolean isversioned,
			SourceGenerator sg,
			Module module) throws IOException {
		String actionname = "Showhistoryfor" + name.toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgShowhistoryfor" + objectvariable + "Page;");
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
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		String arguments = "";
		String pagearguments = "";
		if (isiterated) {
			sg.wl("			// treat iteration");
			sg.wl("		" + objectclass + "[] alliterations = " + objectclass + ".getallobjectiterationsbyobjectid("
					+ objectvariable + "id);");
			arguments += "alliterations,";
			pagearguments += "logicoutput.get" + objectclass + "iterations(),";

		}
		if (isversioned) {
			sg.wl("			// treat versions");

			sg.wl("		" + objectclass + " object = " + objectclass + ".readone(" + objectvariable + "id);");
			sg.wl("		" + objectclass + "[] allversions = " + objectclass
					+ ".getallversions(object.getMasterid(),null);");
			arguments += "allversions,";
			pagearguments += "logicoutput.get" + objectclass + "versions(),";

		}
		sg.wl("		return new ActionOutputData(" + arguments + objectvariable + "id);");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new AtgShowhistoryfor" + objectvariable + "Page(" + pagearguments + "logicoutput.get"
				+ objectclass + "idthru());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate print-out preview action to file
	 * 
	 * @param name             name of the data object
	 * @param sg               source generator
	 * @param module           parent module
	 * @param printoutproperty print-out property of the data object
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generatePrintOutPreviewToFile(
			String name,
			SourceGenerator sg,
			Module module,
			PrintOut printoutproperty) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgPreviewprintoutfor" + objectvariable + "for"
				+ printoutproperty.getInstancename().toLowerCase() + "Action extends AbsPreviewprintoutfor"
				+ objectvariable + "for" + printoutproperty.getInstancename().toLowerCase() + "Action {");
		sg.wl("");
		sg.wl("	public AtgPreviewprintoutfor" + objectvariable + "for"
				+ printoutproperty.getInstancename().toLowerCase() + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		return new ActionOutputData(" + objectvariable + ".generatepreviewfor"
				+ printoutproperty.getInstancename().toLowerCase() + "());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		throw new RuntimeException(\"Not implemented, this action has to be used as inline\");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();

	}

	/**
	 * generates the show action for iteration to a file (automatically generated
	 * code)
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateShowActionForIterationToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		boolean otherattributes = false;

		ArrayList<String> datacreation = new ArrayList<String>();
		ArrayList<String> variablename = new ArrayList<String>();
		ArrayList<String> importstatement = new ArrayList<String>();
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				importstatement.add("import org.openlowcode.tools.messages.SFile;");
				importstatement.add("import org.openlowcode.module.system.data.Binaryfile;");
				importstatement.add("import org.openlowcode.server.data.properties.DataObjectId;");
				otherattributes = true;
				String imagecontentname = imagecontent.getInstancename().toLowerCase();
				datacreation.add("		SFile " + imagecontentname + "tbn = oldobjectiteration.getthumbnailfor"
						+ imagecontentname + "();");
				datacreation.add("		DataObjectId<Binaryfile> " + imagecontentname
						+ "fullimgid = oldobjectiteration.getImagecontentfor" + imagecontentname + "imgid();");
				variablename.add(imagecontentname + "tbn");
				variablename.add(imagecontentname + "fullimgid");
			}

		}
		if (dataobject.hasLifecycle()) {

			String unreleasedwarning = ((Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"))).getUnreleasedWarning();
			if (unreleasedwarning != null) {

				TransitionChoiceCategory lifecycle = ((Lifecycle) (dataobject.getPropertyByName("LIFECYCLE")))
						.getTransitionChoiceCategory();
				importstatement.add(" import " + lifecycle.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(lifecycle.getName()) + "ChoiceDefinition;");
				datacreation
						.add("		boolean isunreleased = !" + StringFormatter.formatForJavaClass(lifecycle.getName())
								+ "ChoiceDefinition.get().isChoiceFinal(oldobjectiteration.getstateforchange());");
				datacreation.add("		String unreleasedwarning = \"\";");
				datacreation.add("		if (isunreleased) unreleasedwarning = ((LifecycleDefinition)(" + objectclass
						+ ".getDefinition().getProperty(\"LIFECYCLE\"))).getUnreleasedWarningText();");

				importstatement.add("import org.openlowcode.server.data.properties.LifecycleDefinition;");
				variablename.add("unreleasedwarning");

			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				if (thislinkedfromchildren.getOriginObjectProperty().getBusinessRuleByName("SUBOJECT") != null) {
					String childclassname = StringFormatter
							.formatForJavaClass(thislinkedfromchildren.getChildObject().getName());
					importstatement.add("import " + thislinkedfromchildren.getChildObject().getOwnermodule().getPath()
							+ ".data."
							+ StringFormatter.formatForJavaClass(thislinkedfromchildren.getChildObject().getName())
							+ ";");
					variablename.add(thislinkedfromchildren.getName().toLowerCase());
					datacreation.add("		" + childclassname + "[] " + thislinkedfromchildren.getName().toLowerCase()
							+ " = " + childclassname + ".getallsubobjectsfromparentiteration(id, iteration, null);");
					otherattributes = true;
				}
			}
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) thisproperty;
				importstatement.add("import " + thisleftforlink.getLinkObjectDefinition().getOwnermodule().getPath()
						+ ".data."
						+ StringFormatter.formatForJavaClass(thisleftforlink.getLinkObjectDefinition().getName())
						+ ";");
				otherattributes = true;
				variablename.add(thisleftforlink.getName().toLowerCase());
				String linkclass = StringFormatter
						.formatForJavaClass(thisleftforlink.getLinkObjectDefinition().getName());
				datacreation.add(linkclass + "[] " + thisleftforlink.getName().toLowerCase() + " = " + linkclass
						+ ".getalllinksfromleftiteration(id, iteration,null);");

			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				// leftforlink
				if (!hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					String autolinkclass = StringFormatter
							.formatForJavaClass(hasautolink.getLinkObjectDefinition().getName());
					String autolinkname = "left" + hasautolink.getName().toLowerCase();
					importstatement.add("import " + hasautolink.getLinkObjectDefinition().getOwnermodule().getPath()
							+ ".data." + autolinkclass + ";");
					otherattributes = true;
					datacreation.add("		" + autolinkclass + "[] " + autolinkname + " = " + autolinkclass
							+ ".getalllinksfromleftiteration(id,iteration,null);");
					variablename.add(autolinkname);
				}
			}

		}

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import " + module.getPath() + ".page.generated.AtgShow" + objectvariable + "iterationPage;");
		for (int i = 0; i < importstatement.size(); i++)
			sg.wl(importstatement.get(i));
		sg.wl("");
		sg.wl("public class AtgShow" + objectvariable + "iterationAction extends AbsShow" + objectvariable
				+ "iterationAction {");
		sg.wl("");
		sg.wl("	public AtgShow" + objectvariable + "iterationAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String output = "ActionOutputData";
		if (!otherattributes)
			output = objectclass;
		sg.wl("	public " + output + " executeActionLogic(DataObjectId<" + objectclass
				+ "> id, Integer iteration,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " oldobjectiteration = " + objectclass + ".readiteration(id, iteration);");
		for (int i = 0; i < datacreation.size(); i++)
			sg.wl(datacreation.get(i));

		sg.wl("		");
		if (!otherattributes) {
			sg.wl("		return oldobjectiteration;");
		} else {
			StringBuffer additionalattributes = new StringBuffer("");
			for (int i = 0; i < variablename.size(); i++) {
				additionalattributes.append(',');
				additionalattributes.append(variablename.get(i).toLowerCase());
			}
			sg.wl("		return new ActionOutputData(oldobjectiteration" + additionalattributes.toString() + ");");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + output + " logicoutput)  {");
		if (!otherattributes) {
			sg.wl("		return new AtgShow" + objectvariable + "iterationPage(logicoutput);");
		} else {
			StringBuffer extravariable = new StringBuffer("");
			for (int i = 0; i < variablename.size(); i++) {
				extravariable.append(',');
				extravariable.append("logicoutput.get");
				extravariable.append(StringFormatter.formatForJavaClass(variablename.get(i)));
				extravariable.append("()");
			}
			sg.wl("		return new AtgShow" + objectvariable + "iterationPage(logicoutput.get" + objectclass + "()"
					+ extravariable + ");");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the change parent action to file
	 * 
	 * @param name           java name of the data object
	 * @param sg             source generator
	 * @param module         parent module
	 * @param linkedtoparent relevant linked to parent property
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateChangeParentActionToFile(
			String name,
			SourceGenerator sg,
			Module module,
			LinkedToParent<?> linkedtoparent) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		DataObjectDefinition parentobject = linkedtoparent.getParentObjectForLink();
		String parentobjectclass = StringFormatter.formatForJavaClass(parentobject.getName());
		String linkedtoparentvariable = StringFormatter
				.formatForAttribute(linkedtoparent.getInstancename().toLowerCase());
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + parentobject.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgChangeparentfor" + linkedtoparentvariable + "of" + objectvariable + "Action");
		sg.wl("		extends AbsChangeparentfor" + linkedtoparentvariable + "of" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgChangeparentfor" + linkedtoparentvariable + "of" + objectvariable + "Action(");
		sg.wl("			SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + objectclass + "> objectid, DataObjectId<" + parentobjectclass
				+ "> newparentid,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(objectid);");
		sg.wl("		" + objectvariable + ".setparentfor" + linkedtoparentvariable + "(newparentid);");
		sg.wl("		// suppressed as update now done in setparent ");
		sg.wl("		//" + objectvariable + ".update(); ");
		sg.wl("		return new ActionOutputData(objectid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)");
		sg.wl("			 {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(outputdata.getOutputid());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	public static void generatePrepareAddLinesActionToFile(
			DataObjectDefinition parent,
			SourceGenerator sg,
			Module module,
			HasMultiDimensionalChild hasmultidimensionchild) throws IOException {
		String parentclass = StringFormatter.formatForJavaClass(parent.getName());
		String childclass = StringFormatter.formatForJavaClass(
				hasmultidimensionchild.getOriginMultiDimensionChildProperty().getParent().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + parentclass + ";");
		sg.wl("import " + module.getPath() + ".data." + childclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgAddlinesfor"
				+ hasmultidimensionchild.getInstancename().toLowerCase() + "Page;");
		sg.wl("");
		sg.wl("");
		sg.wl("public class AtgPrepareaddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action");
		sg.wl("		extends");
		sg.wl("		AbsPrepareaddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action {");
		sg.wl("");
		sg.wl("	public AtgPrepareaddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + parentclass + "> activityid,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter) {");
		sg.wl("		" + parentclass + " activity = " + parentclass + ".readone(activityid);");
		sg.wl("		" + childclass + "[] children = activity.getallchildrenfor"
				+ hasmultidimensionchild.getInstancename().toLowerCase() + "(null);");
		sg.wl("		" + childclass + "[] blanks = new " + childclass + "[10];");
		sg.wl("		for (int i=0;i<blanks.length;i++) blanks[i] = new " + childclass + "();");
		sg.wl("		return new ActionOutputData(activityid, blanks, children, activity.dropIdToString());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput) {");
		sg.wl("		return new AtgAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "Page(logicoutput.get" + parentclass
				+ "id_thru(),logicoutput.getNewblanks(), logicoutput.getExistingchildren(),logicoutput.getContext());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	public static void generateAddLinesActionToFile(
			DataObjectDefinition parent,
			SourceGenerator sg,
			Module module,
			HasMultiDimensionalChild hasmultidimensionchild) throws IOException {
		String parentclass = StringFormatter.formatForJavaClass(parent.getName());
		String parentvariable = StringFormatter.formatForAttribute(parent.getName());
		String childclass = StringFormatter.formatForJavaClass(
				hasmultidimensionchild.getOriginMultiDimensionChildProperty().getParent().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + parentclass + ";");
		sg.wl("import " + module.getPath() + ".data." + childclass + ";");
		sg.wl("");
		sg.wl("");
		sg.wl("public class AtgAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action");
		sg.wl("		extends");
		sg.wl("		AbsAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action {");
		sg.wl("");
		sg.wl("	public AtgAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + parentclass + "> executeActionLogic(");
		sg.wl("			DataObjectId<" + parentclass + "> activityid,");
		sg.wl("			" + childclass + "[] newblanks,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter) {");
		sg.wl("		" + parentclass + " activity = " + parentclass + ".readone(activityid);");
		sg.wl("		activity.addlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "(newblanks);");
		sg.wl("		return activityid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + parentclass + "> activityid_thru) {");
		sg.wl("		return AtgShow" + parentvariable + "Action.get().executeAndShowPage(activityid_thru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	public static void generateAddLinesPageToFile(
			DataObjectDefinition parent,
			SourceGenerator sg,
			Module module,
			HasMultiDimensionalChild hasmultidimensionchild) throws IOException {
		String parentclass = StringFormatter.formatForJavaClass(parent.getName());
		String parentvariable = StringFormatter.formatForAttribute(parent.getName());
		String childclass = StringFormatter.formatForJavaClass(
				hasmultidimensionchild.getOriginMultiDimensionChildProperty().getParent().getName());

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("");

		sg.wl("import org.openlowcode.server.data.DataObjectFieldMarker;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SGrid;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsAddlinesfor"
				+ hasmultidimensionchild.getInstancename().toLowerCase() + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AbsShow" + parentvariable + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgAddlinesfor"
				+ hasmultidimensionchild.getInstancename().toLowerCase() + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + parentvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + parentclass + ";");
		sg.wl("import " + module.getPath() + ".data." + childclass + ";");
		sg.wl("import " + module.getPath() + ".data." + childclass + "Definition;");
		sg.wl("");
		sg.wl("");
		sg.wl("public class AtgAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Page");
		sg.wl("		extends");
		sg.wl("		AbsAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Page {");
		sg.wl("");
		sg.wl("	public AtgAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Page(");
		sg.wl("			DataObjectId<" + parentclass + "> " + parentvariable + "id_thru,");
		sg.wl("			" + childclass + "[] newblanks,");
		sg.wl("			" + childclass + "[] existingchildren,");
		sg.wl("			String context) {");
		sg.wl("		super(" + parentvariable + "id_thru, newblanks, existingchildren, context);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(");
		sg.wl("			DataObjectId<" + parentclass + "> " + parentvariable + "id_thru,");
		sg.wl("			" + childclass + "[] newblanks,");
		sg.wl("			" + childclass + "[] existingchildren,");
		sg.wl("			String context) {");
		sg.wl("");
		sg.wl("		return \"Add lines\";");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent() {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
		sg.wl("		mainband.addElement(new SPageText(\"Add lines\", SPageText.TYPE_TITLE, this));");
		sg.wl("		mainband.addElement(new SPageText(this.getContext(), SPageText.TYPE_NORMAL, this));");
		sg.wl("		");
		sg.wl("		SObjectIdStorage<" + parentclass + "> " + parentvariable + "idstorage = new SObjectIdStorage<"
				+ parentclass + ">(\"ACTIVITYIDSTORAGE\",this,this.get" + parentclass + "id_thru());");
		sg.wl("		mainband.addElement(" + parentvariable + "idstorage);");
		sg.wl("		SObjectArray<" + childclass + "> blankarray = new SObjectArray<" + childclass
				+ ">(\"BLANKARRAY\", this.getNewblanks(),");
		sg.wl("				" + childclass + ".getDefinition(), this);");
		sg.wl("		blankarray.setRowsToDisplay(10);");
		Field[] payload = hasmultidimensionchild.getOriginMultiDimensionChildProperty().getPayloadValue();
		Field firstaxisvalue = hasmultidimensionchild.getOriginMultiDimensionChildProperty().getFirstAxisValue();
		for (int i = 0; i < payload.length; i++)
			sg.wl("		blankarray.hideAttribute(" + childclass + ".get"
					+ StringFormatter.formatForJavaClass(payload[i].getName()) + "FieldMarker());");
		sg.wl("		blankarray.hideAttribute(" + childclass + ".get"
				+ StringFormatter.formatForJavaClass(firstaxisvalue.getName()) + "FieldMarker());");
		if (parent.getPropertyByName("LIFECYCLE") != null)
			sg.wl("		blankarray.hideAttribute(" + childclass + ".getStateFieldMarker());");
		sg.wl("		blankarray.setMinFieldPriority(0);");
		sg.wl("		mainband.addElement(blankarray);");
		sg.wl("");
		sg.wl("		AbsAddlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "Action.ActionRef addchildrenaction = AtgAddlinesfor"
				+ hasmultidimensionchild.getInstancename().toLowerCase() + "Action.get().getActionRef();");
		sg.wl("		addchildrenaction.set" + parentclass + "id(" + parentvariable + "idstorage.getObjectIdInput());");
		sg.wl("		addchildrenaction.setNewblanks(blankarray.getActiveObjectArray());");

		sg.wl("		ArrayList<DataObjectFieldMarker<" + childclass
				+ ">> editionfields = new ArrayList<DataObjectFieldMarker<" + childclass + ">>();");
		Field[] secondaryaxis = hasmultidimensionchild.getOriginMultiDimensionChildProperty().getSecondAxisValue();
		for (int i = 0; i < secondaryaxis.length; i++)
			sg.wl("		editionfields.add(" + childclass + ".get"
					+ StringFormatter.formatForJavaClass(secondaryaxis[i].getName()) + "FieldMarker());");

		sg.wl("		blankarray.addUpdateAction(addchildrenaction, editionfields, true,false);");

		sg.wl("");
		sg.wl("		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
		sg.wl("");
		sg.wl("		AbsShow" + parentvariable + "Action.ActionRef backtoparent = AtgShow" + parentvariable
				+ "Action.get().getActionRef();");
		sg.wl("		backtoparent.setId(" + parentvariable + "idstorage.getObjectIdInput());");
		sg.wl("");
		sg.wl("		buttonband.addElement(new SActionButton(\"Back\", backtoparent,this));");
		sg.wl("		buttonband.addElement(new SActionButton(\"Create new\",addchildrenaction,this));");
		sg.wl("		mainband.addElement(buttonband);");
		sg.wl("		mainband.addElement(new SPageText(\"Existing Elements below\", SPageText.TYPE_TITLE, this));");
		sg.wl("");

		hasmultidimensionchild.getOriginMultiDimensionChildProperty().getLinkedToParent().getLinkedFromChildren()
				.writeGrid(sg, "EXISTING", "existing", "existingchildren");

		sg.wl("		mainband.addElement(existing);");
		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	public static void generateRepairMultiDimensionChildrenToFile(
			DataObjectDefinition parent,
			SourceGenerator sg,
			Module module,
			HasMultiDimensionalChild hasmultidimensionchild) throws IOException {
		String parentclass = StringFormatter.formatForJavaClass(parent.getName());
		String parentvariable = StringFormatter.formatForAttribute(parent.getName());
		String path = module.getPath();

		sg.wl("package " + path + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		;
		sg.wl("import " + path + ".action.generated.AtgShow" + parentvariable + "Action;");
		sg.wl("import " + path + ".data." + parentclass + ";");
		sg.wl("");
		sg.wl("public class AtgRepairlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action");
		sg.wl("		extends");
		sg.wl("		AbsRepairlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase() + "Action {");
		sg.wl("");
		sg.wl("	public AtgRepairlinesfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		sg.wl("			DataObjectId<" + parentclass + "> " + parentvariable + "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter) {");
		sg.wl("		" + parentclass + " " + parentvariable + " = " + parentclass + ".readone(" + parentvariable
				+ "id);");
		sg.wl("		" + parentvariable + ".repairfor" + hasmultidimensionchild.getInstancename().toLowerCase()
				+ "(null);");
		sg.wl("		return new ActionOutputData(" + parentvariable + "id);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput) {");
		sg.wl("		return AtgShow" + parentvariable + "Action.get().executeAndShowPage(logicoutput.get" + parentclass
				+ "id_thru());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

	/**
	 * generates the download attachment action for the data object
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateDownloadAttachment(String name, SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import org.openlowcode.module.system.data.Binaryfile;");
		sg.wl("import org.openlowcode.module.system.data.Objattachment;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("");
		sg.wl("public class AtgDownloadattachmentfor" + objectvariable + "Action extends AbsDownloadattachmentfor"
				+ objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgDownloadattachmentfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> " + objectvariable
				+ ",DataObjectId<Objattachment> attachmentid,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		Objattachment attachment = Objattachment.readone(attachmentid);");
		sg.wl("		if (attachment.getGenericlinkforownerid().getId().compareTo(" + objectvariable
				+ ".getId())!=0) throw new RuntimeException(\"object and attachment parent id not consistent\");");
		sg.wl("		Binaryfile file = Binaryfile.readone(attachment.getLinkedtoparentforcontentid());");
		sg.wl("		return new ActionOutputData(new SFile(file.getFilename(),file.getFilecontent().getContent()));");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		throw new RuntimeException(\"This action can only be called inline\");");
		sg.wl("	}");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the add attachment action for the data object
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateaddAttachment(String name, SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.module.system.data.Objattachment;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgAddnewattachmentfor" + objectvariable + "Action extends AbsAddnewattachmentfor"
				+ objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgAddnewattachmentfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass + "> "
				+ objectvariable + ", String comment, SFile file,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " this" + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ ");");
		sg.wl("		Objattachment attachment = new Objattachment();");
		sg.wl("		attachment.setComment(comment);");
		sg.wl("		this" + objectvariable + ".addattachment(attachment, file);");
		sg.wl("		return this" + objectvariable + ".getId();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> id)  {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(id);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the get full image action for the data object
	 * 
	 * @param name         java name of the data object
	 * @param imagecontent image content property
	 * @param sg           source generator
	 * @param module       parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateGetFullImageToFile(
			String name,
			ImageContent imagecontent,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		String imageclassvariable = imagecontent.getInstancename().toLowerCase();

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("");
		sg.wl("public class AtgGetfullimagefor" + imageclassvariable + "for" + objectvariable
				+ "Action extends AbsGetfullimagefor" + imageclassvariable + "for" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgGetfullimagefor" + imageclassvariable + "for" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> " + objectvariable
				+ "id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		SFile file = " + objectvariable + ".getfullimagefor" + imageclassvariable + "();");
		sg.wl("		return new ActionOutputData(file);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		throw new RuntimeException(\"action can only be called as inline action.\");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the set image to file action for the data object
	 * 
	 * @param name         java name of the data object
	 * @param imagecontent image content property
	 * @param sg           source generator
	 * @param module       parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateSetImageToFile(String name, ImageContent imagecontent, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		String imageclassvariable = imagecontent.getInstancename().toLowerCase();

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgSetimagecontentfor" + imageclassvariable + "for" + objectvariable
				+ "Action extends AbsSetimagecontentfor" + imageclassvariable + "for" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgSetimagecontentfor" + imageclassvariable + "for" + objectvariable
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass + "> "
				+ objectvariable + "id, SFile fullimage, SFile thumbnail,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(" + objectvariable
				+ "id);");
		sg.wl("		" + objectvariable + ".setimagefor" + imageclassvariable + "(fullimage, thumbnail);");
		sg.wl("		return " + objectvariable + "id;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> id)  {");
		sg.wl("		AtgShow" + objectvariable + "Action show" + objectvariable + "action = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return show" + objectvariable + "action.executeAndShowPage(id);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the show autolink tree action to file
	 * 
	 * @param name     java name of the data object
	 * @param sg       source generator
	 * @param module   parent module
	 * @param autolink autolink object property relevant for this file generation
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateShowAutoLinkTreeToFile(
			String name,
			SourceGenerator sg,
			Module module,
			AutolinkObject<?> autolink) throws IOException {
		String linkobjectclass = StringFormatter.formatForJavaClass(autolink.getParent().getName());
		String linkobjectattribute = StringFormatter.formatForAttribute(autolink.getParent().getName());
		String linkedobjectclass = StringFormatter.formatForJavaClass(autolink.getObjectforlink().getName());
		String linkedobjectattribute = StringFormatter.formatForAttribute(autolink.getObjectforlink().getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + linkedobjectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + linkobjectclass + ";");
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgShowautolinktreefor" + linkobjectattribute + "Action extends AbsShowautolinktreefor"
				+ linkobjectattribute + "Action {");
		sg.wl("");
		sg.wl("	public AtgShowautolinktreefor" + linkobjectattribute + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + linkedobjectclass + "> "
				+ linkedobjectattribute + "id,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		NodeTree<" + linkobjectclass + "> answer = new NodeTree<" + linkobjectclass + ">(" + linkobjectclass
				+ ".getDefinition());");
		sg.wl("		" + linkobjectclass + "[] children = " + linkobjectclass + ".getalllinksfromleftid("
				+ linkedobjectattribute + "id, null);");
		sg.wl("		for (int i=0;i<children.length;i++) {");
		sg.wl("			getChildren(answer,null,children[i],0);");
		sg.wl("		}");
		sg.wl("		ActionOutputData output = new ActionOutputData(answer);");
		sg.wl("		return output;");
		sg.wl("	}");
		sg.wl("	");

		sg.wl("	private void getChildren(NodeTree<" + linkobjectclass + "> nodetree," + linkobjectclass + " parent,"
				+ linkobjectclass + " child,int circuitbreaker)  {");
		sg.wl("		if (circuitbreaker>1024) throw new RuntimeException(\"circuitbreaker is exceeded\");");
		sg.wl("		boolean newchild = nodetree.addChild(parent, child);");
		sg.wl("		");
		sg.wl("		if (newchild) {");
		sg.wl("			" + linkobjectclass + "[] grandchildren = " + linkobjectclass
				+ ".getalllinksfromleftid(child.getRgid(),null);");
		sg.wl("			for (int i=0;i<grandchildren.length;i++) {");
		sg.wl("				getChildren(nodetree,child,grandchildren[i],circuitbreaker+1);");
		sg.wl("			}");
		sg.wl("		}");
		sg.wl("	}");

		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {	");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}		");
		sg.close();
	}

	/**
	 * generates the code for the standard creation action without companion
	 * 
	 * @param dataobject data object definition
	 * @param companion  companion object (can be null)
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateStandardCreateActionToFile(
			DataObjectDefinition dataobject,
			DataObjectDefinition companion,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String actionname = objectvariable;
		String companionclass = null;
		if (companion != null) {
			actionname = StringFormatter.formatForAttribute(companion.getName());
			companionclass = StringFormatter.formatForJavaClass(companion.getName());
		}

		// ------- prepare generation

		HashMap<String, String> importdeclaration = dataobject.getImportDeclarationForCreation(dataobject);

		StringBuffer extraattributesdeclaration = dataobject.generateCreateObjectExtraAttributes(dataobject);

		String objectimport = "import " + module.getPath() + ".data." + objectclass + ";";
		if (companion != null) {
			String companionimport = "import " + companion.getOwnermodule().getPath() + ".data." + companionclass + ";";
			importdeclaration.put(companionimport, companionimport);
		}
		importdeclaration.put(objectimport, objectimport);
		String dataobjectidimport = "import org.openlowcode.server.data.properties.DataObjectId;";
		importdeclaration.put(dataobjectidimport, dataobjectidimport);

		// -------- generated
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		sg.wl("import org.openlowcode.server.action.SecurityInDataMethod;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import java.util.Date;");
		sg.wl("");
		sg.wl("public class AtgStandardcreate" + actionname + "Action extends");
		sg.wl("		AbsStandardcreate" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public AtgStandardcreate" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		if (extraattributesdeclaration.length() > 0)
			extraattributesdeclaration.append(',');
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic( " + extraattributesdeclaration.toString()
				+ " " + objectclass + " object" + (companion != null ? "," + companionclass + " companion" : "")
				+ ",Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);

			String[] methodsforcreation = thisproperty.getPropertyInitMethod();
			if (!thisproperty.isDataInputHiddenForCreation())
				if (methodsforcreation != null)
					for (int j = 0; j < methodsforcreation.length; j++) {
						sg.wl("		object" + methodsforcreation[j]);
					}
		}
		if (dataobject.getPropertyByName("TYPED") != null) {
			sg.wl("		object.settypebeforecreation(type);");
		}
		if (companion != null) {
			sg.wl("		companion.createtyped(object,type,this,SecurityInDataMethod.FAIL_IF_NOT_AUTHORIZED);");
		} else {
			sg.wl("		object.insert(this,SecurityInDataMethod.FAIL_IF_NOT_AUTHORIZED);");
		}

		sg.wl("		return object.getId();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> newobjectid)");
		sg.wl("			 {");
		LinkedToParent<?> subobject = dataobject.isSubObject();
		if (subobject != null) {
			String parentattribute = StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName());

			sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(newobjectid);");
			sg.wl("		return AtgShow" + parentattribute + "Action.get().executeAndShowPage(" + objectvariable + ".get"
					+ StringFormatter.formatForJavaClass(subobject.getName()) + "id());");

		} else {

			sg.wl("		return AtgShow" + objectvariable + "Action.get().executeAndShowPage(newobjectid);");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the code for the standard creation action without companion
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateStandardCreateActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		generateStandardCreateActionToFile(dataobject, null, sg, module);
	}

	/**
	 * generates the code for the duplicate (somehow deep copy) action
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateDuplicateActionToFile(DataObjectDefinition dataobject, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");

		sg.wl("import java.util.function.Function;");
		sg.wl("import java.util.HashMap;");
		sg.wl("");
		sg.wl("import " + dataobject.getOwnermodule().getPath() + ".data." + objectclass + ";");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++)
			if (dataobject.propertylist.get(i) instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) dataobject.propertylist.get(i);
				DataObjectDefinition linkobject = thisleftforlink.getLinkObjectDefinition();
				sg.wl("import " + linkobject.getOwnermodule().getPath() + ".data."
						+ StringFormatter.formatForJavaClass(linkobject.getName()) + ";");
			}
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.properties.NumberedQueryHelper;");
		sg.wl("import org.openlowcode.server.data.properties.StoredobjectQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");

		sg.wl("");
		sg.wl("public class AtgDuplicate" + objectvariable + "Action extends");
		sg.wl("		AbsDuplicate" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgDuplicate" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> originid,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");

		sg.wl("		" + objectclass + " originobject = " + objectclass + ".readone(originid);");
		sg.wl("		" + objectclass + " newobject = originobject.deepcopy();");
		if (dataobject.isManualNumbered()) {

			sg.wl("		// -- treat number not managed by autonumbering");
			sg.wl("		TableAlias queryalias = " + objectclass
					+ ".getDefinition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);");
			sg.wl("		" + objectclass + "[] " + objectvariable + "s = " + objectclass
					+ ".getallactive(QueryFilter.get(NumberedQueryHelper.get().getNrLikeQueryCondition(queryalias,originobject.getNr()+\"%\","
					+ objectclass + ".getDefinition()),queryalias));");
			sg.wl("		HashMap<String," + objectclass + "> " + objectvariable + "sbynr = new HashMap<String,"
					+ objectclass + ">();");
			sg.wl("		for (int i=0;i<" + objectvariable + "s.length;i++) " + objectvariable + "sbynr.put("
					+ objectvariable + "s[i].getNr()," + objectvariable + "s[i]);");
			sg.wl("		int copyindex=1;");
			sg.wl("		while (" + objectvariable + "sbynr.get(originobject.getNr()+\"§\"+copyindex)!=null) {");
			sg.wl("			if (copyindex>100) throw new RuntimeException(\"No available number found for saveas for number \"+originobject.getNr());");
			sg.wl("			copyindex++;");
			sg.wl("		}");
			sg.wl("		newobject.setobjectnumber(originobject.getNr()+\"§\"+copyindex);	");

		}
		LinkedToParent<?>[] parents = dataobject.getParents();
		if (parents != null)
			for (int i = 0; i < parents.length; i++) {
				LinkedToParent<?> currentparent = parents[i];
				String parentname = currentparent.getInstancename().toLowerCase();
				sg.wl("		newobject.setparentwithoutupdatefor" + parentname + "(originobject.getLinkedtoparentfor"
						+ parentname + "id());");
			}
		sg.wl("		newobject.insert();");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++)
			if (dataobject.propertylist.get(i) instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) dataobject.propertylist.get(i);
				DataObjectDefinition linkobject = thisleftforlink.getLinkObjectDefinition();
				String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
				String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());

				sg.wl("		// -------------- treat link " + linkobjectclass);
				sg.wl("		" + linkobjectclass + "[] " + linkobjectvariable + "tocopy = " + linkobjectclass
						+ ".getalllinksfromleftid(originid,null);");
				sg.wl("		if (" + linkobjectvariable + "tocopy!=null) for (int i=0;i<" + linkobjectvariable
						+ "tocopy.length;i++) {");
				sg.wl("			" + linkobjectclass + " originlink = " + linkobjectvariable + "tocopy[i];");
				sg.wl("			" + linkobjectclass + " newlink = originlink.deepcopy();");
				sg.wl("			newlink.setleftobject(newobject.getId());");
				sg.wl("			newlink.setrightobject(originlink.getRgid());");
				sg.wl("			newlink.insert();");
				sg.wl("			}");

			}

		sg.wl("		return new ActionOutputData(newobject);");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return AtgShow" + objectvariable
				+ "Action.get().executeAndShowPage(logicoutput.getCopyobject().getId());");
		sg.wl("	}");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the code for create link action (and show left object)
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */

	public static void generateCreateLinkToMasterActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		LinkObjectToMaster<
				?,
				?> linkobjecttomaster = (LinkObjectToMaster<?, ?>) dataobject.getPropertyByName("LINKOBJECTTOMASTER");
		String leftobjectclassname = StringFormatter
				.formatForJavaClass(linkobjecttomaster.getLeftobjectforlink().getName());
		String leftobjectattributename = StringFormatter
				.formatForAttribute(linkobjecttomaster.getLeftobjectforlink().getName());
		String rightobjectclassname = StringFormatter
				.formatForJavaClass(linkobjecttomaster.getRightobjectforlink().getName());
		String rightobjectattributename = StringFormatter
				.formatForAttribute(linkobjecttomaster.getRightobjectforlink().getName());
		String linkclassname = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkattributename = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsCreate" + linkattributename + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AbsShow" + leftobjectattributename + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + leftobjectattributename + "Action;");
		Module rightobjectmodule = linkobjecttomaster.getRightobjectforlink().getOwnermodule();
		sg.wl("import " + rightobjectmodule.getPath() + ".data." + rightobjectclassname + ";");
		sg.wl("import " + module.getPath() + ".data." + linkclassname + ";");
		Module leftobjectmodule = linkobjecttomaster.getLeftobjectforlink().getOwnermodule();
		sg.wl("import " + leftobjectmodule.getPath() + ".data." + leftobjectclassname + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectMasterId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgCreate" + linkattributename + "Action extends AbsCreate" + linkattributename
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgCreate" + linkattributename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	private DataObjectId<" + leftobjectclassname + "> leftobject" + leftobjectattributename + ";");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + linkclassname + ">[] executeActionLogic(DataObjectId<" + leftobjectclassname
				+ "> leftobject" + leftobjectattributename + ",");
		sg.wl("			" + linkclassname + " " + linkattributename + ",");
		sg.wl("			DataObjectMasterId<" + rightobjectclassname + ">[] rightobject" + rightobjectattributename
				+ ",Function<TableAlias,QueryFilter> datafilter)  {");

		sg.wl("		DataObjectId<" + linkclassname + ">[] answerid = new DataObjectId[rightobject"
				+ rightobjectattributename + ".length];");
		sg.wl("		for (int i=0;i< rightobject" + rightobjectattributename + ".length;i++) {");
		sg.wl("			" + linkclassname + " copyof" + linkattributename + " = " + linkattributename + ".deepcopy();");

		sg.wl("			copyof" + linkattributename + ".setleftobject(leftobject" + leftobjectattributename + ");");
		sg.wl("			copyof" + linkattributename + ".setrightobjectmaster(rightobject" + rightobjectattributename
				+ "[i]);");
		sg.wl("			copyof" + linkattributename + ".insert();");
		sg.wl("			answerid[i] = copyof" + linkattributename + ".getId();");
		sg.wl("			}");
		sg.wl("		this.leftobject" + leftobjectattributename + " = leftobject" + leftobjectattributename + ";");
		sg.wl("		return answerid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + linkclassname + ">[] newlinkid)  {");
		sg.wl("		AbsShow" + leftobjectattributename + "Action action =  AtgShow" + leftobjectattributename
				+ "Action.get();");
		sg.wl("		return action.executeAndShowPage(leftobject" + leftobjectattributename + ");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for create link action (and show left object)
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */

	public static void generateCreateLinkActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) dataobject.getPropertyByName("LINKOBJECT");
		String leftobjectclassname = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectattributename = StringFormatter
				.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String rightobjectclassname = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectattributename = StringFormatter
				.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String linkclassname = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkattributename = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsCreate" + linkattributename + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AbsShow" + leftobjectattributename + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + leftobjectattributename + "Action;");
		Module rightobjectmodule = linkobject.getRightobjectforlink().getOwnermodule();
		sg.wl("import " + rightobjectmodule.getPath() + ".data." + rightobjectclassname + ";");
		sg.wl("import " + module.getPath() + ".data." + linkclassname + ";");
		Module leftobjectmodule = linkobject.getLeftobjectforlink().getOwnermodule();
		sg.wl("import " + leftobjectmodule.getPath() + ".data." + leftobjectclassname + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgCreate" + linkattributename + "Action extends AbsCreate" + linkattributename
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgCreate" + linkattributename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	private DataObjectId<" + leftobjectclassname + "> leftobject" + leftobjectattributename + ";");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + linkclassname + ">[] executeActionLogic(DataObjectId<" + leftobjectclassname
				+ "> leftobject" + leftobjectattributename + ",");
		sg.wl("			" + linkclassname + " " + linkattributename + ",");
		sg.wl("			DataObjectId<" + rightobjectclassname + ">[] rightobject" + rightobjectattributename
				+ ",Function<TableAlias,QueryFilter> datafilter)  {");

		sg.wl("		DataObjectId<" + linkclassname + ">[] answerid = new DataObjectId[rightobject"
				+ rightobjectattributename + ".length];");
		sg.wl("		for (int i=0;i< rightobject" + rightobjectattributename + ".length;i++) {");
		sg.wl("			" + linkclassname + " copyof" + linkattributename + " = " + linkattributename + ".deepcopy();");

		sg.wl("			copyof" + linkattributename + ".setleftobject(leftobject" + leftobjectattributename + ");");
		sg.wl("			copyof" + linkattributename + ".setrightobject(rightobject" + rightobjectattributename
				+ "[i]);");
		sg.wl("			copyof" + linkattributename + ".insert();");
		sg.wl("			answerid[i] = copyof" + linkattributename + ".getId();");
		sg.wl("			}");
		sg.wl("		this.leftobject" + leftobjectattributename + " = leftobject" + leftobjectattributename + ";");
		sg.wl("		return answerid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + linkclassname + ">[] newlinkid)  {");
		sg.wl("		AbsShow" + leftobjectattributename + "Action action =  AtgShow" + leftobjectattributename
				+ "Action.get();");
		sg.wl("		return action.executeAndShowPage(leftobject" + leftobjectattributename + ");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for create link action (and show right object)
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateCreateLinkActionAndShowRightToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) dataobject.getPropertyByName("LINKOBJECT");
		String leftobjectclassname = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectattributename = StringFormatter
				.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String rightobjectclassname = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectattributename = StringFormatter
				.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String linkclassname = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkattributename = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsCreate" + linkattributename + "Action;");
		Module rightobjectmodule = linkobject.getRightobjectforlink().getOwnermodule();
		sg.wl("import " + rightobjectmodule.getPath() + ".action.generated.AbsShow" + rightobjectattributename
				+ "Action;");
		sg.wl("import " + rightobjectmodule.getPath() + ".action.generated.AtgShow" + rightobjectattributename
				+ "Action;");

		sg.wl("import " + rightobjectmodule.getPath() + ".data." + rightobjectclassname + ";");
		sg.wl("import " + module.getPath() + ".data." + linkclassname + ";");
		Module leftobjectmodule = linkobject.getLeftobjectforlink().getOwnermodule();
		sg.wl("import " + leftobjectmodule.getPath() + ".data." + leftobjectclassname + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action extends AbsCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	private DataObjectId<" + rightobjectclassname + "> rightobject" + rightobjectattributename + ";");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + linkclassname + ">[] executeActionLogic(DataObjectId<" + leftobjectclassname
				+ ">[] leftobject" + leftobjectattributename + ",");
		sg.wl("			" + linkclassname + " " + linkattributename + ",");
		sg.wl("			DataObjectId<" + rightobjectclassname + "> rightobject" + rightobjectattributename
				+ ",Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		DataObjectId<" + linkclassname + ">[] answerid = new DataObjectId[leftobject"
				+ leftobjectattributename + ".length];");
		sg.wl("		for (int i=0;i< leftobject" + leftobjectattributename + ".length;i++) {");
		sg.wl("			" + linkclassname + " copyof" + linkattributename + " = " + linkattributename + ".deepcopy();");
		sg.wl("			copyof" + linkattributename + ".setleftobject(leftobject" + leftobjectattributename + "[i]);");
		sg.wl("			copyof" + linkattributename + ".setrightobject(rightobject" + rightobjectattributename + ");");
		sg.wl("			copyof" + linkattributename + ".insert();");
		sg.wl("			answerid[i] = copyof" + linkattributename + ".getId();");
		sg.wl("			}");
		sg.wl("		this.rightobject" + rightobjectattributename + " = rightobject" + rightobjectattributename + ";");
		sg.wl("		return answerid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + linkclassname + ">[] newlinkid)  {");
		sg.wl("		AbsShow" + rightobjectattributename + "Action action =  AtgShow" + rightobjectattributename
				+ "Action.get();");
		sg.wl("		return action.executeAndShowPage(rightobject" + rightobjectattributename + ");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for create link action (and show right object)
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateCreateLinkToMasterActionAndShowRightToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		LinkObjectToMaster<
				?,
				?> linkobjecttomaster = (LinkObjectToMaster<?, ?>) dataobject.getPropertyByName("LINKOBJECTTOMASTER");
		String leftobjectclassname = StringFormatter
				.formatForJavaClass(linkobjecttomaster.getLeftobjectforlink().getName());
		String leftobjectattributename = StringFormatter
				.formatForAttribute(linkobjecttomaster.getLeftobjectforlink().getName());
		String rightobjectclassname = StringFormatter
				.formatForJavaClass(linkobjecttomaster.getRightobjectforlink().getName());
		String rightobjectattributename = StringFormatter
				.formatForAttribute(linkobjecttomaster.getRightobjectforlink().getName());
		String linkclassname = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkattributename = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsCreate" + linkattributename + "Action;");
		Module rightobjectmodule = linkobjecttomaster.getRightobjectforlink().getOwnermodule();
		sg.wl("import " + rightobjectmodule.getPath() + ".action.generated.AbsShow" + rightobjectattributename
				+ "Action;");
		sg.wl("import " + rightobjectmodule.getPath() + ".action.generated.AtgShow" + rightobjectattributename
				+ "Action;");

		sg.wl("import " + rightobjectmodule.getPath() + ".data." + rightobjectclassname + ";");
		sg.wl("import " + module.getPath() + ".data." + linkclassname + ";");
		Module leftobjectmodule = linkobjecttomaster.getLeftobjectforlink().getOwnermodule();
		sg.wl("import " + leftobjectmodule.getPath() + ".data." + leftobjectclassname + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectMasterId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action extends AbsCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgCreate" + linkattributename + "andshowright" + rightobjectattributename
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	private DataObjectId<" + rightobjectclassname + "> rightobject" + rightobjectattributename + ";");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + linkclassname + ">[] executeActionLogic(DataObjectId<" + leftobjectclassname
				+ ">[] leftobject" + leftobjectattributename + ",");
		sg.wl("			" + linkclassname + " " + linkattributename + ",");
		sg.wl("			DataObjectId<" + rightobjectclassname + "> rightobject" + rightobjectattributename
				+ ",Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		DataObjectMasterId<" + rightobjectclassname + "> rightobjectmsid = " + rightobjectclassname
				+ ".readone(rightobject" + rightobjectattributename + ").getMasterid();");
		sg.wl("		DataObjectId<" + linkclassname + ">[] answerid = new DataObjectId[leftobject"
				+ leftobjectattributename + ".length];");
		sg.wl("		for (int i=0;i< leftobject" + leftobjectattributename + ".length;i++) {");
		sg.wl("			" + linkclassname + " copyof" + linkattributename + " = " + linkattributename + ".deepcopy();");
		sg.wl("			copyof" + linkattributename + ".setleftobject(leftobject" + leftobjectattributename + "[i]);");
		sg.wl("			copyof" + linkattributename + ".setrightobjectmaster(rightobjectmsid);");
		sg.wl("			copyof" + linkattributename + ".insert();");
		sg.wl("			answerid[i] = copyof" + linkattributename + ".getId();");
		sg.wl("			}");
		sg.wl("		this.rightobject" + rightobjectattributename + " = rightobject" + rightobjectattributename + ";");
		sg.wl("		return answerid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + linkclassname + ">[] newlinkid)  {");
		sg.wl("		AbsShow" + rightobjectattributename + "Action action =  AtgShow" + rightobjectattributename
				+ "Action.get();");
		sg.wl("		return action.executeAndShowPage(rightobject" + rightobjectattributename + ");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for the standard create action
	 * 
	 * @param dataobject data object definition
	 * @param companion  companion object (can be null)
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generatePrepareStandardCreateActionToFile(
			DataObjectDefinition dataobject,
			DataObjectDefinition companion,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String actionname = objectvariable;
		String companionclass = null;
		if (companion != null) {
			actionname = StringFormatter.formatForAttribute(companion.getName());
			companionclass = StringFormatter.formatForJavaClass(companion.getName());
		}

		sg.wl("package " + module.getPath() + ".action.generated;");
		HashMap<String, String> importdeclaration = new HashMap<String, String>();

		StringBuffer extraattributesdeclaration = new StringBuffer();
		StringBuffer extraattributesfilling = new StringBuffer();
		StringBuffer extraattributestopage = new StringBuffer();
		StringBuffer companionactionattributecall=new StringBuffer();
		// ------------------------ Attributes for properties ---------------------
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {

				ArgumentContent thisargument = thisproperty.getContextDataForCreation(j);
				if (extraattributesdeclaration.length() > 0)
					extraattributesdeclaration.append(" , ");
				extraattributesdeclaration
						.append(" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");

				if (extraattributesfilling.length() > 0)
					extraattributesfilling.append(" , ");
				extraattributesfilling.append(" " + thisargument.getName().toLowerCase() + " ");

				if (extraattributestopage.length() > 0)
					extraattributestopage.append(" , ");
				extraattributestopage.append("logicoutput.getCopy" + thisargument.getName().toLowerCase() + "()");

				if (companionactionattributecall.length()>0)
					companionactionattributecall.append(" , ");

				companionactionattributecall.append("logicoutput.getCopy");
				companionactionattributecall.append(thisargument.getName().toLowerCase());
				companionactionattributecall.append("()");
				
				ArrayList<String> imports = thisargument.getImports();
				for (int k = 0; k < imports.size(); k++) {
					importdeclaration.put(imports.get(k), imports.get(k));
				}
			}
			if (!thisproperty.isDataInputHiddenForCreation())
				for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);

					if (extraattributesfilling.length() > 0)
						extraattributesfilling.append(" , ");
					extraattributesfilling.append(" " + thisargument.initblank() + " ");
					if (extraattributestopage.length() > 0)
						extraattributestopage.append(" , ");
					extraattributestopage.append(
							"logicoutput.get" + StringFormatter.formatForJavaClass(thisargument.getName()) + "()");

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {
						importdeclaration.put(imports.get(k), imports.get(k));
					}
				}
		}
		// ------------------------ Attributes for field suggestions
		// ---------------------
		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {

					if (extraattributesfilling.length() > 0)
						extraattributesfilling.append(" , ");
					extraattributesfilling.append(" suggestionsforfield" + stringfield.getName().toLowerCase() + " ");
					if (extraattributestopage.length() > 0)
						extraattributestopage.append(" , ");
					extraattributestopage
							.append("logicoutput.getSuggestionsforfield" + stringfield.getName().toLowerCase() + "()");
				}
			}
		}

		String objectimport = "import " + dataobject.getOwnermodule().getPath() + ".data." + objectclass + ";";
		importdeclaration.put(objectimport, objectimport);
		if (companion != null) {
			String companionimport = "import " + companion.getOwnermodule().getPath() + ".data." + companionclass + ";";
			importdeclaration.put(companionimport, companionimport);
		}
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}

		sg.wl("");
		sg.wl("import " + module.getPath() + ".page.generated.AtgStandardcreate" + actionname + "Page;");
		if (dataobject.getPropertyByName("TYPED")!=null) {
			Typed typedproperty =(Typed) dataobject.getPropertyByName("TYPED");
			for (int i=0;i<typedproperty.getCompanionNumber();i++) {
				DataObjectDefinition specificcompanion = typedproperty.getCompanion(i);
				sg.wl("import " + specificcompanion.getOwnermodule().getPath() + ".page.generated.AtgStandardcreate" + specificcompanion.getName().toLowerCase() + "Page;");
			}
		}
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import java.util.Date;");
		sg.wl("");
		sg.wl("public class AtgPreparestandardcreate" + actionname + "Action extends");
		sg.wl("		AbsPreparestandardcreate" + actionname + "Action {");
		sg.wl("");
		sg.wl("	public AtgPreparestandardcreate" + actionname + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");

		String output = "ActionOutputData";
		if (extraattributesfilling.length() > 0)
			extraattributesfilling.append(',');
		if (extraattributestopage.length() > 0)
			extraattributestopage.append(',');

		sg.wl("	public " + output + " executeActionLogic(" + extraattributesdeclaration.toString()
				+ (extraattributesdeclaration.length() > 0 ? "," : "")
				+ "Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					sg.wl("		String[] suggestionsforfield" + stringfield.getName().toLowerCase() + " = "
							+ objectclass + ".getValuesForField"
							+ StringFormatter.formatForJavaClass(stringfield.getName().toLowerCase()) + "(null);");
				}
			}
		}
		sg.wl("		return new ActionOutputData(" + extraattributesfilling.toString() + "new " + objectclass + "()"
				+ (companion != null ? ", new " + companionclass + "()" : "") + ");");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");

		if (dataobject.getPropertyByName("TYPED") != null)
			if (companion == null) {
				Typed typed = (Typed) (dataobject.getPropertyByName("TYPED"));
				Iterator<ChoiceValue> types = typed.getTypesIterator();
				while (types.hasNext()) {
					ChoiceValue thistype = types.next();
					DataObjectDefinition specificcompanion = typed.getCompanionForType(thistype);
					if (specificcompanion != null) {
						
						sg.wl("		if ("+StringFormatter.formatForJavaClass(typed.getTypes().getName())+"ChoiceDefinition.get()."+thistype.getName().toUpperCase()+".getStorageCode().equals(logicoutput.getCopytype().getStorageCode()))");
						sg.wl("			return AtgPreparestandardcreate"+specificcompanion.getName().toLowerCase()+"Action.get().executeAndShowPage("+ companionactionattributecall.toString()+");");


						
					}
				}
			}

		sg.wl("		return new AtgStandardcreate" + actionname + "Page(" + extraattributestopage.toString()
				+ "logicoutput.getObject()" + (companion != null ? ",logicoutput.getCompanion()" : "") + ");");
		sg.wl("	}");

		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the code for the standard create action without companion object
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generatePrepareStandardCreateActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		generatePrepareStandardCreateActionToFile(dataobject, null, sg, module);
	}

	/**
	 * generate an action creating a link and right object action
	 * 
	 * @param dataobject         data object definition
	 * @param sg                 source generator
	 * @param module             parent module
	 * @param linkobjectproperty the link object to generate the action for
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateCreateLinkAndRightObjectActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module,
			LinkObject<?, ?> linkobjectproperty) throws IOException {
		DataObjectDefinition rightobjecttocreate = linkobjectproperty.getRightobjectforlink();
		DataObjectDefinition originleftobject = linkobjectproperty.getLeftobjectforlink();
		HashMap<String, String> importdeclaration = dataobject.getImportDeclarationForCreation(rightobjecttocreate);
		StringBuffer extraattributesdeclaration = dataobject.generateCreateObjectExtraAttributes(rightobjecttocreate);

		String rightobjecttocreateclass = StringFormatter.formatForJavaClass(rightobjecttocreate.getName());

		String originleftobjectclass = StringFormatter.formatForJavaClass(originleftobject.getName());
		String originleftobjectvariable = StringFormatter.formatForAttribute(originleftobject.getName());

		String linkobjectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkobjectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		sg.wl("import org.openlowcode.server.action.SecurityInDataMethod;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import " + module.getPath() + ".data." + linkobjectclass + ";");
		sg.wl("import " + rightobjecttocreate.getOwnermodule().getPath() + ".data." + rightobjecttocreateclass + ";");
		sg.wl("import " + originleftobject.getOwnermodule().getPath() + ".data." + originleftobjectclass + ";");

		sg.wl("import java.util.Date;");
		sg.wl("");
		sg.wl("public class AtgCreatelinkandrightobjectfor" + linkobjectvariable + "Action extends");
		sg.wl("		AbsCreatelinkandrightobjectfor" + linkobjectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgCreatelinkandrightobjectfor" + linkobjectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		if (extraattributesdeclaration.length() > 0)
			extraattributesdeclaration.append(',');
		sg.wl("	public DataObjectId<" + originleftobjectclass + "> executeActionLogic( "
				+ extraattributesdeclaration.toString() + " " + rightobjecttocreateclass + " object,DataObjectId<"
				+ originleftobjectclass + "> leftobjectid,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");

		for (int i = 0; i < rightobjecttocreate.propertylist.getSize(); i++) {
			Property<?> thisproperty = rightobjecttocreate.propertylist.get(i);

			String[] methodsforcreation = thisproperty.getPropertyInitMethod();
			if (!thisproperty.isDataInputHiddenForCreation())
				if (methodsforcreation != null)
					for (int j = 0; j < methodsforcreation.length; j++) {
						sg.wl("		object" + methodsforcreation[j]);
					}
		}
		boolean needleftobjectdata = false;

		for (int i = 0; i < linkobjectproperty.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<?> thisbusinessrule = linkobjectproperty.getBusinessRule(i);
			if (thisbusinessrule instanceof ConstraintOnLinkObjectSameChoiceFieldValue)
				needleftobjectdata = true;
		}

		if (needleftobjectdata) {
			sg.wl("		" + originleftobjectclass + " leftobject = " + originleftobjectclass
					+ ".readone(leftobjectid);");
		}
		// process similar fields
		for (int i = 0; i < linkobjectproperty.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<?> thisbusinessrule = linkobjectproperty.getBusinessRule(i);
			if (thisbusinessrule instanceof ConstraintOnLinkObjectSameChoiceFieldValue) {
				ConstraintOnLinkObjectSameChoiceFieldValue samechoicefieldconstraint = (ConstraintOnLinkObjectSameChoiceFieldValue) thisbusinessrule;
				sg.wl("		object.set"
						+ StringFormatter.formatForJavaClass(samechoicefieldconstraint.getRightchoicefield().getName())
						+ "(leftobject.get"
						+ StringFormatter.formatForJavaClass(samechoicefieldconstraint.getLeftchoicefield().getName())
						+ "());");
			}
		}

		sg.wl("		object.insert(this,SecurityInDataMethod.FAIL_IF_NOT_AUTHORIZED);");

		sg.wl("		" + linkobjectclass + " link = new " + linkobjectclass + "();");
		sg.wl("		link.setleftobject(leftobjectid);");
		sg.wl("		link.setrightobject(object.getId());");
		sg.wl("		link.insert();");

		sg.wl("		return leftobjectid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + originleftobjectclass + "> leftobjectid)");
		sg.wl("			 {");

		sg.wl("		return AtgShow" + originleftobjectvariable + "Action.get().executeAndShowPage(leftobjectid);");

		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the code for the create auto-link action
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateCreateAutolinkActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		AutolinkObject<?> autolinkobject = (AutolinkObject<?>) dataobject.getPropertyByName("AUTOLINKOBJECT");
		String linkparentobjectclassname = StringFormatter
				.formatForJavaClass(autolinkobject.getObjectforlink().getName());
		String linkparentattributename = StringFormatter
				.formatForAttribute(autolinkobject.getObjectforlink().getName());

		String linkclassname = StringFormatter.formatForJavaClass(dataobject.getName());
		String linkattributename = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + dataobject.getOwnermodule().getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + dataobject.getOwnermodule().getPath() + ".action.generated.AbsCreate" + linkattributename
				+ "Action;");
		sg.wl("import " + dataobject.getOwnermodule().getPath() + ".action.generated.AtgShow" + linkparentattributename
				+ "Action;");
		sg.wl("import " + dataobject.getOwnermodule().getPath() + ".data." + linkparentobjectclassname + ";");
		sg.wl("import " + dataobject.getOwnermodule().getPath() + ".data." + linkclassname + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgCreate" + linkattributename + "Action extends AbsCreate" + linkattributename
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgCreate" + linkattributename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + linkparentobjectclassname + "> executeActionLogic(DataObjectId<"
				+ linkparentobjectclassname + ">[] left" + linkattributename + "id, " + linkclassname + " "
				+ linkattributename + ",");
		sg.wl("			DataObjectId<" + linkparentobjectclassname + ">[] right" + linkattributename
				+ "id, DataObjectId<" + linkparentobjectclassname + "> " + linkattributename + "idobjecttoshow,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		ArrayList<" + linkclassname + "> " + linkattributename + "toinsert = new ArrayList<" + linkclassname
				+ ">();");
		sg.wl("		for (int i=0;i<left" + linkattributename + "id.length;i++) for (int j=0;j<right" + linkattributename
				+ "id.length;j++) {");
		sg.wl("			" + linkclassname + " " + linkattributename + "copy = " + linkattributename + ".deepcopy();");
		sg.wl("			" + linkattributename + "copy.setleftobject(left" + linkattributename + "id[i]);");
		sg.wl("			" + linkattributename + "copy.setrightobject(right" + linkattributename + "id[j]);");
		sg.wl("			" + linkattributename + "toinsert.add(" + linkattributename + "copy);			");
		sg.wl("		}");
		sg.wl("		" + linkclassname + ".insert(" + linkattributename + "toinsert.toArray(new " + linkclassname
				+ "[0]));");
		sg.wl("		return " + linkattributename + "idobjecttoshow;");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + linkparentobjectclassname + "> " + linkattributename
				+ "idobjecttoshow_thru)  {");
		sg.wl("		return AtgShow" + linkparentattributename + "Action.get().executeAndShowPage(" + linkattributename
				+ "idobjecttoshow_thru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for force version as latest
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateForceAsLatestVersionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {

		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgForceversionaslastfor" + objectvariable + "Action extends AbsForceversionaslastfor"
				+ objectvariable + "Action {");
		sg.wl("	");
		sg.wl("	public AtgForceversionaslastfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		sg.wl("		" + objectvariable + ".forceaslatestversion();");
		sg.wl("		return new ActionOutputData(id);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return AtgShow" + objectvariable + "Action.get().executeAndShowPage(logicoutput.getId_thru());");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");
		sg.close();
	}

	/**
	 * generates new version action to file
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */

	public static void generateNewVersionActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("");
		sg.wl("public class AtgNewversionfor" + objectvariable + "Action extends AbsNewversionfor" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgNewversionfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> id,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " oldversion = " + objectclass + ".readone(id);");
		sg.wl("		" + objectclass + " newversion = oldversion.revise(null);");
		sg.wl("		return new ActionOutputData(newversion.getId());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return AtgShow" + objectvariable
				+ "Action.get().executeAndShowPage(logicoutput.getNewersionid());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates renumber action to file
	 * 
	 * @param dataobject data object definition
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens during the generation
	 */
	public static void generateRenumberActionToFile(DataObjectDefinition dataobject, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class AtgRenumber" + objectvariable + "Action extends AbsRenumber" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgRenumber" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass
				+ "> id, String newnumber,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		if (dataobject.IsIterated())
			sg.wl("		" + objectvariable + ".setupdatenote(\"changing number from \"+" + objectvariable
					+ ".getNr()+\" to \"+newnumber);");
		sg.wl("		" + objectvariable + ".setobjectnumber(newnumber);");
		sg.wl("		return " + objectvariable + ".getId();");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> idthru)  {");
		sg.wl("		AtgShow" + objectvariable + "Action showaction = new AtgShow" + objectvariable
				+ "Action(this.getParent());");
		sg.wl("		return showaction.executeAndShowPage(idthru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

}
