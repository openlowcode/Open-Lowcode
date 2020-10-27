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
import java.util.Iterator;

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

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
	private HashMap<DataObjectDefinition, ChoiceValue[]> typespercompanion;
	private ArrayList<DataObjectDefinition> allcompanions;

	public Iterator<ChoiceValue> getTypesIterator() {
		return companionspertype.keySet().iterator();
	}
	
	public DataObjectDefinition getCompanionForType(ChoiceValue type) {
		return companionspertype.get(type);
	}
	
	public DataObjectDefinition getCompanion(int index) {
		return this.allcompanions.get(index);
	}

	public int getCompanionNumber() {
		return this.allcompanions.size();
	}

	/**
	 * @param types
	 */
	public Typed(SimpleChoiceCategory types) {
		super("TYPED");
		this.types = types;
		companionspertype = new HashMap<ChoiceValue, DataObjectDefinition>();
		typespercompanion = new HashMap<DataObjectDefinition, ChoiceValue[]>();
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
			companionspertype.put(types[i], companion);
		}
		allcompanions.add(companion);
		if (typespercompanion.get(companion) != null)
			throw new RuntimeException("Companion " + companion.getName() + " has already been added");
		typespercompanion.put(companion, types);
	}

	@Override
	public void controlAfterParentDefinition() {
		this.addChoiceCategoryHelper("TYPES", types);
		for (int i = 0; i < allcompanions.size(); i++) {
			DataObjectDefinition companion = allcompanions.get(i);
			if (companion.getPropertyByName("UNIQUEIDENTIFIED") != null)
				throw new RuntimeException("Companion object cannot have property Unique Identified");
			if (companion.getPropertyByName("STOREDOBJECT") == null) {
				companion.addProperty(new StoredObject());
			}
			if (companion.getPropertyByName("HASID") == null) {
				companion.addProperty(new HasId());
			}
		}
		DataAccessMethod getobjectype = new DataAccessMethod("GETOBJECTTYPE",
				new ChoiceArgument("OBJECTTYPE", types), false);
		getobjectype.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getobjectype);
		
		DataAccessMethod settypebeforecreation = new DataAccessMethod("SETTYPEBEFORECREATION", null, false, false);
		settypebeforecreation
				.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", this.getParent())));
		settypebeforecreation.addInputArgument(new MethodArgument("TYPE", new ChoiceArgument("TYPE", types)));
		this.addDataAccessMethod(settypebeforecreation);
		this.addDependentProperty(this.getParent().getPropertyByName("UNIQUEIDENTIFIED"));
		StoredObject storedobject = (StoredObject) this.getParent().getPropertyByName("STOREDOBJECT");
		MethodAdditionalProcessing insertidgeneration = new MethodAdditionalProcessing(false,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(insertidgeneration);

		ArgumentContent typeargument = new ChoiceArgument("TYPE", types);
		typeargument.setOptional(false);
		this.addContextForDataCreation(typeargument);
		
		StoredElement type = new StringStoredElement("TYPE",types.getKeyStorageLength());
		this.addElementasSearchElement(type, "Type", "Type of "+parent.getLabel(),
				Property.FIELDDIPLSAY_TITLE_MOD, 860, 40,
				new SearchWidgetDefinition(true, "TYPE", "Type", types));

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

	@Override
	public void writeAdditionalDefinition(SourceGenerator sg) throws IOException {
		Iterator<DataObjectDefinition> companioniterator = this.typespercompanion.keySet().iterator();
		while (companioniterator.hasNext()) {
			DataObjectDefinition companion = companioniterator.next();
			sg.wl("		typed.getHelper().setCompanion(()->(new "
					+ StringFormatter.formatForJavaClass(companion.getName()) + "()), new ChoiceValue[] ");
			String values = "";
			ChoiceValue[] typeslist = this.typespercompanion.get(companion);
			for (int i = 0; i < typeslist.length; i++) {
				if (i > 0)
					values += ",";
				values += StringFormatter.formatForJavaClass(types.getName());
				values += "ChoiceDefinition.get().";
				values += typeslist[i].getName().toUpperCase();
			}
			sg.wl("				{" + values + "});");
		}

	}

}
