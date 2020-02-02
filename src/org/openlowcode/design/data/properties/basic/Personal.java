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
import org.openlowcode.design.data.Property;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * An object with a personal property can assign privileges to specific people,
 * not a role 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Personal
		extends
		Property<Personal> {
	private LinkObject<?, ?> linkobjectproperty;
	private DataObjectDefinition linkobject;

	/**
	 * Creates a personal property on a data object
	 * 
	 * @param linkobject a link object from the object to the a specific user
	 */
	public Personal(DataObjectDefinition linkobject) {
		super(linkobject.getName().toUpperCase(), "PERSONAL");
		this.linkobject = linkobject;
		if (linkobject.getPropertyByName("LINKOBJECT") == null)
			throw new RuntimeException(
					"object " + linkobject.getName().toUpperCase() + " added for personal should be a link");
		linkobjectproperty = (LinkObject<?, ?>) linkobject.getPropertyByName("LINKOBJECT");
		if (linkobjectproperty.getBusinessRuleByName("UNIQUEFORLEFTANDRIGHT") == null)
			throw new RuntimeException("An object with Personal should have a link object '" + this.linkobject.getName()
					+ "' with business rule ConstraintOnLinkObjectUniqueForLeftAndRight");
	}

	/**
	 * @return the link object used for the personal property
	 */
	public DataObjectDefinition getLinkObject() {
		return this.linkobject;
	}

	@Override
	public void controlAfterParentDefinition() {
		super.controlAfterParentDefinition();
		if (linkobjectproperty.getLeftobjectforlink() != this.getParent())
			throw new RuntimeException(" link object '" + linkobject.getName().toUpperCase()
					+ "' should have as left object '" + this.getParent().getName() + "', not '"
					+ linkobjectproperty.getLeftobjectforlink().getName() + "'.");
		if (linkobjectproperty.getRightobjectforlink() != SystemModule.getSystemModule().getAppuser())
			throw new RuntimeException(" link object '" + linkobject.getName().toUpperCase()
					+ "' should have as right object System Module AppUser, not '"
					+ linkobjectproperty.getRightobjectforlink().getName() + "'.");

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
	public String[] getPropertyDeepCopyStatement() {
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
	}

}