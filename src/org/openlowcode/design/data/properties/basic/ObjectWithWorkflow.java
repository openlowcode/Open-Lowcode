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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * This property is common to all types of workflows running on an object on
 * Open Lowcode
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ObjectWithWorkflow
		extends
		Property<ObjectWithWorkflow> {
	private WidgetDisplayPriority workflowtablepriority;

	/**
	 * sets the priority of the widget holding the workflow tasks table (allows to
	 * check the workflow progress)
	 * 
	 * @param workflowtablepriority display priority for the workflow
	 */
	public void setWorkflowTableWidgetPriority(WidgetDisplayPriority workflowtablepriority) {
		this.workflowtablepriority = workflowtablepriority;
	}

	/**
	 * creates an object with workflow with the given name
	 * 
	 * @param name name of the workflow. Should be unique in the data object and a
	 *             valid java field name
	 */
	public ObjectWithWorkflow(String name) {
		super(name);

	}

	/**
	 * @return the object workflow widget
	 */
	public Widget getObjectWorkflowTable() {
		return new ObjectWorkflowTable(this);
	}

	/**
	 * this class is the widget holding all the tasks of the workflow
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class ObjectWorkflowTable
			extends
			Widget {

		private ObjectWithWorkflow parentworkflow;

		/**
		 * creates an object workflow table widget for the given ObjectWithWorkflow
		 * property
		 * 
		 * @param parentworkflow parent workflow
		 */
		ObjectWorkflowTable(ObjectWithWorkflow parentworkflow) {
			super("OBJECTWORKFLOWTABLE");
			this.parentworkflow = parentworkflow;
		}

		@Override
		public String[] getImportStatements() {
			ArrayList<String> importstatements = new ArrayList<String>();
			importstatements.add("import org.openlowcode.module.system.action.ShowactivetaskAction;");
			importstatements.add("import org.openlowcode.module.system.action.generated.AtgShowworkflowAction;");
			importstatements.add("import org.openlowcode.module.system.data.Task;");
			importstatements.add("import org.openlowcode.module.system.data.TaskDefinition;");
			importstatements.add("import org.openlowcode.module.system.data.Workflow;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectArrayField;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectArray;");
			return importstatements.toArray(new String[0]);
		}

		@Override
		public void generateWidgetCode(SourceGenerator sg, Module module, String locationname,DataObjectDefinition companion) throws IOException {
			sg.wl("		// ----------------------------------------------------------------------------");
			sg.wl("		// Display all tasks of object workflows");
			sg.wl("		// ----------------------------------------------------------------------------");
			sg.wl("		");
			sg.wl("		" + locationname
					+ ".addElement(new SPageText(\"Workflow History\",SPageText.TYPE_TITLE,this));");
			sg.wl("		SObjectArray<Task> alltasks = new SObjectArray<Task>(\"WORKFLOWS\",");
			sg.wl("				this.getAlltasks(),");
			sg.wl("				Task.getDefinition(),");
			sg.wl("				this);");
			sg.wl("		" + locationname + ".addElement(alltasks);");
			sg.wl("		alltasks.setMinFieldPriority(700);");
			sg.wl("		alltasks.hideAttribute(Task.getSubjectFieldMarker());");
			sg.wl("		alltasks.hideAttribute(Task.getDescriptionFieldMarker());");
			sg.wl("		AtgShowtaskAction.ActionRef showtaskref = AtgShowtaskAction.get().getActionRef();");
			sg.wl("		showtaskref.setId(alltasks.getAttributeInput(Task.getIdMarker())); ");
			sg.wl("		alltasks.addDefaultAction(showtaskref);");

		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return parentworkflow.workflowtablepriority;
		}

	}

}
