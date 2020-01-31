/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * a switch condition performing the switch based on a choice field on the
 * object the workflow is running on
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SwitchConditionChoiceField
		extends
		SwitchCondition {
	private DataObjectDefinition object;
	private ChoiceField field;
	private WorkflowStep defaultnextstep;
	private HashMap<ChoiceValue, WorkflowStep> specificnextsteps;

	/**
	 * creates a switch condition for choice field
	 * 
	 * @param object          data object
	 * @param field           field
	 * @param defaultnextstep default next step of choice field values that do not
	 *                        have a specific mapping
	 */
	public SwitchConditionChoiceField(DataObjectDefinition object, ChoiceField field, WorkflowStep defaultnextstep) {
		this.object = object;
		this.field = field;
		this.defaultnextstep = defaultnextstep;
		specificnextsteps = new HashMap<ChoiceValue, WorkflowStep>();
	}

	/**
	 * @return data object the workflow is running on
	 */
	public DataObjectDefinition getObject() {
		return object;
	}

	/**
	 * @return choice field used for criteria
	 */
	public ChoiceField getField() {
		return field;
	}

	/**
	 * @return the default next step for choice values that do not have a specific
	 *         mapping
	 */
	public WorkflowStep getDefaultnextstep() {
		return defaultnextstep;
	}

	/***
	 * adds a specific mapping
	 * 
	 * @param value            choice value of the choice field
	 * @param specificnextstep specific next workflow step to choose
	 */
	public void addSpecificNextStep(ChoiceValue value, WorkflowStep specificnextstep) {
		specificnextsteps.put(value, specificnextstep);
	}

	@Override
	public void writeimport(SourceGenerator sg, Module module) throws IOException {
		ChoiceCategory choice = field.getChoice();
		sg.wl("import " + choice.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(choice.getName()) + "ChoiceDefinition;");
	}

	@Override
	public void writedeclaration(
			SourceGenerator sg,
			Module module,
			String parentclass,
			String lifecycleclass,
			String taskvariable) throws IOException {
		ChoiceCategory choicecategory = field.getChoice();
		String choiceclass = StringFormatter.formatForJavaClass(choicecategory.getName());
		sg.wl("		// ----------------- switch on field " + field.getName() + " ----------------------");
		sg.wl("		ObjectElementSwitchComplexWorkflowStep<" + parentclass + "," + lifecycleclass
				+ "ChoiceDefinition,ChoiceValue<" + choiceclass + "ChoiceDefinition>> " + taskvariable);
		sg.wl("		= new ObjectElementSwitchComplexWorkflowStep<" + parentclass + "," + lifecycleclass
				+ "ChoiceDefinition,ChoiceValue<" + choiceclass + "ChoiceDefinition>>");
		sg.wl("			(a -> a.get" + StringFormatter.formatForJavaClass(field.getName()) + "());		");
		sg.wl("");

	}

	@Override
	public void writeNextSteps(SourceGenerator sg, Module module, String switchtaskcode) throws IOException {

		sg.wl("		" + switchtaskcode + ".setDefaultNextStep(" + defaultnextstep.gettaskid().toLowerCase() + ");");
		Iterator<Entry<ChoiceValue, WorkflowStep>> conditioniterator = specificnextsteps.entrySet().iterator();
		while (conditioniterator.hasNext()) {
			ChoiceCategory choicecategory = field.getChoice();
			String choiceclass = StringFormatter.formatForJavaClass(choicecategory.getName());
			Entry<ChoiceValue, WorkflowStep> thisentry = conditioniterator.next();
			ChoiceValue choicevalue = thisentry.getKey();
			WorkflowStep nextstep = thisentry.getValue();
			sg.wl("		" + switchtaskcode + ".addSpecificStep(" + choiceclass + "ChoiceDefinition.get()."
					+ choicevalue.getName().toUpperCase() + "," + nextstep.gettaskid().toLowerCase() + ");");

		}
	}
}
