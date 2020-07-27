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

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectMasterIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.BooleanArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectMasterIdArgument;
import org.openlowcode.design.data.migrator.DataMigratorInitVersionMasterId;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.module.system.design.SystemModule;

/**
 * This property allows to create a number of versions of this object. A copy is
 * kept for each version, and each version is considered a distinct data object,
 * with adaptation of rules when required (e.g. several versions of the same
 * object can of course have the same number)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Versioned
		extends
		Property<Versioned> {

	public UniqueIdentified getRelatedUniqueIdentified() {
		return this.uniqueidentified;
	}

	private UniqueIdentified uniqueidentified;
	private boolean restrictnewversion;

	/**
	 * creates a Versioned property, potentially specifying that it will get
	 * different privileges from the action to create the object in the first place
	 * 
	 * @param restrictnewversion true if the new version action should not be part
	 *                           of the create action group.
	 */
	public Versioned(boolean restrictnewversion) {
		super("VERSIONED");
		this.restrictnewversion = restrictnewversion;
	}

	/**
	 * creates a Versioned property, giving privileges to create a new version to
	 * all authority that have privilege to create a new object
	 */
	public Versioned() {
		this(false);

	}

	/**
	 * @return true if new version creation is restricted and not part of the create
	 *         action group, false else
	 */
	public boolean isNewVersionRestricted() {
		return this.restrictnewversion;
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
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.properties.DataObjectMasterId;");

	}

	@Override
	public void controlAfterParentDefinition() {
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);

		StoredElement version = new StringStoredElement("VERSION", 8);
		this.addElement(version, "Version", "Version of the object", FIELDDISPLAY_NORMAL, 850, 8);
		StoredElement lastversion = new StringStoredElement("LASTVERSION", 1);
		this.addElementasSearchElement(lastversion, "Last Version", "unique business identifier of the object",
				Property.FIELDDIPLSAY_TITLE_MOD, 50, 1,
				new SearchWidgetDefinition(true, "LASTVERSION", "Last Version",
						SystemModule.getSystemModule().getLastVersionChoice(), new ChoiceValue[] {
								SystemModule.getSystemModule().getLastVersionChoice().GetValueForKey("Y") }));
		// Master Id to refer to the master
		StoredElement id = new ObjectMasterIdStoredElement("MASTERID", parent);
		this.addElement(id, "Master Id", "technical identification common to all versions of an object",
				Property.FIELDDISPLAY_NORMAL, -49, 25);
		this.addIndex(new Index("MSID", id, true));
		// ------------ add automatic migrator
		this.getParent().getOwnermodule().addMigrator(new DataMigratorInitVersionMasterId(this.getParent()));

	}

	@Override
	public void setFinalSettings() {

		// specific behaviour on insertion: generate master id, default version, and
		// precise latest
		MethodAdditionalProcessing generateversionatcreation = new MethodAdditionalProcessing(true,
				uniqueidentified.getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(generateversionatcreation);

		MethodAdditionalProcessing setnewlastversionafterdelete = new MethodAdditionalProcessing(false,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(setnewlastversionafterdelete);

		DataAccessMethod revise = new DataAccessMethod("REVISE", new ObjectArgument("REVISEDOBJECT", parent), false,
				true);
		revise.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(revise);
		


		DataAccessMethod getlastversion = new DataAccessMethod("GETLASTVERSION", new ObjectArgument("OBJECT", parent),
				false, true);
		getlastversion.addInputArgument(new MethodArgument("MASTERID", new ObjectMasterIdArgument("MASTERID", parent)));
		this.addDataAccessMethod(getlastversion);

		DataAccessMethod forceaslatest = new DataAccessMethod("FORCEASLATESTVERSION", null, false);
		forceaslatest.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(forceaslatest);

		DataAccessMethod canberevised = new DataAccessMethod("CANBEREVISED", new BooleanArgument("CANBEREVISED"),
				false);
		canberevised.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(canberevised);

		DataAccessMethod getallversions = new DataAccessMethod("GETALLVERSIONS",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), true);
		getallversions.addInputArgument(new MethodArgument("MASTERID", new ObjectMasterIdArgument("MASTERID", parent)));
		this.addDataAccessMethod(getallversions);

		DataAccessMethod getpreviousversion = new DataAccessMethod("GETPREVIOUSVERSION",
				new ObjectArgument("PREVIOUSVERSION", parent), false);
		getpreviousversion.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getpreviousversion);

		DataAccessMethod initversion = new DataAccessMethod("INITVERSION", null, false, true);
		initversion.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(initversion);

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	/**
	 * get the new version action to give it special privileges
	 * 
	 * @param parent data object the 'Versioned' property is in
	 * @return the version action
	 */
	public static AutomaticActionMarker getNewVersionAction(DataObjectDefinition parent) {
		return new AutomaticActionMarker("NEWVERSIONFOR" + parent.getName(), parent.getOwnermodule());
	}

}
