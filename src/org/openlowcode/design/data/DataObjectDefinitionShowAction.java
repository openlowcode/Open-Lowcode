/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * this program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.properties.basic.ComplexWorkflow;
import org.openlowcode.design.data.properties.basic.FileContent;
import org.openlowcode.design.data.properties.basic.HasAutolink;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.LeftForLinkToMaster;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.RightForLink;
import org.openlowcode.design.data.properties.basic.RightForLinkToMaster;
import org.openlowcode.design.data.properties.basic.Schedule;
import org.openlowcode.design.data.properties.basic.SimpleTaskWorkflow;
import org.openlowcode.design.data.properties.basic.TimeSlot;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;


/**
 * A utility class generating the show action to file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionShowAction {
	/**
	 * generates the show action code to the given file
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generateShowActionToFile(DataObjectDefinition mainobject, SourceGenerator sg, Module module)
			throws IOException {
		generateShowActionToFile(mainobject, null, sg, module);
	}

	/**
	 * generates the show action code to the given file
	 * 
	 * @param dataobject data object
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generateShowActionToFile(
			DataObjectDefinition dataobject,
			DataObjectDefinition companionobject,
			SourceGenerator sg,
			Module module) throws IOException {

		// define general class naming
		String actionname = "Show" + dataobject.getName().toLowerCase() + "Action";
		if (companionobject != null)
			actionname = "Show" + companionobject.getName().toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		String companionclass = null;
		if (companionobject!=null) {
			companionclass = StringFormatter.formatForJavaClass(companionobject.getName());
			
		}
		String lifecycleclass = null;
		ChoiceCategory lifecycle = null;
		if (dataobject.hasLifecycle()) {
			lifecycle = ((Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"))).getTransitionChoiceCategory();
			lifecycleclass = StringFormatter.formatForJavaClass(lifecycle.getName());
		}

		boolean isfileobject = false;
		boolean hasworkflow = false;
		boolean isschedule = false;
		// define linked objects through properties that will be considered in the show
		// action
		NamedList<DataObjectDefinition> linkedobjects = new NamedList<DataObjectDefinition>();

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> dataobjectproperty = dataobject.propertylist.get(i);
			ArrayList<DataObjectDefinition> dependentobjects = dataobjectproperty.getExternalObjectDependence();
			if (dependentobjects != null)
				for (int j = 0; j < dependentobjects.size(); j++) {
					linkedobjects.addIfNew(dependentobjects.get(j));
				}

			if (dataobjectproperty instanceof Schedule) {

				isschedule = true;
			}

			if (dataobjectproperty instanceof ImageContent) {

				isfileobject = true;
			}
			if (dataobjectproperty instanceof FileContent) {

				isfileobject = true;
			}
			if ((dataobjectproperty instanceof SimpleTaskWorkflow) || (dataobjectproperty instanceof ComplexWorkflow)) {
				hasworkflow = true;
			}
		}

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		if (companionobject!=null) {
			sg.wl("import org.openlowcode.server.data.TwoDataObjects;");
			sg.wl("import "+companionobject.getOwnermodule().getPath()+".data."+companionclass+";");
		}
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		if (companionobject==null) {
			sg.wl("import " + module.getPath() + ".page.generated.AtgShow" + objectvariable + "Page;");
		} else {
			sg.wl("import "+companionobject.getOwnermodule().getPath()+".page.generated.AtgShow"+companionobject.getName().toLowerCase()+"Page;");
		}
		sg.wl("import java.util.Date;");
		if (dataobject.hasLifecycle()) {
			sg.wl("import org.openlowcode.server.data.properties.LifecycleDefinition;");
			sg.wl("import org.openlowcode.server.data.ChoiceValue;");
			sg.wl("import " + lifecycle.getParentModule().getPath() + ".data.choice." + lifecycleclass
					+ "ChoiceDefinition;");
		}
		if (isfileobject) {
			sg.wl("import org.openlowcode.tools.messages.SFile;");
			sg.wl("import org.openlowcode.module.system.data.Binaryfile;");
		}
		if (hasworkflow) {
			sg.wl("import org.openlowcode.module.system.action.ActivetaskcomplexqueryAction;");
			sg.wl("import org.openlowcode.module.system.data.Task;");
			sg.wl("import org.openlowcode.module.system.data.Workflow;");
			sg.wl("import org.openlowcode.server.data.DataObject;");
		}
		for (int i = 0; i < linkedobjects.getSize(); i++) {
			DataObjectDefinition linkedobject = linkedobjects.get(i);
			String classname = StringFormatter.formatForJavaClass(linkedobject.getName());
			Module currentmodule = linkedobject.getOwnermodule();
			if (!classname.equals(objectclass)) { // only adds if not already main object
				sg.wl("import " + currentmodule.getPath() + ".data." + classname + ";");
				if (linkedobject.isShowActionAutomaticallyGenerated())
					sg.wl("import " + currentmodule.getPath() + ".action.generated.AtgMassupdate"
							+ StringFormatter.formatForAttribute(linkedobject.getName()) + "Action;");

			}

		}

		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		String outputforexecuteaction = "ActionOutputData";
		sg.wl("	public " + outputforexecuteaction + " executeActionLogic(DataObjectId<" + objectclass
				+ "> id,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		");
		if (companionobject == null) {
			sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		} else {
			sg.wl("		TwoDataObjects<"+objectclass+","+companionclass+"> mainandcompanion = "+companionclass+".readtyped(id);");

			
		}
		sg.wl("		ChoiceValue<ApplocaleChoiceDefinition> userlocale = OLcServer.getServer().getCurrentUser().getPreflang();");
		sg.wl("		ChoiceValue<PreferedfileencodingChoiceDefinition> preffileencoding = OLcServer.getServer().getCurrentUser().getPreffileenc();");

		ArrayList<String> extravariableforaction = new ArrayList<String>();
		ArrayList<String> extraargumentfordata = new ArrayList<String>();

		if (hasworkflow) {

			sg.wl("		DataObjectId genericid = DataObjectId.generateDataObjectId(id.getId(),id.getObjectId());");
			sg.wl("		Task[] activetasks = ActivetaskcomplexqueryAction.get().executeActionLogic(genericid,null).getTask();");
			sg.wl("		Task[] alltasks = Task.getallforgenericidfortaskobject(DataObjectId.generateDataObjectId(id.getId(), id.getObjectId()),null);");
			extravariableforaction.add("activetasks");
			extravariableforaction.add("alltasks");
			extraargumentfordata.add("Youractivetasks");
			extraargumentfordata.add("Alltasks");

		}

		if (dataobject.hasLifecycle()) {
			sg.wl("		ChoiceValue<" + lifecycleclass + "ChoiceDefinition> potentialnewstates = " + objectvariable
					+ ".getstateforchange();");
			extravariableforaction.add("potentialnewstates");
			extraargumentfordata.add("Potentialstates");
			String unreleasedwarning = ((Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"))).getUnreleasedWarning();
			if (unreleasedwarning != null) {
				sg.wl("		boolean isunreleased = !" + StringFormatter.formatForJavaClass(lifecycle.getName())
						+ "ChoiceDefinition.get().isChoiceFinal(" + objectvariable + ".getstateforchange());");
				sg.wl("		String unreleasedwarning = \"\";");
				sg.wl("		if (isunreleased) unreleasedwarning = ((LifecycleDefinition)(" + objectclass
						+ ".getDefinition().getProperty(\"LIFECYCLE\"))).getUnreleasedWarningText();");

				extravariableforaction.add("unreleasedwarning");
				extraargumentfordata.add("Unreleasedwarning");

			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> dataobjectproperty = dataobject.propertylist.get(i);
			if (dataobjectproperty instanceof TimeSlot) {
				sg.wl("		Date starttime = " + objectvariable + ".getStarttime();");
				sg.wl("		Date endtime = " + objectvariable + ".getEndtime();");
				extravariableforaction.add("starttime");
				extravariableforaction.add("endtime");
				extraargumentfordata.add("Starttime");
				extraargumentfordata.add("Endtime");

			}
		}

		if (isschedule) {
			if (dataobject.hasNumbered())
				if (!dataobject.isAutoNumbered()) {
					sg.wl("		String insertafternr = (" + objectvariable + ".getNr()+\"<Extra>\");");
					sg.wl("		if (insertafternr.length()>63) insertafternr = insertafternr.substring(0,63);");
					extravariableforaction.add("insertafternr");
					extraargumentfordata.add("Insertafternr");

				}
			if (dataobject.hasNamed()) {
				sg.wl("		String insertaftername = (" + objectvariable + ".getObjectname()+\"<Extra>\");");
				sg.wl("		if (insertaftername.length()>63) insertaftername = insertaftername.substring(0,63);");
				extravariableforaction.add("insertaftername");
				extraargumentfordata.add("Insertaftername");

			}

			sg.wl("		" + objectclass + " blankforinsertafter = new " + objectclass + "();");
			sg.wl("		Date insertafterstart = " + objectvariable + ".getnextstarthour();");
			sg.wl("		Date insertafterend = new Date(insertafterstart.getTime()+60*60*1000);");
			extravariableforaction.add("blankforinsertafter");
			extravariableforaction.add("insertafterstart");
			extravariableforaction.add("insertafterend");

			extraargumentfordata.add("Blankforinsertafter");
			extraargumentfordata.add("Insertafterstart");
			extraargumentfordata.add("Insertafterend");
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> dataobjectproperty = dataobject.propertylist.get(i);
			if (dataobjectproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) dataobjectproperty;
				String imagecontentname = imagecontent.getInstancename().toLowerCase();
				sg.wl("		SFile " + imagecontentname + "tbn = " + objectvariable + ".getthumbnailfor"
						+ imagecontentname + "();");
				sg.wl("		DataObjectId<Binaryfile> " + imagecontentname + "fullimgid = " + objectvariable
						+ ".getImagecontentfor" + imagecontentname + "imgid();");
				extravariableforaction.add(imagecontentname + "tbn");
				extraargumentfordata.add(StringFormatter.formatForJavaClass(imagecontentname) + "tbn");
				extravariableforaction.add(imagecontentname + "fullimgid");
				extraargumentfordata.add(StringFormatter.formatForJavaClass(imagecontentname) + "fullimgid");

			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> dataobjectproperty = dataobject.propertylist.get(i);
			if (dataobjectproperty instanceof FileContent) {

				sg.wl("		Objattachment[] attachments = " + objectvariable + ".getattachments(null);");

				extravariableforaction.add("attachments");
				extraargumentfordata.add("Attachments");

			}
		}
		if (dataobject.hasNumbered()) {
			sg.wl("		String numberforrenumber = " + objectvariable + ".getNr();");
			extravariableforaction.add("numberforrenumber");
			extraargumentfordata.add("Numberforrenumber");
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> dataobjectproperty = dataobject.propertylist.get(i);
			if (dataobjectproperty instanceof LinkedFromChildren) {
				LinkedFromChildren dataobjectlinkedfromchildren = (LinkedFromChildren) dataobjectproperty;
				DataObjectDefinition childobject = dataobjectlinkedfromchildren.getChildObject();
				String dataobjectchildobjectclass = StringFormatter.formatForJavaClass(childobject.getName());
				String dataobjectextravariable = "extradatafor"
						+ StringFormatter.formatForJavaClass(dataobjectlinkedfromchildren.getName());
				sg.wl("		" + dataobjectchildobjectclass + "[] " + dataobjectextravariable + " = "
						+ dataobjectchildobjectclass + ".getallchildrenfor"
						+ StringFormatter.formatForAttribute(
								dataobjectlinkedfromchildren.getOriginObjectProperty().getInstancename())
						+ "(id,null);");
				sg.wl("		AtgMassupdate" + StringFormatter.formatForAttribute(childobject.getName())
						+ "Action.get().freezeUnauthorizedObjects(" + dataobjectextravariable + ");");
				extravariableforaction.add(dataobjectextravariable);
				extraargumentfordata.add(StringFormatter.formatForJavaClass(dataobjectlinkedfromchildren.getName()));

			}
			if (dataobjectproperty instanceof LeftForLink) {
				LeftForLink<?, ?> dataobjectleftforlink = (LeftForLink<?, ?>) dataobjectproperty;
				DataObjectDefinition linkedobject = dataobjectleftforlink.getLinkObjectDefinition();
				String dataobjectlinkobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
				String dataobjectextravariable = "extradatafor"
						+ StringFormatter.formatForJavaClass(dataobjectleftforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectextravariable + " = "
						+ dataobjectlinkobjectclass + ".getalllinksfromleftid(id,null);");
				sg.wl("		AtgMassupdate" + StringFormatter.formatForAttribute(linkedobject.getName())
						+ "Action.get().freezeUnauthorizedObjects(" + dataobjectextravariable + ");");
				extravariableforaction.add(dataobjectextravariable);
				extraargumentfordata.add("Leftforlinkfor" + linkedobject.getName().toLowerCase());
				String dataobjectblankextravariable = "blankforaddfor"
						+ StringFormatter.formatForJavaClass(dataobjectleftforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + " " + dataobjectblankextravariable + " = new "
						+ dataobjectlinkobjectclass + "();");
				extravariableforaction.add(dataobjectblankextravariable);
				extraargumentfordata.add("Leftforlinkfor" + linkedobject.getName().toLowerCase() + "blankforadd");
			}
			if (dataobjectproperty instanceof RightForLink) {
				RightForLink<?, ?> dataobjectrightforlink = (RightForLink<?, ?>) dataobjectproperty;
				DataObjectDefinition linkedobject = dataobjectrightforlink.getLinkObjectDefinition();
				String dataobjectlinkobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
				String dataobjectextravariable = "extradatafor"
						+ StringFormatter.formatForJavaClass(dataobjectrightforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectextravariable + " = "
						+ dataobjectlinkobjectclass + ".getalllinksfromrightid(id,null);");
				extravariableforaction.add(dataobjectextravariable);
				extraargumentfordata.add("Rightforlinkfor" + linkedobject.getName().toLowerCase());
				String dataobjectblankextravariable = "blankforaddfor"
						+ StringFormatter.formatForJavaClass(dataobjectrightforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + " " + dataobjectblankextravariable + " = new "
						+ dataobjectlinkobjectclass + "();");
				extravariableforaction.add(dataobjectblankextravariable);
				extraargumentfordata.add("Rightforlinkfor" + linkedobject.getName().toLowerCase() + "blankforadd");

			}

			if (dataobjectproperty instanceof LeftForLinkToMaster) {
				LeftForLinkToMaster<?, ?> dataobjectleftforlink = (LeftForLinkToMaster<?, ?>) dataobjectproperty;
				DataObjectDefinition linkedobject = dataobjectleftforlink.getLinkObjectDefinition();
				String dataobjectlinkobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
				String dataobjectextravariable = "extradatafor"
						+ StringFormatter.formatForJavaClass(dataobjectleftforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectextravariable + " = "
						+ dataobjectlinkobjectclass + ".getalllinksfromleftid(id,null);");
				sg.wl("		AtgMassupdate" + StringFormatter.formatForAttribute(linkedobject.getName())
						+ "Action.get().freezeUnauthorizedObjects(" + dataobjectextravariable + ");");
				extravariableforaction.add(dataobjectextravariable);
				extraargumentfordata.add("Leftforlinktomasterfor" + linkedobject.getName().toLowerCase());
				String dataobjectblankextravariable = "blankforaddfor"
						+ StringFormatter.formatForJavaClass(dataobjectleftforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + " " + dataobjectblankextravariable + " = new "
						+ dataobjectlinkobjectclass + "();");
				extravariableforaction.add(dataobjectblankextravariable);
				extraargumentfordata
						.add("Leftforlinktomasterfor" + linkedobject.getName().toLowerCase() + "blankforadd");
			}
			if (dataobjectproperty instanceof RightForLinkToMaster) {
				RightForLinkToMaster<?, ?> dataobjectrightforlink = (RightForLinkToMaster<?, ?>) dataobjectproperty;
				DataObjectDefinition linkedobject = dataobjectrightforlink.getLinkObjectDefinition();
				String dataobjectlinkobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
				String dataobjectextravariable = "extradatafor"
						+ StringFormatter.formatForJavaClass(dataobjectrightforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectextravariable + " = "
						+ dataobjectlinkobjectclass + ".getalllinksfromrightmsid(" + objectvariable
						+ ".getMasterid(),null);");
				extravariableforaction.add(dataobjectextravariable);
				extraargumentfordata.add("Rightforlinktomasterfor" + linkedobject.getName().toLowerCase());
				String dataobjectblankextravariable = "blankforaddfor"
						+ StringFormatter.formatForJavaClass(dataobjectrightforlink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + " " + dataobjectblankextravariable + " = new "
						+ dataobjectlinkobjectclass + "();");
				extravariableforaction.add(dataobjectblankextravariable);
				extraargumentfordata
						.add("Rightforlinktomasterfor" + linkedobject.getName().toLowerCase() + "blankforadd");

			}

			if (dataobjectproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) dataobjectproperty;
				DataObjectDefinition linkedobject = hasautolink.getLinkObjectDefinition();
				String dataobjectlinkobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
				String dataobjectlinkobjectvariable = StringFormatter.formatForAttribute(linkedobject.getName());

				// autolink as left link
				if (hasautolink.getLinkObjectProperty().isSymetricLink()) {
					String dataobjectleftextravariable = "lefthasautolinkfor"
							+ StringFormatter.formatForJavaClass(hasautolink.getName());
					sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectleftextravariable + " = "
							+ dataobjectlinkobjectclass + ".getalllinksfromleftid(id,null);");
					extravariableforaction.add(dataobjectleftextravariable);
				} else {
					if (hasautolink.getLinkObjectProperty().isShowLinkTree()) {
						String dataobjectleftextravariable = "lefthasautolinkfor"
								+ StringFormatter.formatForJavaClass(hasautolink.getName());
						sg.wl("		NodeTree<" + dataobjectlinkobjectclass + "> " + dataobjectleftextravariable
								+ " = AtgShowautolinktreefor" + dataobjectlinkobjectvariable
								+ "Action.get().executeActionLogic(id,null).get" + dataobjectlinkobjectclass
								+ "tree();");
						extravariableforaction.add(dataobjectleftextravariable);
					} else {
						String dataobjectleftextravariable = "lefthasautolinkfor"
								+ StringFormatter.formatForJavaClass(hasautolink.getName());
						sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectleftextravariable + " = "
								+ dataobjectlinkobjectclass + ".getalllinksfromleftid(id,null);");
						extravariableforaction.add(dataobjectleftextravariable);

					}
				}
				extraargumentfordata.add("Lefthasautolinkfor" + linkedobject.getName().toLowerCase());
				String dataobjectblankextravariable = "Lefthasautolinkfor"
						+ StringFormatter.formatForJavaClass(hasautolink.getName());
				sg.wl("		" + dataobjectlinkobjectclass + " " + dataobjectblankextravariable + " = new "
						+ dataobjectlinkobjectclass + "();");
				extravariableforaction.add(dataobjectblankextravariable);
				extraargumentfordata.add("Hasautolinkfor" + linkedobject.getName().toLowerCase() + "blankforadd");
				// autolink as right link
				String dataobjectrightextravariable = "extradataforright"
						+ StringFormatter.formatForJavaClass(hasautolink.getName());

				sg.wl("		" + dataobjectlinkobjectclass + "[] " + dataobjectrightextravariable + " = "
						+ dataobjectlinkobjectclass + ".getalllinksfromrightid(id,null);");
				extravariableforaction.add(dataobjectrightextravariable);
				extraargumentfordata.add("Righthasautolinkfor" + linkedobject.getName().toLowerCase());
			}

		}

		sg.wl("		");
		/* if (isextraobject) { */
// ActionOutputData is used only if output has more than one argument
		if (companionobject==null) {
		sg.w("		ActionOutputData outputdata = new ActionOutputData(" + objectvariable
				+ ",userlocale,preffileencoding");
		} else {
			sg.w("		ActionOutputData outputdata = new ActionOutputData(mainandcompanion.getObjectOne(),mainandcompanion.getObjectTwo()"
					+ ",userlocale,preffileencoding");
		}
		for (int i = 0; i < extravariableforaction.size(); i++)
			sg.w("," + extravariableforaction.get(i));

		sg.wl(");");
		sg.wl("		return outputdata;");
		/*
		 * } else { sg.wl("		return "+objectvariable+";"); }
		 */

		sg.wl("	}");
		sg.wl("	");
		/* if (isextraobject) { */
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		");
		sg.wl("		return new AtgShow" + (companionobject!=null?companionobject.getName().toLowerCase():objectvariable) + "Page(logicoutput.get" + objectclass
				+ "(),"+(companionobject!=null?"logicoutput.get"+companionclass+"(),":"")+"logicoutput.getUserlocale(),logicoutput.getPrefencoding()");
		for (int i = 0; i < extraargumentfordata.size(); i++) {
			sg.wl("				,logicoutput.get" + extraargumentfordata.get(i) + "()");
		}
		sg.wl("				);");
		sg.wl("	}");

		sg.wl("	");
		sg.wl("}");

		sg.close();

	}
}
