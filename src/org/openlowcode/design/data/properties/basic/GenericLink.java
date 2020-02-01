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
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A generic link allows to link an object to any type of object. This will
 * store the id of the object and the type of object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GenericLink
		extends
		Property<GenericLink> {
	/**
	 * creates a generic link property on the object
	 * 
	 * @param name unique name amongst generic links on this data object
	 */
	public GenericLink(String name) {
		super(name, "GENERICLINK");

	}

	@Override
	public void controlAfterParentDefinition() {
		DataAccessMethod setobjectid = new DataAccessMethod("SETLINKEDOBJECTID", null, false);
		setobjectid.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setobjectid.addInputArgument(new MethodArgument("parent", new ObjectIdArgument("LINKEDOBJECT", null)));
		this.addDataAccessMethod(setobjectid);
		DataAccessMethod getobjectid = new DataAccessMethod("GETLINKEDOBJECTID",
				new ObjectIdArgument("LINKEDOBJECT", null), false);
		getobjectid.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		this.addDataAccessMethod(getobjectid);
		DataAccessMethod getallforgenericobject = new DataAccessMethod("GETALLFORGENERICID",
				new ArrayArgument(new ObjectArgument("object", this.parent)), true);
		getallforgenericobject
				.addInputArgument(new MethodArgument("GENERICOBJECTID", new ObjectIdArgument("LINKEDOBJECT", null)));
		this.addDataAccessMethod(getallforgenericobject);
		// -- field
		String idname = this.getName() + "ID";
		StoredElement objectid = new ObjectIdStoredElement(idname, null);
		objectid.setGenericsName("ID");
		this.addElement(objectid);
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();

		return dependencies;
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
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
