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

import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Schedule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A utility class generating action for workflows and schedule
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionWorkflowAndSchedule {
	/**
	 * generate the reschedule and show planning action
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateRescheduleandShowPlanningActionToFile(String name, SourceGenerator sg, Module module)
			throws IOException {

		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgShowplanningfor" + objectvariable + "Page;");
		sg.wl("");
		sg.wl("public class AtgRescheduleandshowplanningfor" + objectvariable
				+ "Action extends AbsRescheduleandshowplanningfor" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgRescheduleandshowplanningfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic( DataObjectId<" + objectclass
				+ "> rescheduleid,");
		sg.wl("			Date starttime, Date endtime, Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		AtgReschedule" + objectvariable
				+ "Action.get().executeActionLogic(rescheduleid, starttime, endtime,null);");

		sg.wl("		return rescheduleid;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> rescheduleid_thru)  {");
		sg.wl("		return AbsPrepareshowplanningfor" + objectvariable
				+ "Action.get().executeAndShowPage(rescheduleid_thru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate show planning action
	 * 
	 * @param name     java name of the data object
	 * @param schedule schedule property
	 * @param sg       source generator
	 * @param module   parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateShowPlanningActionToFile(
			String name,
			Schedule schedule,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		DataObjectDefinition dependeny = schedule.getDependencyObject();
		String linkclass = StringFormatter.formatForJavaClass(dependeny.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.HashMap;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data.Abs" + objectclass + "ScheduleHelper;");

		sg.wl("import " + module.getPath() + ".data." + linkclass + ";");

		sg.wl("import " + module.getPath() + ".page.generated.AtgShowplanningfor" + objectvariable + "Page;");

		sg.wl("");
		sg.wl("public class AtgPrepareshowplanningfor" + objectvariable + "Action extends AbsPrepareshowplanningfor"
				+ objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgPrepareshowplanningfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");

		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new AtgShowplanningfor" + objectvariable
				+ "Page(logicoutput.getAlltasks(),logicoutput.getAlldependencies(),logicoutput.getScheduledaystart(),logicoutput.getScheduledayend());");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass
				+ "> id, Function<TableAlias, QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		" + objectclass + " roottask  = " + objectclass + ".readone(id);");
		sg.wl("		" + objectclass + "[] alltasks = Abs" + objectclass
				+ "ScheduleHelper.get().getFullPlanning(roottask);");
		sg.wl("		HashMap<String," + linkclass + "> dependencies = new HashMap<String," + linkclass + ">();");
		sg.wl("		for (int i=0;i<alltasks.length;i++) {");
		sg.wl("			" + linkclass + "[] alldependencies = " + linkclass
				+ ".getalllinksfromleftid(alltasks[i].getId(),null);");
		sg.wl("			for (int j=0;j<alldependencies.length;j++) {");
		sg.wl("				" + linkclass + " thisdep = alldependencies[j];");
		sg.wl("				dependencies.put(thisdep.getId().getId(),thisdep);");
		sg.wl("			}");
		sg.wl("		}");
		sg.wl("		" + linkclass + "[] alldependencies = dependencies.values().toArray(new " + linkclass + "[0]);");
		sg.wl("	");
		sg.wl("		return new ActionOutputData(alltasks,alldependencies,alltasks[0].getcalendarstarthour(),alltasks[0].getcalendarendhour());");
		sg.wl("	}");
		sg.wl("}");
		sg.close();
	}

	/**
	 * generate the workflow cockpit action
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateSimpleWorkflowCockpitActionToFile(String name, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + objectclass + "workflowcockpitAction;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "Definition;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "SimpleTaskWorkflowHelper;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "wfreport;");
		sg.wl("import " + module.getPath() + ".page.generated.Atg" + objectclass + "workflowcockpitPage;");
		sg.wl("import org.openlowcode.module.system.data.Appuser;");
		sg.wl("import org.openlowcode.module.system.data.Authority;");
		sg.wl("import org.openlowcode.module.system.data.Task;");
		sg.wl("import org.openlowcode.module.system.data.Taskuser;");
		sg.wl("import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.TasklifecycleChoiceDefinition;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.TwoDataObjects;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.LifecycleQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("");
		sg.wl("public class Atg" + objectclass + "workflowcockpitAction extends Abs" + objectclass
				+ "workflowcockpitAction {");
		sg.wl("");
		sg.wl("	public Atg" + objectclass + "workflowcockpitAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter)");
		sg.wl("			 {");
		sg.wl("		// Get all " + objectvariable + "s");
		sg.wl("		" + objectclass + "[] open" + objectvariable + "s = " + objectclass
				+ ".getallactive(new QueryFilter(");
		sg.wl("			LifecycleQueryHelper.get().getAllNonFinalStatesQueryCondition(\"U0\"," + objectclass
				+ ".getDefinition()),null));");
		sg.wl("		ArrayList<" + objectclass + "wfreport> reportlist = new ArrayList<" + objectclass + "wfreport>();");
		sg.wl("		for (int i=0;i<open" + objectvariable + "s.length;i++) {");
		sg.wl("			" + objectclass + " this" + objectvariable + " = open" + objectvariable + "s[i];");
		sg.wl("			// create the report and put " + objectvariable + " on it");
		sg.wl("			" + objectclass + "wfreport report = new  " + objectclass + "wfreport();");
		sg.wl("			reportlist.add(report);");
		sg.wl("			report.setNumber(this" + objectvariable + ".getNr());");
		sg.wl("			report.setName(this" + objectvariable + ".getObjectname());");
		sg.wl("			report.setStatus(this" + objectvariable + ".getstateforchange());");
		sg.wl("			report.setCreateddate(this" + objectvariable + ".getCreatetime());");
		sg.wl("			report.setTargetdate(this" + objectvariable + ".getTargetdate());");
		sg.wl("			report.setparentforparentid(this" + objectvariable + ".getId());");
		sg.wl("			// get authority");
		sg.wl("			DataObjectId<Authority> authorityid = " + objectclass
				+ "SimpleTaskWorkflowHelper.get().getSingleAuthorityMapper().getAuthority(this" + objectvariable
				+ ");");
		sg.wl("			Authority authority = Authority.readone(authorityid);");
		sg.wl("			report.setAuthority(authority.getObjectname()+\" (\"+authority.getNr()+\")\");");
		sg.wl("			// Get all tasks");
		sg.wl("			Task[] allopentasks = Task.getallforgenericidfortaskobject(DataObjectId.generateDataObjectId(this"
				+ objectvariable + ".getId().getId(),this" + objectvariable + ".getId().getObjectId()),");
		sg.wl("					new QueryFilter(LifecycleQueryHelper.get().getAllNonFinalStatesQueryCondition(\"U0\",Task.getDefinition()),null));");
		sg.wl("			// Several opened tasks is not normal for simple workflow");
		sg.wl("			if (allopentasks.length>1) {");
		sg.wl("				report.setAccepted(BooleanChoiceDefinition.get().UNKN);");
		sg.wl("				report.setAssignedsingle(BooleanChoiceDefinition.get().UNKN);");
		sg.wl("				report.setAssignee(\"Error: multiple workflow tasks opened.\");");
		sg.wl("			}");
		sg.wl("			//");
		sg.wl("			if (allopentasks.length==0) {");
		sg.wl("				report.setAccepted(BooleanChoiceDefinition.get().UNKN);");
		sg.wl("				report.setAssignedsingle(BooleanChoiceDefinition.get().UNKN);");
		sg.wl("				report.setAssignee(\"Error: no opened task assigned.\");");
		sg.wl("			}");
		sg.wl("			if (allopentasks.length==1) {");
		sg.wl("				Task task = allopentasks[0];");
		sg.wl("				report.setLastassignment(task.getCreatetime());");
		sg.wl("				TwoDataObjects<Taskuser,Appuser>[] taskusers = Taskuser.getlinksandrightobject(task.getId(),null);");
		sg.wl("				if (taskusers.length==1) {");
		sg.wl("					report.setparentfortask(task.getId());");
		sg.wl("					report.setAssignedsingle(BooleanChoiceDefinition.get().YES);");
		sg.wl("					if (task.getState().equals(TasklifecycleChoiceDefinition.getChoiceInwork().getStorageCode())) {");
		sg.wl("						report.setAccepted(BooleanChoiceDefinition.get().YES);");
		sg.wl("					} else {");
		sg.wl("						report.setAccepted(BooleanChoiceDefinition.get().NO);");
		sg.wl("					}");
		sg.wl("					report.setAssignee(taskusers[0].getObjectTwo().getObjectname()+\" (\"+taskusers[0].getObjectTwo().getNr()+\")\");");
		sg.wl("				} else {");
		sg.wl("					report.setAssignedsingle(BooleanChoiceDefinition.get().NO);");
		sg.wl("					report.setAccepted(BooleanChoiceDefinition.get().NO);");
		sg.wl("				}");
		sg.wl("			}");
		sg.wl("");
		sg.wl("");
		sg.wl("		}");
		sg.wl("		return new ActionOutputData(reportlist.toArray(new " + objectclass + "wfreport[0]));");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new Atg" + objectclass + "workflowcockpitPage(logicoutput.get" + objectclass
				+ "workflow());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate the task reassign to file
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateTaskReassignActionToFile(String name, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + objectclass + "adminreassignAction;");
		sg.wl("import org.openlowcode.module.system.action.ReassigntaskAction;");
		sg.wl("import org.openlowcode.module.system.data.Appuser;");
		sg.wl("import org.openlowcode.module.system.data.Task;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.security.ServerSecurityBuffer;");
		sg.wl("import org.openlowcode.server.security.ServerSession;");
	
		sg.wl("");
		sg.wl("public class Atg" + objectclass + "adminreassignAction extends Abs" + objectclass
				+ "adminreassignAction {");
		sg.wl("");
		sg.wl("	public Atg" + objectclass + "adminreassignAction(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void executeActionLogic(DataObjectId<Task> taskid, String admincomment, DataObjectId<Appuser> newuserid,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		String fulladmincomment = \"admin reassign from\"+OLcServer.getServer().getCurrentUser().getName()+\" (\"+");
		sg.wl("			OLcServer.getServer().getCurrentUser().getNr()+\") \"+admincomment;");
		sg.wl("		if (fulladmincomment.length()>2000) fulladmincomment = fulladmincomment.substring(0,1997)+\"...\";");
		sg.wl("		ReassigntaskAction.get().executeActionLogic(taskid,fulladmincomment, newuserid, null);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage()  {");
		sg.wl("		return Atg" + objectclass + "workflowcockpitAction.get().executeAndShowPage();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate the simple workflow cockpit page to file
	 * 
	 * @param name   java name of the data object
	 * @param label  data object label
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateSimpleWorkflowCockpitPageToFile(
			String name,
			String label,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Atg" + objectclass + "adminreassignAction;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "wfreport;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "wfreportDefinition;");
		sg.wl("import " + module.getPath() + ".page.generated.Abs" + objectclass + "workflowcockpitPage;");
		sg.wl("import org.openlowcode.module.system.action.generated.AtgSearchappuserAction;");
		sg.wl("import org.openlowcode.module.system.data.Appuser;");
		sg.wl("import org.openlowcode.module.system.page.generated.AtgSearchappuserPage;");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
		sg.wl("");
		sg.wl("public class Atg" + objectclass + "workflowcockpitPage extends Abs" + objectclass
				+ "workflowcockpitPage {");
		sg.wl("");
		sg.wl("	public Atg" + objectclass + "workflowcockpitPage(" + objectclass + "wfreport[] " + objectvariable
				+ "workflow)  {");
		sg.wl("		super(" + objectvariable + "workflow);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + objectclass + "wfreport[] " + objectvariable + "workflow)  {");
		sg.wl("		return \"" + label + " workflow cockpit \"+" + objectvariable + "workflow.length+\" task\"+("
				+ objectvariable + "workflow.length>1?\"s\":\"\")+\" opened\";");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("		mainband.addElement(new SPageText(\"" + label + " Workflow Cockpit\",SPageText.TYPE_TITLE,this));");
		sg.wl("		SObjectArray<" + objectclass + "wfreport> reportarray = new SObjectArray<" + objectclass
				+ "wfreport>");
		sg.wl("			(\"REPORTARRAY\",this.get" + objectclass + "workflow()," + objectclass
				+ "wfreport.getDefinition(),this);");
		sg.wl("		mainband.addElement(reportarray);");
		sg.wl("");
		sg.wl("		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
		sg.wl("		AtgShow" + objectvariable + "Action.ActionRef show" + objectvariable + " = AtgShow" + objectvariable
				+ "Action.get().getActionRef();");
		sg.wl("		show" + objectvariable + ".setId(reportarray.getAttributeInput(" + objectclass
				+ "wfreport.getTransientparentforparentididMarker())); ");
		sg.wl("		buttonband.addElement(new SActionButton(\"" + objectclass + " details\", show" + objectvariable
				+ ", this));");
		sg.wl("		reportarray.addDefaultAction(show" + objectvariable + ");");
		sg.wl("		SComponentBand adminreassignband= new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("		SObjectSearcher<Appuser> usersearcher = AtgSearchappuserPage.getsearchpanel(this,\"REASSIGNEE\");");
		sg.wl("		adminreassignband.addElement(new SPageText(\"Choose user to reassign task to\", SPageText.TYPE_NORMAL,this));");
		sg.wl("		adminreassignband.addElement(usersearcher);");
		sg.wl("		Atg" + objectclass + "adminreassignAction.ActionRef adminreassign = Atg" + objectclass
				+ "adminreassignAction.get().getActionRef();");
		sg.wl("		STextField admincomment = new STextField(\"Reassign comment\", \"COMMENT\", \"Comment that will be recorded in the system\",1800,");
		sg.wl("				\"\", true,");
		sg.wl("				this, false, false, false, adminreassign, false);");
		sg.wl("		adminreassignband.addElement(admincomment);");
		sg.wl("		adminreassign.setTaskid(reportarray.getAttributeInput(" + objectclass
				+ "wfreport.getTransientparentfortaskidMarker()));");
		sg.wl("		adminreassign.setNewuserid(usersearcher.getresultarray().getAttributeInput(Appuser.getIdMarker())); ");
		sg.wl("		adminreassign.setAdmincomment(admincomment.getTextInput());");
		sg.wl("		SActionButton launchreassign = new SActionButton(\"Reassign\", adminreassign, this);");
		sg.wl("		adminreassignband.addElement(launchreassign);");
		sg.wl("");
		sg.wl("		SPopupButton adminreassignbutton = new SPopupButton(this, adminreassignband, \"Reassign Task\", \"Reassign Task\", false, show"
				+ objectvariable + ");");
		sg.wl("		buttonband.addElement(adminreassignbutton);");
		sg.wl("		mainband.addElement(buttonband);");
		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generate the show planning page to page
	 * 
	 * @param name     java name of the data object
	 * @param schedule schedule property
	 * @param sg       source generator
	 * @param module   parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateShowPlanningPageToFile(String name, Schedule schedule, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);
		DataObjectDefinition dependeny = schedule.getDependencyObject();
		String linkclass = StringFormatter.formatForJavaClass(dependeny.getName());

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("");
		sg.wl("import org.openlowcode.server.action.SActionRef;");

		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SGanntChart;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgRescheduleandshowplanningfor" + objectvariable
				+ "Action;");

		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + linkclass + ";");

		sg.wl("");
		sg.wl("public class AtgShowplanningfor" + objectvariable + "Page extends AbsShowplanningfor" + objectvariable
				+ "Page {");
		sg.wl("");
		sg.wl("	public AtgShowplanningfor" + objectvariable + "Page(" + objectclass + "[] alltasks," + linkclass
				+ "[] alldependencies,Integer scheduledaystart, Integer scheduledayend)  {");
		sg.wl("		super(alltasks,alldependencies,scheduledaystart,scheduledayend);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + objectclass + "[] alltasks," + linkclass
				+ "[] alldependencies, Integer scheduledaystart, Integer scheduledayend)  {");
		sg.wl("		StringBuffer title = new StringBuffer (\"Show planning for " + objectclass + "\");");
		sg.wl("		return title.toString();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("		mainband.addElement(new SPageText(\"Planning View\",SPageText.TYPE_TITLE,this));");

		sg.wl("		SGanntChart<" + objectclass + "," + linkclass + "> chart = new SGanntChart<" + objectclass + ","
				+ linkclass + ">(\"PLANNING\",this.getScheduledaystart(),this.getScheduledayend(),this.getAlltasks(),");
		sg.wl("				" + objectclass + ".getStarttimeFieldMarker()," + objectclass + ".getEndtimeFieldMarker(),"
				+ objectclass + ".getNrFieldMarker()," + objectclass + ".getObjectnameFieldMarker()," + objectclass
				+ ".getDefinition(),this);");
		sg.wl("		chart.addDependencies(this.getAlldependencies()," + linkclass + ".getLfidFieldMarker()," + linkclass
				+ ".getRgidFieldMarker());");
		if (schedule.getFieldForColor() != null) {
			sg.wl("		chart.setColorField(" + objectclass + ".get"
					+ StringFormatter.formatForJavaClass(schedule.getFieldForColor().getName()) + "FieldMarker());");
		}
		if (schedule.getFieldForDot() != null) {
			sg.wl("		chart.setDotMarkField(" + objectclass + ".get"
					+ StringFormatter.formatForJavaClass(schedule.getFieldForDot().getName()) + "FieldMarker());");
		}
		if (schedule.getMinWidthForDisplay() > 0)
			sg.wl("		chart.setMinWidth(" + schedule.getMinWidthForDisplay() + ");");
		sg.wl("		mainband.addElement(chart);");
		sg.wl("		AtgShow" + objectvariable + "Action.ActionRef show" + objectvariable + " = AtgShow" + objectvariable
				+ "Action.get().getActionRef();");
		sg.wl("		chart.addDefaultAction(show" + objectvariable + ");");
		sg.wl("		show" + objectvariable + ".setId(chart.getSelectedTimeslotIdInput()); ");

		sg.wl("		AtgRescheduleandshowplanningfor" + objectvariable
				+ "Action.ActionRef rescheduleandshowplanning = AtgRescheduleandshowplanningfor" + objectvariable
				+ "Action.get().getActionRef();");
		sg.wl("		chart.addRescheduleAction(rescheduleandshowplanning);");
		sg.wl("		rescheduleandshowplanning.setRescheduleid(chart.getSelectedTimeslotIdInput());  ");
		sg.wl("		rescheduleandshowplanning.setStarttime(chart.getReschedulePopupStartDateInput()); ");
		sg.wl("		rescheduleandshowplanning.setEndtime(chart.getReschedulePopupEndDateInput());  ");

		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();
	}

	/**
	 * Generate the source code of the reschedule action
	 * 
	 * @param name   java name of the data object
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateRescheduleActionToFile(String name, SourceGenerator sg, Module module)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + module.getPath() + ".action.generated.AbsReschedule" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("");
		sg.wl("public class AtgReschedule" + objectvariable + "Action extends AbsReschedule" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgReschedule" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass
				+ "> id, Date starttime, Date endtime,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " " + objectvariable + " = " + objectclass + ".readone(id);");
		sg.wl("		" + objectvariable + ".reschedule(starttime, endtime);");
		sg.wl("		return " + objectvariable + ".getId();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> outputid)  {");
		sg.wl("		return AtgShow" + objectvariable + "Action.get().executeAndShowPage(outputid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();

	}

	/**
	 * Generate the source code of the insert after action (for schedules)
	 * 
	 * @param dataobject DataObjectDefinition the source code is generated for
	 * @param sg         source generator
	 * @param module     parent module
	 * @throws IOException if anything bad happens while writing the source code
	 */
	public static void generateInsertafterActionToFile(
			DataObjectDefinition dataobject,
			SourceGenerator sg,
			Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".page.generated.AtgShow" + objectvariable + "Page;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("");
		sg.wl("public class AtgInsertafter" + objectvariable + "Action extends AbsInsertafter" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgInsertafter" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		String extracreationfields = "";
		if (dataobject.hasNumbered())
			if (!dataobject.isAutoNumbered())
				extracreationfields += ",String successornr";
		if (dataobject.hasNamed())
			extracreationfields += ",String successorname";
		sg.wl("	public DataObjectId<" + objectclass + "> executeActionLogic(DataObjectId<" + objectclass + "> originid"
				+ extracreationfields + ", " + objectclass + " successor, Date successorstartdate,");
		sg.wl("			Date successorenddate, Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " origin = " + objectclass + ".readone(originid);");
		if (dataobject.hasNumbered())
			if (!dataobject.isAutoNumbered())
				sg.wl("		successor.setobjectnumber(successornr);");
		if (dataobject.hasNamed())
			sg.wl("		successor.setobjectname(successorname);");
		for (int i = 0; i < dataobject.getPropertySize(); i++) {
			Property<?> thisproperty = dataobject.getPropertyAt(i);
			if (thisproperty instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) thisproperty;
				sg.wl("		successor.setparentwithoutupdatefor" + linkedtoparent.getInstancename().toLowerCase()
						+ "(origin.getLinkedtoparentfor" + linkedtoparent.getInstancename().toLowerCase() + "id());");
			}
		}
		sg.wl("		successor.reschedule(successorstartdate, successorenddate);");
		sg.wl("		successor.insert();");
		sg.wl("		origin.insertafter(successor);");
		sg.wl("		return origin.getId();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(DataObjectId<" + objectclass + "> originidthru)  {");
		sg.wl("		return AtgShow" + objectvariable + "Action.get().executeAndShowPage(originidthru);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}
}
