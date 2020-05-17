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

import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class SubtypeCompanion<E extends DataObjectDefinition>
		extends
		Property<SubtypeCompanion<E>> {

	Subtype<?> subtypeformainobject;
	DataObjectDefinition mainobjet;
	private DataObjectDefinition mainobject;
	private ChoiceValue[] typesforcompanion;
	private SimpleChoiceCategory subtypecategory;
	private ChoiceCategory types;
	private StoredObject storedobject;

	public SubtypeCompanion(DataObjectDefinition mainobject, ChoiceValue[] typesforcompanion) {
		super("SUBTYPECOMPANION");
		if (mainobject == null)
			throw new RuntimeException("Main object is not defined");
		subtypeformainobject = (Subtype<?>) mainobject.getPropertyByName("SUBTYPE");
		this.mainobject = mainobject;
		this.typesforcompanion = typesforcompanion;
		if (typesforcompanion == null)
			throw new RuntimeException("SubtypeCompanion property for object " + mainobject.getName()
					+ "  does not have a type for companion choice (null array)");
		if (typesforcompanion.length == 0)
			throw new RuntimeException("SubtypeCompanion property for object " + mainobject.getName()
					+ " does not have a type for companion choice (zero element array)");
		types = typesforcompanion[0].getParent();
		this.addChoiceCategoryHelper("LISTOFSUBTYPES", types);

	}

	@Override
	public void controlAfterParentDefinition() {
		this.storedobject = (StoredObject) parent.getPropertyByName("STOREDOBJECT");
		if (this.storedobject == null)
			throw new RuntimeException("Subtypecompanion is dependent on storedobject");
		this.addDependentProperty(storedobject);
		this.addPropertyGenerics(new PropertyGenerics("MAINOBJECT", mainobject, new UniqueIdentified()));
	}

	@Override
	public String[] getPropertyInitMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFinalSettings() {
		if (this.getParent().getPropertyByName("UNIQUEIDENTIFIED") != null)
			throw new RuntimeException("Object " + this.getParent().getName()
					+ " has both subtype companion and unique identified, this is not possible");
		subtypeformainobject = (Subtype<?>) mainobject.getPropertyByName("SUBTYPE");
		if (subtypeformainobject == null)
			throw new RuntimeException(
					"Main object " + this.mainobject.getName() + " does not have the property SUBTYPE, so object "
							+ this.getParent().getName() + " cannot reference it as a main object for subtype");
		subtypecategory = subtypeformainobject.getSubTypes();
		for (int i = 0; i < typesforcompanion.length; i++) {
			ChoiceValue thissubtype = typesforcompanion[i];

			if (!subtypecategory.isKeyPresent(thissubtype.getName()))
				throw new RuntimeException("Subtype " + thissubtype + " for companion object "
						+ this.getParent().getName() + " is not part of the category for main object "
						+ subtypeformainobject.getParent().getName());
		}

	}

	@Override
	public String getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

		sg.wl("import " + types.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(types.getName()) + "ChoiceDefinition;");

	}

}
