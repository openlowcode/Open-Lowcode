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
import org.openlowcode.design.data.IntegerStoredElement;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * Records every evolution of an object by copying the data version of each
 * version of the object. This is a method with higher storage requirements, but
 * faster on consultation than alternative methods (such as auding on fields).
 * As of January 2020, alternative methods are not implemented<br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Iterated
		extends
		Property<Iterated> {
	private UniqueIdentified uniqueidentified;

	/**
	 * creates an iterated property for a given data object
	 */
	public Iterated() {

		super("ITERATED");
		// ----- can only iterate unique identified object

	}

	@Override
	public void controlAfterParentDefinition() {
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		// ----- stored elements: iteration and latest
		StoredElement iteration = new IntegerStoredElement("ITERATION");
		this.addElement(iteration, "Iteration",
				"the index of iteration for this copy of the object. Every update of the object is traced as an iteration in history",
				FIELDDISPLAY_NORMAL, 50, 5);
		// new element updatenote added 31/08/2017
		StoredElement updatenote = new StringStoredElement("UPDATENOTE", 200);
		this.addElement(updatenote, "Update Note", "Note explaining the business meaning of this iteration",
				FIELDDISPLAY_NORMAL, 49, 200);

		DataAccessMethod setupdatenote = new DataAccessMethod("SETUPDATENOTE", null, false);
		setupdatenote.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		setupdatenote.addInputArgument(
				new MethodArgument("UPDATENOTE", new StringArgument("UPDATENOTE", 200, "Update Note")));
		this.addDataAccessMethod(setupdatenote);
		StoredElement lastiteration = new StringStoredElement("LATEST", 1);
		this.addElement(lastiteration);
		DataAccessMethod getblankupdatenote = new DataAccessMethod("GETBLANKUPDATENOTE",
				new StringArgument("BLANKNOTE", 200, "Blank Note"), false);
		getblankupdatenote.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getblankupdatenote);
		// new method get alls iterations of an object

		DataAccessMethod getallobjectiterationsbyobjectid = new DataAccessMethod("GETALLOBJECTITERATIONSBYOBJECTID",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		getallobjectiterationsbyobjectid.addInputArgument(new MethodArgument("ID", new ObjectIdArgument("ID", parent)));
		this.addDataAccessMethod(getallobjectiterationsbyobjectid);

		DataAccessMethod archivecurrentiteration = new DataAccessMethod("ARCHIVETHISITERATION",
				new IntegerArgument("NEWITERATION"), false);
		archivecurrentiteration
				.addInputArgument(new MethodArgument("OBJECTTOARCHIVE", new ObjectArgument("OBJECTTOARCHIVE", parent)));

		this.addDataAccessMethod(archivecurrentiteration);

		// adding iterated link to all linkobjects refering to this object as left
		// object

		for (int i = 0; i < parent.getOwnermodule().getObjectNumber(); i++) {
			DataObjectDefinition thisobject = parent.getOwnermodule().getObject(i);
			LinkObject<?, ?> linkobject = (LinkObject<?, ?>) thisobject.getPropertyByName("LINKOBJECT");
			if (linkobject != null)
				if (linkobject.getLeftobjectforlink().getName().equals(parent.getName())) {
					this.addExternalObjectProperty(thisobject, new IteratedLink());
				}
		}

		this.addDataInput(new StringArgument("UPDATENOTE", 200, "Update note"));
		this.setDataInputForUpdate();
		this.hideDataInputForCreation();
		this.setDatainputatbottom(true);
		DataAccessMethod readiteration = new DataAccessMethod("READITERATION", new ObjectArgument("OBJECT", parent),
				false);
		readiteration.addInputArgument(new MethodArgument("ID", new ObjectIdArgument("ID", parent)));
		readiteration.addInputArgument(new MethodArgument("ITERATION", new IntegerArgument("ITERATION")));
		this.addDataAccessMethod(readiteration);

	}

	public Property<?> getDependentUniqueIdentified() {
		return uniqueidentified;
	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[1];
		returnvalues[0] = ".setupdatenote(updatenote);";
		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		String[] returnvalues = new String[1];
		returnvalues[0] = ".getblankupdatenote()";
		return returnvalues;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	@Override
	public void setFinalSettings() {

		MethodAdditionalProcessing generateiterationatcreation = new MethodAdditionalProcessing(true,
				uniqueidentified.getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(generateiterationatcreation);
		MethodAdditionalProcessing archiveandcreatenewiteration = new MethodAdditionalProcessing(true, true,
				uniqueidentified.getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(archiveandcreatenewiteration);

	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// do nothing

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
