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

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.properties.control.DataControlElement;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * Data Control property will perform data controls under some conditions. Data
 * control will prevent lifecycle transitions to working state until they are
 * corrected.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataControl
		extends
		Property<DataControl> {
	private ArrayList<DataControlElement> controlelements;
	private Lifecycle lifecycle;

	/**
	 * creates a data control property (only one per data object)
	 */
	public DataControl() {
		super("DATACONTROL");
		this.controlelements = new ArrayList<DataControlElement>();

	}

	@Override
	public void controlAfterParentDefinition() {
		lifecycle = (Lifecycle) parent.getPropertyByName("LIFECYCLE");
		if (lifecycle == null)
			throw new RuntimeException("Data Control is dependent on lifecycle for object " + parent.getName());
		this.addDependentProperty(lifecycle);
		MethodAdditionalProcessing statechangepostprocessing = new MethodAdditionalProcessing(true,
				lifecycle.getDataAccessMethod("CHANGESTATE"));
		this.addMethodAdditionalProcessing(statechangepostprocessing);
		MethodAdditionalProcessing objectcreationprocessing = new MethodAdditionalProcessing(false,
				lifecycle.getDependentPropertyUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(objectcreationprocessing);
		MethodAdditionalProcessing objectupdateprocessing = new MethodAdditionalProcessing(true,
				lifecycle.getDependentPropertyUniqueIdentified().getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(objectupdateprocessing);
		StringStoredElement validationcontrol = new StringStoredElement("SUMMARY", 200);
		this.addElement(validationcontrol);
		DataAccessMethod getvalidationdetail = new DataAccessMethod("GETVALIDATIONDETAIL",
				new StringArgument("DETAIL", 4000), false);
		getvalidationdetail.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getvalidationdetail);
		DataAccessMethod validate = new DataAccessMethod("VALIDATE",
				new ChoiceArgument("STATUS", SystemModule.getSystemModule().getControlLevel()), false);
		validate.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(validate);
	}

	/**
	 * @param element adds a control element to this data control
	 */
	public void addControlElement(DataControlElement element) {
		this.controlelements.add(element);
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
		sg.wl("import org.openlowcode.module.system.data.choice.ControllevelChoiceDefinition;");

	}

	@Override
	public String getPropertyHelperName() {
		return StringFormatter.formatForJavaClass(this.getParent().getName()) + "DataControlHelper";
	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(this.getParent().getName());
		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.properties.constraints.DataControlHelper;");
		sg.wl("import org.openlowcode.server.data.properties.constraints.DataControlRuleFieldFilled;");
		sg.wl("");
		sg.wl("public class " + objectclass + "DataControlHelper extends DataControlHelper<" + objectclass + "> {");
		sg.wl("");
		sg.wl("	public " + objectclass + "DataControlHelper()  {");
		sg.wl("		super();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void addrules()  {");
		for (int i = 0; i < this.controlelements.size(); i++) {
			DataControlElement thiscontrolelement = this.controlelements.get(i);
			thiscontrolelement.writeRule(sg, module, objectclass);
		}
		sg.wl("	}");
		sg.wl("	private static " + objectclass + "DataControlHelper singleton;");
		sg.wl("	public static DataControlHelper get() {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclass + "DataControlHelper helper = new " + objectclass + "DataControlHelper();");
		sg.wl("			singleton = helper;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
