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
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.module.system.design.SystemModule;

/**
 * 
 * Will record the user who last updated the object and the date of update
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UpdateLog
		extends
		Property<UpdateLog> {
	/**
	 * creates an update log property on the object
	 */
	public UpdateLog() {
		super("UPDATELOG");

	}

	@Override
	public void controlAfterParentDefinition() {
		Property<?> uniqueidentifiedobject = parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentifiedobject);
		MethodAdditionalProcessing updatelog = new MethodAdditionalProcessing(true,
				uniqueidentifiedobject.getDataAccessMethod("UPDATE"));

		this.addMethodAdditionalProcessing(updatelog);
		Property<?> storedobjectproperty = parent.getPropertyByName("STOREDOBJECT");
		this.addDependentProperty(storedobjectproperty);
		MethodAdditionalProcessing updateloginit = new MethodAdditionalProcessing(true,
				storedobjectproperty.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(updateloginit);
		StoredElement updateuser = new ObjectIdStoredElement("UPDATEUSERID",
				SystemModule.getSystemModule().getAppuser());
		this.addElement(updateuser);
		this.addIndex(new Index("UPDATEUSER", updateuser, false));
		StoredElement updatetime = new TimestampStoredElement("UPDATETIME");
		this.addElementasSearchElement(updatetime, "Updated on", "", FIELDDISPLAY_NORMAL, -150, 30, 
				new SearchWidgetDefinition(true,"UPDATETIME", "Updated on",SearchWidgetDefinition.TYPE_DATE, SearchWidgetDefinition.POSTTREATMENT_NONE));
	
		this.addIndex(new Index("UPDATETIME", updatetime, false));
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
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings()  {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
