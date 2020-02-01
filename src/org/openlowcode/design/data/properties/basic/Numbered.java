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
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * Creates a Number field on the object that will be unique, and typically
 * represents the main business id of the data object. <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Numbered
		extends
		Property<Numbered> {

	private AutonumberingRule autonumberingrule;
	private String[] returnvalues;
	private UniqueIdentified uniqueidentified;

	@Override
	public void addBusinessRule(PropertyBusinessRule<Numbered> rule) {
		super.addBusinessRule(rule);
		if (rule instanceof AutonumberingRule) {
			this.autonumberingrule = (AutonumberingRule) rule;
		}
	}

	public Property<?> getDependentUniqueIdentified() {
		return uniqueidentified;
	}

	private String numberlabel = "Object Number";

	/**
	 * create the 'Numbered' property with the default 'Number' labal
	 */
	public Numbered() {
		this(null);
	}

	/**
	 * create the 'Numbered' property with a specific number label (e.g. 'Social
	 * Security Nr')
	 * 
	 * @param newnumberlabel the specific label for the number on that object
	 */
	public Numbered(String newnumberlabel) {
		super("NUMBERED");
		if (newnumberlabel != null)
			this.numberlabel = newnumberlabel;
		if (newnumberlabel != null)
			this.addFieldOverrides(new FieldOverrideForProperty("NUMBER", newnumberlabel));

	}

	/**
	 * @return the specific number label
	 */
	public String getNumberLabel() {
		return this.numberlabel;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		DataAccessMethod setnumber = new DataAccessMethod("SETOBJECTNUMBER", null, false);
		setnumber.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		setnumber.addInputArgument(new MethodArgument("NR", new StringArgument("NR", 64)));
		this.addDataAccessMethod(setnumber);
		// Fields
		StoredElement number = new StringStoredElement("NR", 64);
		this.addElementasSearchElement(number, numberlabel, "unique business identifier of the object",
				Property.FIELDDIPLSAY_TITLE_MOD, 900, 30, new SearchWidgetDefinition(true, "NR", numberlabel));
		this.addIndex(new Index("NUMBERINDEX", number, false));
		DataAccessMethod getobjectbynumber = new DataAccessMethod("GETOBJECTBYNUMBER",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		getobjectbynumber.addInputArgument(new MethodArgument("NR", new StringArgument("NR", 64)));
		this.addDataAccessMethod(getobjectbynumber);
		DataAccessMethod getobjectbyuniquenumber = new DataAccessMethod("GETUNIQUEOBJECTBYNUMBER",
				new ObjectArgument("OBJECT", parent), true);
		getobjectbyuniquenumber.addInputArgument(new MethodArgument("NR", new StringArgument("NR", 64)));
		this.addDataAccessMethod(getobjectbyuniquenumber);

	}

	@Override
	public String getJavaType() {

		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
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
		return new String[0];
	}

	@Override
	public void setFinalSettings() {
		if (this.autonumberingrule == null) {
			returnvalues = new String[1];
			returnvalues[0] = ".setobjectnumber(number);";
			this.addDataInput(new StringArgument("NUMBER", 64, this.numberlabel));

		} else {
			MethodAdditionalProcessing generatenumber = new MethodAdditionalProcessing(true,
					uniqueidentified.getStoredObject().getDataAccessMethod("INSERT"));
			this.addMethodAdditionalProcessing(generatenumber);
			returnvalues = new String[0];
		}
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
