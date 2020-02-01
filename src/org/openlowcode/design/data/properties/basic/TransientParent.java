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
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A transient parent property allows to store the data object id of another
 * object on the current object. This is typically used to have a link back to
 * persisted data objects on a report object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TransientParent
		extends
		Property<TransientParent> {

	private DataObjectDefinition parentobjectforlink;

	/**
	 * creates a transient parent property
	 * 
	 * @param name                name of the property, should be unique amongst
	 *                            transient parents for this object, and a valid
	 *                            java field name
	 * @param parentobjectforlink data object the transient link will point to.
	 *                            Should be unique identified
	 */
	public TransientParent(String name, DataObjectDefinition parentobjectforlink) {
		super(name, "TRANSIENTPARENT");
		this.parentobjectforlink = parentobjectforlink;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.addPropertyGenerics(
				new PropertyGenerics("PARENTOBJECTFORLINK", parentobjectforlink, new UniqueIdentified()));
		DataAccessMethod setparent = new DataAccessMethod("SETPARENT", null, false);
		setparent.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setparent.addInputArgument(
				new MethodArgument("parent", new ObjectIdArgument("parentobject", this.parentobjectforlink)));
		this.addDataAccessMethod(setparent);

		String idname = this.getName() + "ID";
		StoredElement parentid = new ObjectIdStoredElement(idname, parentobjectforlink);
		parentid.setGenericsName("ID");
		this.addElement(parentid);
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
	public String[] getPropertyDeepCopyStatement() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(parentobjectforlink);
		return dependencies;
	}

	@Override
	public void setFinalSettings() {
	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}
}
