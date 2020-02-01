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
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * this property is put on data objects that are used to store dependencies
 * between items in a schedule
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ScheduleDependency
		extends
		Property<ScheduleDependency> {

	private AutolinkObject<?> autolink;
	private DataObjectDefinition scheduleobject;
	private Schedule scheduleproperty;

	/**
	 * create a property schedule dependency
	 * 
	 * @param scheduleobject   the object used for schedule items
	 * @param scheduleproperty the schedule property of the object used for schedule
	 *                         items
	 */
	public ScheduleDependency(DataObjectDefinition scheduleobject, Schedule scheduleproperty) {
		super("SCHEDULEDEPENDENCY");
		this.scheduleobject = scheduleobject;
		this.scheduleproperty = scheduleproperty;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.autolink = (AutolinkObject<?>) parent.getPropertyByName("AUTOLINKOBJECT");
		this.addDependentProperty(autolink);
		this.addPropertyGenerics(new PropertyGenerics("SCHEDULE", scheduleobject, scheduleproperty));
		StoredElement split = new StringStoredElement("SPLIT", 8);
		this.addElement(split);
		DataAccessMethod setassplit = new DataAccessMethod("SETASSPLIT", null, false);
		setassplit.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(setassplit);
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
		MethodAdditionalProcessing adddependency = new MethodAdditionalProcessing(false,
				autolink.getUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(adddependency);

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
