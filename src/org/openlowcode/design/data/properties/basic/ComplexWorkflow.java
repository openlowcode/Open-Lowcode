/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.argument.BooleanArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.properties.workflow.ComplexWorkflowClose;
import org.openlowcode.design.data.properties.workflow.ComplexWorkflowGround;
import org.openlowcode.design.data.properties.workflow.TaskUserSelector;
import org.openlowcode.design.data.properties.workflow.UserTask;
import org.openlowcode.design.data.properties.workflow.UserTaskChoice;
import org.openlowcode.design.data.properties.workflow.WorkflowStep;
import org.openlowcode.design.data.properties.workflow.WorkflowTriggerCondition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;


/**
 * A complex workflow with an ad-hoc multi-step workflow with conditions and
 * routings
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ComplexWorkflow
		extends
		ObjectWithWorkflow {

	public static final String EMAIL_NONE = null;
	public static final String EMAIL_NOW = "NOW";
	public static final String EMAIL_DELAY15MIN = "D15M";
	public static final String EMAIL_DELAY2H = "D2H";
	public static final String EMAIL_DAILY = "DAY";
	public static final String EMAIL_WEEKLY = "WEEKLY";

	private ComplexWorkflowClose workflowclose;
	private ComplexWorkflowGround workflowground;
	private WorkflowStep firststep;

	private Lifecycle parentlifecycle;
	private HashMap<String, UserTask> allusertasks;
	private ArrayList<WorkflowStep> workflowstepsnottask;
	private WorkflowTriggerCondition automaticworkflowtrigger;
	private String emailtype;

	/**
	 * @return get the parent lifecycle property
	 */
	public Lifecycle getParentLifecycle() {
		return this.parentlifecycle;
	}

	/**
	 * registers a user task in the catalog of tasks of the workflows. Checks if the
	 * code is unique
	 * 
	 * @param task a task to register in the workflow
	 */
	public void registerUserTask(UserTask task) {
		String code = task.getCode();
		if (allusertasks.containsKey(code))
			throw new RuntimeException("Duplicate task code '" + code + "'");
		allusertasks.put(code, task);
	}

	/**
	 * registers a step that is not a user task
	 * 
	 * @param step step to register
	 */
	public void registerNonTaskWorkflowStep(WorkflowStep step) {
		workflowstepsnottask.add(step);
	}

	/**
	 * creates a complex workflow with the given trigger and e-mail type
	 * 
	 * @param automaticworkflowtrigger type of trigger (at the start of object,
	 *                                 reaching of a lifecycle...)
	 * @param emailtype                type of e-mail (as defined in a static String
	 *                                 in this class)
	 */
	public ComplexWorkflow(WorkflowTriggerCondition automaticworkflowtrigger, String emailtype) {
		super("COMPLEXWORKFLOW");

		boolean emailtypevalue = false;
		if (emailtype == null)
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_NOW))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DELAY15MIN))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DELAY2H))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DAILY))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_WEEKLY))
			emailtypevalue = true;

		if (!emailtypevalue)
			throw new RuntimeException("email type is not valid, please refer to the class static string " + emailtype);
		this.emailtype = emailtype;

		this.automaticworkflowtrigger = automaticworkflowtrigger;
		allusertasks = new HashMap<String, UserTask>();
		workflowstepsnottask = new ArrayList<WorkflowStep>();
		this.workflowclose = new ComplexWorkflowClose(this);
		workflowstepsnottask.add(this.workflowclose);
		this.workflowground = new ComplexWorkflowGround(this);
		workflowstepsnottask.add(this.workflowground);
	}

	/**
	 * defines the first step of the workflow
	 * 
	 * @param firststep first step of the workflow (should already be registered
	 *                  with the workflow)
	 */
	public void setFirstStep(WorkflowStep firststep) {
		this.firststep = firststep;
		if (firststep.getParent() != this)
			throw new RuntimeException("Worflow step should belong to this workflow (" + this.getParent().getName()
					+ ") and not workflow (" + firststep.getParent().getName() + ") for object "
					+ this.parent.getName());
	}

	/**
	 * @return get the workflow first step
	 */
	public WorkflowStep getFirstStep() {
		return this.firststep;
	}

	/**
	 * @return the workflow close step (can be used several times in the workflow)
	 */
	public ComplexWorkflowClose getWorkflowClose() {
		return this.workflowclose;
	}

	/**
	 * @return the workflow ground step (can be used several times in the workflow)
	 */
	public ComplexWorkflowGround getWorkflowGround() {
		return this.workflowground;
	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[0];
		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public void setFinalSettings() {

	}

	@Override
	public String getJavaType() {

		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import gallium.module.system.data.Task;");
		sg.wl("import gallium.module.system.data.Taskchoice;");
		sg.wl("import gallium.module.system.data.Appuser;");

	}

	@Override
	public void controlAfterParentDefinition() {

		Property<?> lifecycle = parent.getPropertyByName("LIFECYCLE");
		if (lifecycle == null)
			throw new RuntimeException( "Parent object " + parent.getName() + " has no lifecycle");
		parentlifecycle = (Lifecycle) lifecycle;
		MethodAdditionalProcessing objectcreationprocessing = new MethodAdditionalProcessing(false,
				parentlifecycle.getDependentPropertyUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(objectcreationprocessing);
		MethodAdditionalProcessing statechangepostprocessing = new MethodAdditionalProcessing(false,
				parentlifecycle.getDataAccessMethod("CHANGESTATE"));
		this.addMethodAdditionalProcessing(statechangepostprocessing);
		DataAccessMethod processtask = new DataAccessMethod("PROCESSTASK", null, false);
		processtask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		processtask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		processtask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		processtask.addInputArgument(new MethodArgument("TASKCHOICE",
				new ObjectIdArgument("TASKCHOICEID", SystemModule.getSystemModule().getTaskChoice())));
		processtask.addInputArgument(new MethodArgument("COMMENT", new StringArgument("COMMENT", 2000)));

		this.addDataAccessMethod(processtask);
		DataAccessMethod accepttask = new DataAccessMethod("ACCEPTTASK", null, false);
		accepttask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		accepttask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		accepttask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		this.addDataAccessMethod(accepttask);
		DataAccessMethod rejecttask = new DataAccessMethod("REJECTTASK", null, false);
		rejecttask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		rejecttask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		rejecttask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		this.addDataAccessMethod(rejecttask);

		DataAccessMethod savetaskcomment = new DataAccessMethod("SAVETASKCOMMENT", null, false);
		savetaskcomment.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		savetaskcomment.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		savetaskcomment.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		savetaskcomment.addInputArgument(new MethodArgument("COMMENT", new StringArgument("COMMENT", 2000)));
		this.addDataAccessMethod(savetaskcomment);

		DataAccessMethod canaccepttask = new DataAccessMethod("CANACCEPTTASK", new BooleanArgument("RESULT"), false);
		canaccepttask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		canaccepttask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		canaccepttask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		this.addDataAccessMethod(canaccepttask);

		DataAccessMethod reassigntask = new DataAccessMethod("REASSIGNTASK", null, false);
		reassigntask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		reassigntask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		reassigntask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));
		reassigntask.addInputArgument(new MethodArgument("NEWUSERID",
				new ObjectIdArgument("NEWUSERID", SystemModule.getSystemModule().getAppuser())));
		reassigntask.addInputArgument(new MethodArgument("COMMENT", new StringArgument("COMMENT", 2000)));
		this.addDataAccessMethod(reassigntask);

		DataAccessMethod canrejecttask = new DataAccessMethod("CANREJECTTASK", new BooleanArgument("RESULT"), false);
		canrejecttask.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		canrejecttask.addInputArgument(
				new MethodArgument("TASKID", new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask())));
		canrejecttask.addInputArgument(new MethodArgument("USERID",
				new ObjectIdArgument("USERID", SystemModule.getSystemModule().getAppuser())));

		this.addDataAccessMethod(canrejecttask);
		this.addChoiceCategoryHelper("LIFECYCLEHELPER", parentlifecycle.getLifecycleHelper());
	}

	@Override
	public String getPropertyHelperName() {
		return StringFormatter.formatForJavaClass(this.getParent().getName()) + "ComplexWorkflowHelper";
	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {
		String lifecycleclass = StringFormatter.formatForJavaClass(this.parentlifecycle.getLifecycleHelper().getName());
		String parentobjectclass = StringFormatter.formatForJavaClass(this.getParent().getName());
		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("");
		sg.wl("import gallium.module.system.data.Authority;");
		sg.wl("import gallium.server.data.ChoiceValue;");
		for (int i = 0; i < this.workflowstepsnottask.size(); i++) {
			WorkflowStep step = this.workflowstepsnottask.get(i);
			step.writeimport(sg, module);
		}
		sg.wl("import gallium.server.data.workflowhelper.ChangeStateWorkflowStep;");
		sg.wl("import gallium.server.data.workflowhelper.ComplexWorkflowClose;");
		sg.wl("import gallium.server.data.workflowhelper.ComplexWorkflowGround;");
		sg.wl("import gallium.server.data.workflowhelper.ObjectElementSwitchComplexWorkflowStep;");
		sg.wl("import gallium.server.data.workflowhelper.ComplexWorkflowHelper;");
		sg.wl("import gallium.server.data.workflowhelper.ObjectParentUserSelectionForTask;");
		sg.wl("import gallium.server.data.workflowhelper.SimpleAuthorityUserSelectionForTask;");
		sg.wl("import gallium.server.data.workflowhelper.TaskChoiceTemplate;");
		sg.wl("import gallium.server.data.workflowhelper.WorkflowTaskStep;");
		sg.wl("import gallium.tools.trace.GalliumException;");
		sg.wl("import " + module.getPath() + ".data.choice." + lifecycleclass + "ChoiceDefinition;");
		sg.wl("");
		sg.wl("");
		sg.wl("public class " + parentobjectclass + "ComplexWorkflowHelper extends ComplexWorkflowHelper<"
				+ parentobjectclass + "," + lifecycleclass + "ChoiceDefinition> {");
		sg.wl("	public " + parentobjectclass + "ComplexWorkflowHelper() throws GalliumException {");
		sg.wl("		super(" + (emailtype != null ? "\"" + emailtype + "\"" : "null") + ");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	private static " + parentobjectclass + "ComplexWorkflowHelper singleton;");
		sg.wl("	public static " + parentobjectclass + "ComplexWorkflowHelper get() throws GalliumException {");
		sg.wl("		if (singleton!=null) return singleton;");
		sg.wl("		" + parentobjectclass + "ComplexWorkflowHelper newinstance = new " + parentobjectclass
				+ "ComplexWorkflowHelper();");
		sg.wl("		singleton = newinstance;");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	@Override");
		sg.wl("	public void initializetaskandsteps() throws GalliumException {");
		sg.wl("");
		Iterator<Entry<String, UserTask>> allusertasksset = allusertasks.entrySet().iterator();
		while (allusertasksset.hasNext()) {
			UserTask thisusertask = allusertasksset.next().getValue();
			String taskcode = thisusertask.getCode().toUpperCase();
			String taskcodelowercase = thisusertask.getCode().toLowerCase();
			sg.wl("		// ------------------ define usertask " + taskcode
					+ " ----------------------------------------");
			TaskUserSelector selector = thisusertask.getUserSelector();
			selector.writeSelectorDeclaration(sg, module, parentobjectclass, lifecycleclass, taskcodelowercase);

			sg.wl("		WorkflowTaskStep<" + parentobjectclass + "," + lifecycleclass + "ChoiceDefinition> "
					+ taskcodelowercase + " =");
			sg.wl("				new WorkflowTaskStep<" + parentobjectclass + "," + lifecycleclass + "ChoiceDefinition>("
					+ taskcodelowercase + "userselection,");
			sg.wl("						new TaskChoiceTemplate[]{");
			for (int i = 0; i < thisusertask.getTaskChoiceNumber(); i++) {
				UserTaskChoice thisusertaskchoice = thisusertask.getChoiceAtIndex(i);
				sg.wl("							" + (i > 0 ? "," : "") + "new TaskChoiceTemplate(\""
						+ thisusertaskchoice.getName().toUpperCase() + "\", \"" + thisusertaskchoice.getLabel()
						+ "\")");
			}
			sg.w("							}");
			sg.wl("				,\"" + taskcode + "\"");
			sg.wl("				,\"" + thisusertask.getTaskmessage() + "\"");
			sg.wl("				,\"" + thisusertask.getTitle() + "\"");
			sg.wl("				," + thisusertask.getDelay() + "");
			// TODO : replace by email spec for task
			sg.wl("				," + thisusertask.isDelayInDays() + ");");
			sg.wl("	");
		}
		for (int i = 0; i < this.workflowstepsnottask.size(); i++) {
			WorkflowStep thisstep = this.workflowstepsnottask.get(i);
			thisstep.setSequence(i);
			thisstep.writedeclaration(sg, module, parentobjectclass, lifecycleclass);
		}
		sg.wl("");

		sg.wl("		// ---------------- SET FIRST STEP OF WORKFLOW");
		sg.wl("		this.addnextstepaftertask(this.START_CODE,null," + this.firststep.gettaskid().toLowerCase() + ");");

		for (int i = 0; i < this.workflowstepsnottask.size(); i++) {
			WorkflowStep thisstep = this.workflowstepsnottask.get(i);
			thisstep.writeNextSteps(sg, module);
		}
		Iterator<Entry<String, UserTask>> allusertasksset2 = allusertasks.entrySet().iterator();
		while (allusertasksset2.hasNext()) {
			UserTask thisusertask = allusertasksset2.next().getValue();
			thisusertask.writeNextSteps(sg, module);
		}

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public boolean isStartAtChangeState(ChoiceValue<" + lifecycleclass
				+ "ChoiceDefinition> state) throws GalliumException {");
		if (this.automaticworkflowtrigger.getNewlifecyclestate() != null) {
			String newlifecycletrigger = StringFormatter
					.formatForJavaClass(automaticworkflowtrigger.getNewlifecyclestate().getName());
			sg.wl("		if (state.equals(" + lifecycleclass + "ChoiceDefinition.getChoice" + newlifecycletrigger
					+ "())) return true;");
		}
		sg.wl("		return false;");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public boolean isStartAtInsert() throws GalliumException {");
		if (this.automaticworkflowtrigger.getNewlifecyclestate() != null) {
			sg.wl("		return false;");
		} else {
			sg.wl("		return true;");

		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("}		");

		sg.close();
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
