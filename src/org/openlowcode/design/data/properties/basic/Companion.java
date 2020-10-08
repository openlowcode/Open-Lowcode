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

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.TwoObjectsArgument;
import org.openlowcode.design.generation.SourceGenerator;
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

	public Companion(DataObjectDefinition maintypedobject) {
		super("COMPANION");
		this.maintypedobject = maintypedobject;

	}

	@Override
	public void controlAfterParentDefinition() {
		// READ THE TYPED OBJECTS
		DataAccessMethod readtyped = new DataAccessMethod("READTYPED", new TwoObjectsArgument("TYPED",
				new ObjectArgument("MAIN", maintypedobject), new ObjectArgument("COMPANION", this.getParent())), false,
				true);
		readtyped.addInputArgument(new MethodArgument("OBJECTID", new ObjectIdArgument("OBJECT", this.getParent())));
		this.addDataAccessMethod(readtyped);
		// UPDATE THE TYPED OBJECT
		DataAccessMethod updatetyped = new DataAccessMethod("UPDATETYPED",null,false,true);
		updatetyped.addInputArgument(new MethodArgument("TYPEDOBJECT", new TwoObjectsArgument("TYPED",
				new ObjectArgument("MAIN", maintypedobject), new ObjectArgument("COMPANION", this.getParent()))));
		this.addDataAccessMethod(updatetyped);
		// put the main typed object as related to this property
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
		// TODO Auto-generated method stub

	}

}
