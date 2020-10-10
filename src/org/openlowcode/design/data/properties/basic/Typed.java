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
import java.util.HashMap;

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A Typed data object (e.g. vehicle) can be declared with a specialized type
 * (e.g. car, truck, train, helicopter...). Specialized types can have specific
 * data (e.g. Rotor diameter for helicopters) in a companion object.
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 *
 * @param <E>
 */
public class Typed
		extends
		Property<Typed> {

	private SimpleChoiceCategory types;

	private HashMap<ChoiceValue, DataObjectDefinition> companionspertype;
	private ArrayList<DataObjectDefinition> allcompanions;
	
	
	/**
	 * @param types
	 */
	public Typed(SimpleChoiceCategory types) {
		super("TYPED");
		this.types = types;
		companionspertype = new HashMap<ChoiceValue, DataObjectDefinition>();
		allcompanions = new ArrayList<DataObjectDefinition>();
	}
	
	public SimpleChoiceCategory getTypes() {
		return types;
	}
	
	/**
	 * @param companion
	 * @param types
	 */
	protected void addCompanionObject(DataObjectDefinition companion, ChoiceValue[] types) {
		for (int i = 0; i < types.length; i++) {
			if (types[i] == null)
				throw new RuntimeException("Type " + i + " is null.");
			if (companionspertype.containsKey(types[i]))
				throw new RuntimeException("Duplicate companion declaration for type " + types[i] + " new companion = "
						+ companion.getName() + ", old companion = " + companionspertype.get(types[i]).getName());
		companionspertype.put(types[i],companion);
		allcompanions.add(companion);
		}
		
	}
	
	

	@Override
	public void controlAfterParentDefinition() {
		this.addChoiceCategoryHelper("TYPES", types);
		for (int i=0;i<allcompanions.size();i++) {
			DataObjectDefinition companion = allcompanions.get(i);
			if (companion.getPropertyByName("UNIQUEIDENTIFIED")!=null) throw new RuntimeException("Companion object cannot have property Unique Identified");
			if (companion.getPropertyByName("STOREDOBJECT")==null) {
				companion.addProperty(new StoredObject());
			}
			if (companion.getPropertyByName("HASID")==null) {
				companion.addProperty(new HasId());
			}
		}
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
		// TODO Auto-generated method stub

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
