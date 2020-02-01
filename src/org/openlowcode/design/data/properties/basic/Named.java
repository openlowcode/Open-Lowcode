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
import org.openlowcode.design.data.FieldOverrideForProperty;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * the named property is providing a short description (name) that appears in
 * most widgets of the application pages
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Named
		extends
		Property<Named> {
	private String namelabel = "Name";
	private String[] returnvalues;
	private UniqueIdentified uniqueidentified;
	private AutoNamingRule autonamingrule;

	/**
	 * create a 'Named' property with default label 'Name'
	 */
	public Named() {
		this(null);
	}

	/**
	 * Create a 'Named' property with specific label (e.g. 'Description' )
	 * 
	 * @param newnamelabel the alternative label for the 'Named' field
	 */
	public Named(String newnamelabel) {
		super("NAMED");
		if (newnamelabel != null)
			this.namelabel = newnamelabel;

		if (newnamelabel != null)
			this.addFieldOverrides(new FieldOverrideForProperty("OBJECTNAME", newnamelabel));

	}

	/**
	 * @return the specific name label
	 */
	public String getNameLabel() {
		return this.namelabel;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		Property<?> uniqueidentified = parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		DataAccessMethod setname = new DataAccessMethod("SETOBJECTNAME", null, false);
		setname.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		setname.addInputArgument(new MethodArgument("NAME", new StringArgument("NAME", 64)));
		this.addDataAccessMethod(setname);
		// Fields
		StoredElement name = new StringStoredElement("OBJECTNAME", 64);

		this.addElementasSearchElement(name, namelabel, "Short description of the object",
				Property.FIELDDIPLSAY_TITLE_MOD, 890, 64, new SearchWidgetDefinition(true, "OBJECTNAME", namelabel));

	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	@Override
	public String[] getPropertyInitMethod() {

		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		String[] returnvalues = new String[1];
		returnvalues[0] = ".getObjectname()";
		return returnvalues;
	}

	@Override
	public void setFinalSettings() {
		if (this.autonamingrule == null) {
			returnvalues = new String[1];
			returnvalues[0] = ".setobjectname(objectname);";
			this.addDataInput(new StringArgument("OBJECTNAME", 64, namelabel));
			this.setDataInputForUpdate();

		} else {
			MethodAdditionalProcessing generatenameatcreation = new MethodAdditionalProcessing(true,
					uniqueidentified.getStoredObject().getDataAccessMethod("INSERT"));
			this.addMethodAdditionalProcessing(generatenameatcreation);
			MethodAdditionalProcessing generatenameatupdate = new MethodAdditionalProcessing(true,
					uniqueidentified.getDataAccessMethod("UPDATE"));
			this.addMethodAdditionalProcessing(generatenameatupdate);

			returnvalues = new String[0];
		}
	}

	@Override
	public void addBusinessRule(PropertyBusinessRule<Named> rule) {
		super.addBusinessRule(rule);
		if (rule instanceof AutoNamingRule) {
			this.autonamingrule = (AutoNamingRule) rule;
		}
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		if (this.autonamingrule == null)
			return new String[] { "		deepcopy.named.setobjectname(deepcopy, this.getObjectname());" };
		return null;
	}
}
