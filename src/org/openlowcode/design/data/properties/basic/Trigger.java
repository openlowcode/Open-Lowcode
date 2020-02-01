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
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.properties.trigger.TriggerLaunchCondition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * create a trigger property that will launch specific code on specific events
 * on the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Trigger
		extends
		Property<Trigger> {
	private UniqueIdentified uniqueidentified;
	private TriggerLaunchCondition triggerlaunchcondition;

	/**
	 * @return the unique identified property
	 */
	public UniqueIdentified getUniqueidentified() {
		return uniqueidentified;
	}

	/**
	 * creates a trigger property
	 * 
	 * @param name                   a unique name amongst the triggers on this
	 *                               object, should be a valid java field name
	 * @param triggerlaunchcondition the condition to launch the trigger
	 */
	public Trigger(String name, TriggerLaunchCondition triggerlaunchcondition) {
		super(name, "TRIGGER");
		this.triggerlaunchcondition = triggerlaunchcondition;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.parent.getOwnermodule().addTrigger(this);
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		if (uniqueidentified == null)
			throw new RuntimeException("trigger property needs the object to have property uniqueidentified");
		this.addDependentProperty(uniqueidentified);

		// --------------------------- Method additional processing.
		MethodAdditionalProcessing triggeronupdate = new MethodAdditionalProcessing(false,
				uniqueidentified.getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(triggeronupdate);
		MethodAdditionalProcessing triggerondelete = new MethodAdditionalProcessing(true,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(triggerondelete);
		Property<?> storedobjectproperty = parent.getPropertyByName("STOREDOBJECT");
		MethodAdditionalProcessing triggeroninsert = new MethodAdditionalProcessing(false,
				storedobjectproperty.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(triggeroninsert);
		Property<?> parentlifecycle = parent.getPropertyByName("LIFECYCLE");
		if (parentlifecycle != null) {
			MethodAdditionalProcessing statechangepostprocessing = new MethodAdditionalProcessing(false,
					parentlifecycle.getDataAccessMethod("CHANGESTATE"));
			this.addMethodAdditionalProcessing(statechangepostprocessing);
		}
		this.setExtraAttributes("," + this.triggerlaunchcondition.generateDeclaration() + ",Abs"
				+ StringFormatter.formatForJavaClass(this.getInstancename()) + "Trigger.factory");

	}

	@Override
	public String[] getPropertyInitMethod() {

		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {

		return null;
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
		sg.wl("import " + this.parent.getOwnermodule().getPath() + ".utility.generated.Abs"
				+ StringFormatter.formatForJavaClass(this.getInstancename()) + "Trigger;");
		String[] triggerconditionimport = this.triggerlaunchcondition.generateImportConditions();
		for (int i = 0; i < triggerconditionimport.length; i++)
			sg.wl(triggerconditionimport[i]);
	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {

		String objectclass = StringFormatter.formatForJavaClass(this.parent.getName());
		String triggernameclass = StringFormatter.formatForJavaClass(this.getInstancename());

		sg.wl("package " + this.parent.getOwnermodule().getPath() + ".utility.generated;");
		sg.wl("");
		sg.wl("import " + this.parent.getOwnermodule().getPath() + ".data." + objectclass + ";");
		sg.wl("import " + this.parent.getOwnermodule().getPath() + ".utility." + triggernameclass + "Trigger;");
		sg.wl("import org.openlowcode.server.data.properties.trigger.CustomTriggerExecution;");
		sg.wl("import org.openlowcode.server.data.properties.trigger.CustomTriggerExecutionFactory;");
		sg.wl("");
		sg.wl("/**");
		sg.wl(" * This class needs to be implemented by the user as a concrete class. The framework is ensuring");
		sg.wl(" * there will be one instance per execution, so data can be stored on the concrete class between the");
		sg.wl(" * generateTriggerString and execute method");
		sg.wl(" *");
		sg.wl(" */");

		sg.wl("public abstract class Abs" + triggernameclass + "Trigger extends CustomTriggerExecution<" + objectclass
				+ ">  {");
		sg.wl("	");
		sg.wl("	");
		sg.wl("	public Abs" + triggernameclass + "Trigger() {");
		sg.wl("		super(\"" + this.getName().toUpperCase() + "\");");
		sg.wl("	");
		sg.wl("	}");
		sg.wl("	public static class Factory implements CustomTriggerExecutionFactory<" + objectclass + "> {");
		sg.wl("");
		sg.wl("		@Override");
		sg.wl("		public CustomTriggerExecution<" + objectclass + "> generate() {");
		sg.wl("			return new " + triggernameclass + "Trigger();");
		sg.wl("		}");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("	public static final Factory factory = new Factory();");
		sg.wl("}");
		sg.wl("");
		sg.wl("");
		sg.close();
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
