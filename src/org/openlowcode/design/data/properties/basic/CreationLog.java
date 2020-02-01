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
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.TimestampStoredElement;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * Will record the user who created the object and the date of creation
 * 
 * * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CreationLog
		extends
		Property<CreationLog> {

	/**
	 * Creates a creation log property
	 */
	public CreationLog() {
		super("CREATIONLOG");

	}

	@Override
	public void controlAfterParentDefinition() {
		Property<?> storedobject = parent.getPropertyByName("STOREDOBJECT");
		this.addDependentProperty(storedobject);
		MethodAdditionalProcessing insertcreationlog = new MethodAdditionalProcessing(true,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(insertcreationlog);
		StoredElement createuser = new ObjectIdStoredElement("CREATEUSERID",
				SystemModule.getSystemModule().getAppuser());
		this.addElement(createuser);
		this.addIndex(new Index("CREATEUSER", createuser, false));
		StoredElement creationtime = new TimestampStoredElement("CREATETIME");
		this.addElement(creationtime);
		this.addIndex(new Index("CREATETIME", creationtime, false));

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
		dependencies.add(SystemModule.getSystemModule().getAppuser());
		return dependencies;
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {

	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
