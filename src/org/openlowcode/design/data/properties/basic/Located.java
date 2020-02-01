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
import org.openlowcode.design.data.ExternalElement;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.properties.security.LocationHelperDefinition;
import org.openlowcode.design.data.properties.security.ParentLocationHelperDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * The property 'located' locates the data object on a Domain of the related
 * module
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Located
		extends
		Property<Located> {
	private DataObjectDefinition domaindef;
	private LocationHelperDefinition locationhelper;

	/**
	 * Creates a property located for the data object
	 * 
	 * @param locationhelper the helper generating the location on the data object
	 */
	public Located(LocationHelperDefinition locationhelper) {
		super("LOCATED");
		this.locationhelper = locationhelper;

	}

	/**
	 * @return the location helper
	 */
	public LocationHelperDefinition getLocationHelper() {
		return this.locationhelper;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void controlAfterParentDefinition() {
		domaindef = SystemModule.getSystemModule().getDomain();
		//
		Property<?> uniqueidentifiedobject = parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentifiedobject);
		Property<?> storedobject = parent.getPropertyByName("STOREDOBJECT");
		this.addDependentProperty(storedobject);
		// set method additional processing
		MethodAdditionalProcessing setdefaultlocationinsert = new MethodAdditionalProcessing(true,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(setdefaultlocationinsert);

		MethodAdditionalProcessing setdefaultlocationupdate = new MethodAdditionalProcessing(true,
				uniqueidentifiedobject.getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(setdefaultlocationupdate);

		// data
		StoredElement location = new ObjectIdStoredElement("LOCATIONDOMAINID", domaindef);
		this.addElement(location);
		this.addIndex(new Index("LOCATION", location, false));
		// external data
		Numbered locationnumberedproperty = (Numbered) domaindef.getPropertyByName("NUMBERED");
		ExternalElement refnameelement = new ExternalElement(this, domaindef, locationnumberedproperty, false,
				(StoredElement) locationnumberedproperty.getElements()[0]);
		this.addElement(refnameelement);
		// Methods - 1 - Set Location
		DataAccessMethod setlocation = new DataAccessMethod("SETLOCATION", null, false);
		setlocation.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setlocation.addInputArgument(new MethodArgument("domain", new ObjectIdArgument("domain", domaindef)));
		this.addDataAccessMethod(setlocation);

		if (locationhelper instanceof ParentLocationHelperDefinition) {
			ParentLocationHelperDefinition parentlocationhelper = (ParentLocationHelperDefinition) locationhelper;

			this.addExternalObjectProperty(parentlocationhelper.getParentLinkForLocation().getParentObjectForLink(),
					new LinkedFromChildrenForLocation(
							parentlocationhelper.getParentLinkForLocation().getInstancename() + "for"
									+ parent.getName().toLowerCase(),
							parent, parentlocationhelper.getParentLinkForLocation().getLinkedFromChildren(),
							parentlocationhelper.getParentLinkForLocation(), this));

		}

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
		dependencies.add(domaindef);
		return dependencies;

	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[0];
		return returnvalues;
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

	@Override
	public String getPropertyHelperName() {
		return StringFormatter.formatForJavaClass(this.getParent().getName()) + "LocationHelper";
	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {
		locationhelper.generateLocationHelper(sg, module);
	}

}
