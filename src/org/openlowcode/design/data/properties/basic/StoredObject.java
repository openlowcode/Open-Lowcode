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

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property will allow persistence of the object in the database. Most data
 * objects, except reports, should add this property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StoredObject
		extends
		Property<StoredObject> {

	private NamedList<DynamicActionDefinition> actionsonobject;
	private boolean saveasincreatenewgroup;

	/**
	 * @return
	 */
	public boolean isSaveAsInCreateNewGroup() {
		return this.saveasincreatenewgroup;
	}

	/**
	 * creates a stored object property with default save-as / duplicate action
	 * visible in create new group
	 */
	public StoredObject() {
		this(true);
	}

	/**
	 * creates a stored object property with specified visibility for save-as /
	 * duplicate action
	 * 
	 * @param saveasincreatenewgroup
	 */
	public StoredObject(boolean saveasincreatenewgroup) {
		super("STOREDOBJECT");
		this.saveasincreatenewgroup = saveasincreatenewgroup;
	}

	@Override
	public void controlAfterParentDefinition() {
		DataAccessMethod insert = new DataAccessMethod("INSERT", null, false, true);
		insert.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(insert);
		DataAccessMethod readallactive = new DataAccessMethod("GETALLACTIVE",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), true);
		this.addDataAccessMethod(readallactive);
		this.actionsonobject = new NamedList<DynamicActionDefinition>();
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	/**
	 * adds an action on the object display page
	 * 
	 * @param action an action with a single argument being the data object
	 */
	public void addActionOnObject(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException("you can add an action on object only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(0);
		if (!(uniqueinputargument instanceof ObjectArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectArgument objectargument = (ObjectArgument) uniqueinputargument;
		DataObjectDefinition objectforargument = objectargument.getPayload();
		if (objectforargument != parent) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforargument.getOwnermodule().getName() + "/" + objectforargument.getName()
					+ ", object parentid type = " + parent.getOwnermodule().getName() + "/" + parent.getName());
		}

		actionsonobject.add(action);
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
