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
import org.openlowcode.design.data.Index;
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
 * This property is a split of the original UniqueIdentified property to store
 * only the fact that the object has a unique id that can be used for links and
 * other references
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 2.0
 *
 */
public class HasId
		extends
		Property<HasId> {

	public HasId() {
		super("HASID");

	}

	@Override
	public void controlAfterParentDefinition() {
		StoredElement id = new ObjectIdStoredElement("ID", parent);
		this.addElement(id, "Id", "technical identification", Property.FIELDDISPLAY_NORMAL, -50, 25);
		this.addIndex(new Index("ID", id, true));
		
		DataAccessMethod read = new DataAccessMethod("READONE", new ObjectArgument("OBJECT", parent), false);
		read.addInputArgument(new MethodArgument("ID", new ObjectIdArgument("ID", parent)));
		this.addDataAccessMethod(read);

		DataAccessMethod readseveral = new DataAccessMethod("READSEVERAL",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		readseveral.addInputArgument(new MethodArgument("ID", new ArrayArgument(new ObjectIdArgument("ID", parent))));
		this.addDataAccessMethod(readseveral);
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
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
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
	}

}
