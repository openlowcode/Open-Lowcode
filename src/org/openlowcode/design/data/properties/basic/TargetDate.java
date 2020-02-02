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
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.TimestampStoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * This property provides a target date to an object with a lifecycle. Lifecycle
 * is defined by a {@link org.openlowcode.design.data.TransitionChoiceCategory}.
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.Lifecycle}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TargetDate
		extends
		Property<TargetDate> {
	private Lifecycle lifecycle;
	private String[] returnvalues;
	private CreationLog creationlog;

	/**
	 * create a target date property for an object with a lifecycle
	 */
	public TargetDate() {
		super("TARGETDATE");

	}

	@Override
	public void controlAfterParentDefinition() {
		this.creationlog = (CreationLog) parent.getPropertyByName("CREATIONLOG");
		if (this.creationlog == null)
			throw new RuntimeException("TargetDate is dependent on CreationLog property for object "
					+ this.getParent().getName() + ". This property is  missing");
		lifecycle = (Lifecycle) parent.getPropertyByName("LIFECYCLE");
		this.addDependentProperty(lifecycle);
		this.addChoiceCategoryHelper("LIFECYCLEHELPER", lifecycle.getTransitionChoiceCategory());
		StoredElement targetdate = new TimestampStoredElement("TARGETDATE");
		this.addElement(targetdate);
		DataAccessMethod settargetdate = new DataAccessMethod("SETTARGETDATE", null, false);
		settargetdate.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		settargetdate.addInputArgument(new MethodArgument("TARGETDATE", new TimestampArgument("TARGETDATE")));
		this.addDataAccessMethod(settargetdate);

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
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	@Override
	public void setFinalSettings() {
		returnvalues = new String[1];
		returnvalues[0] = ".settargetdate(targetdate);";
		TimestampArgument creationargument = new TimestampArgument("TARGETDATE", "Target Date", false);
		creationargument.setOptional(true);
		this.addDataInput(creationargument);

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
