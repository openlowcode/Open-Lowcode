/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.TwoObjectsArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A Companion is a secondary data object linked to a main data object. It
 * provides extra data for some types of the main data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public class Companion
		extends
		Property<Companion> {

	private DataObjectDefinition maintypedobject;
	private ChoiceValue[] types;

	public Companion(DataObjectDefinition maintypedobject,ChoiceValue[] types) {
		super("COMPANION");
		this.maintypedobject = maintypedobject;
		this.types = types;
		
	}

	@Override
	public void controlAfterParentDefinition() {

		Typed mainobjecttypedproperty = (Typed) maintypedobject.getPropertyByName("TYPED");
		if (mainobjecttypedproperty==null) throw new RuntimeException("Main object "+maintypedobject.getName()+" should have the Typed property added before this statement.");
		mainobjecttypedproperty.addCompanionObject(this.getParent(), types);
		
		// READ THE TYPED OBJECTS
		DataAccessMethod readtyped = new DataAccessMethod("READTYPED", new TwoObjectsArgument("TYPED",
				new ObjectArgument("MAIN", maintypedobject), new ObjectArgument("COMPANION", this.getParent())), false,
				false);
		readtyped.addInputArgument(new MethodArgument("OBJECTID", new ObjectIdArgument("OBJECT", this.getParent())));
		this.addDataAccessMethod(readtyped);
		// UPDATE THE TYPED OBJECT
		DataAccessMethod updatetyped = new DataAccessMethod("UPDATETYPED",null,false,false);
		updatetyped.addInputArgument(new MethodArgument("THISCOMPANION",new ObjectArgument("COMPANION",this.getParent())));
		updatetyped.addInputArgument(new MethodArgument("MAINOBJECT",new ObjectArgument("MAINOBJECT",maintypedobject)));

		this.addDataAccessMethod(updatetyped);
		// CREATE A TYPED OBJECT
		DataAccessMethod createtyped = new DataAccessMethod("CREATETYPED",null,false,false);
		createtyped.addInputArgument(new MethodArgument("THISCOMPANION",new ObjectArgument("COMPANION",this.getParent())));
		createtyped.addInputArgument(new MethodArgument("MAINOBJECT",new ObjectArgument("MAINOBJECT",maintypedobject)));
		createtyped.addInputArgument(new MethodArgument("TYPE",new ChoiceArgument("TYPE", mainobjecttypedproperty.getTypes())));
		this.addDataAccessMethod(createtyped);
		
		// INSERT AFTER TYPED OBJECT CREATION
		DataAccessMethod insertcompanion = new DataAccessMethod("INSERTCOMPANION",null,false,false);
		insertcompanion.addInputArgument(new MethodArgument("THISCOMPANION",new ObjectArgument("COMPANION",this.getParent())));
		insertcompanion.addInputArgument(new MethodArgument("MAINOBJECT",new ObjectArgument("MAINOBJECT",maintypedobject)));	
		this.addDataAccessMethod(insertcompanion);
		
		// put the main typed object as related to this property
		this.addChoiceCategoryHelper("TYPE",((Typed)maintypedobject.getPropertyByName("TYPED")).getTypes() );
		this.addPropertyGenerics(new PropertyGenerics("MAINTYPEDOBJECT",maintypedobject, maintypedobject.getPropertyByName("TYPED")));
		this.addDependentProperty(this.getParent().getPropertyByName("HASID"));
	}

	@Override
	public String[] getPropertyInitMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFinalSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		SimpleChoiceCategory typechoice = ((Typed)maintypedobject.getPropertyByName("TYPED")).getTypes();
		sg.wl("import " + typechoice.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(typechoice.getName()) + "ChoiceDefinition;");

	}

}
