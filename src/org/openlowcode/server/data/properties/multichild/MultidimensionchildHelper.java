/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.multichild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.properties.MultidimensionchildInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> type of parent object
 */
public class MultidimensionchildHelper<
		E extends DataObject<E> & UniqueidentifiedInterface<E> & MultidimensionchildInterface<E, F>,
		F extends DataObject<F> & UniqueidentifiedInterface<F>> {
	private ArrayList<MultichildValueHelper<E, ?, F>> secondarycriteriavaluehelpers;
	private MultichildValueHelper<E, ?, F> maincriteriavaluehelper;
	private ArrayList<MultichildValueHelper<E, ?, F>> fullvaluehelpers;
	private MultichildValueHelper<E, ?, F> payloadvaluehelper;
	private BiConsumer<E, E> consolidator;

	public static String generateKey(ArrayList<String> values) {
		StringBuffer keybuffer = new StringBuffer();
		if (values == null)
			return "";
		for (int i = 0; i < values.size(); i++) {
			if (i > 0)
				keybuffer.append("@|@");
			
			if (values.get(i)!=null) keybuffer.append(values.get(i).replace("@", "@@"));
			if (values.get(i)==null) keybuffer.append("");
			
		}
		return keybuffer.toString();
	}

	public String generateKeyForObject(E object, boolean excludemainvalue) {
		ArrayList<String> elementsforkey = new ArrayList<String>();
		for (int i = 0; i < secondarycriteriavaluehelpers.size(); i++) {
			MultichildValueHelper<E, ?, F> thishelper = secondarycriteriavaluehelpers.get(i);
			
			elementsforkey.add(thishelper.getAndPrint(object));
		}
		if (!excludemainvalue) {
			elementsforkey.add(maincriteriavaluehelper.getAndPrint(object));
		}
		return generateKey(elementsforkey);

	}

	/**
	 * @param object one object
	 * @return a unique key with all value helpers
	 */
	public String generateKeyForObject(E object) {
		return generateKeyForObject(object, false);
	}

	/**
	 * returns the key to consolidate this object into, if it needs to be
	 * consolidated. Else, returns null if it should be dropped
	 * 
	 * @param object the object (on which consolidated values will be put so that
	 *               the object can be inserted if the consolidated value does not
	 *               yet exist)
	 * @return the key, or null if the value should be dropped
	 */

	public String getKeyForConsolidation(E object, F parent) {
		for (int i = 0; i < fullvaluehelpers.size(); i++) {
			MultichildValueHelper<E, ?, F> currentvaluehelper = fullvaluehelpers.get(i);
			boolean valid = currentvaluehelper.replaceWithDefaultValue(object);
			if (!valid)
				return null;
		}
		return generateKeyForObject(object);
	}

	/**
	 * @param parent
	 * @param objectdefinition
	 * @return
	 */
	public ArrayList<E> generateObjectsForAllValueHelpers(F parent, DataObjectDefinition<E> objectdefinition) {
		ArrayList<E> currentvalues = new ArrayList<E>();
		currentvalues.add(objectdefinition.generateBlank());
		for (int i = 0; i < fullvaluehelpers.size(); i++) {
			currentvalues = generateObjectsForOneValueHelper(parent, currentvalues, objectdefinition,
					fullvaluehelpers.get(i));
		}

		return currentvalues;
	}

	private <G extends Object> ArrayList<E> generateObjectsForOneValueHelper(
			F parent,
			ArrayList<E> valuesbeforehelper,
			DataObjectDefinition<E> objectdefinition,
			MultichildValueHelper<E, G, F> valuehelper) {
		ArrayList<E> newvaluearray = new ArrayList<E>();

		for (int i = 0; i < valuesbeforehelper.size(); i++) {
			E object = valuesbeforehelper.get(i);
			G[] values = valuehelper.getMandatoryValues();
			for (int j = 0; j < values.length; j++) {
				E newobject = object.deepcopy();
				valuehelper.set(newobject, values[j]);
				newvaluearray.add(newobject);
			}
		}
		return newvaluearray;
	}

	/**
	 * creates a multi-dimensional child helper with any value helper
	 */
	public MultidimensionchildHelper(BiConsumer<E, E> consolidator) {
		this.fullvaluehelpers = new ArrayList<MultichildValueHelper<E, ?, F>>();
		this.secondarycriteriavaluehelpers = new ArrayList<MultichildValueHelper<E, ?, F>>();
		this.consolidator = consolidator;

	}

	public void setContext(F parent) {
		for (int i = 0; i < this.fullvaluehelpers.size(); i++) {
			this.fullvaluehelpers.get(i).setContext(parent);
		}
	}

	/**
	 * sets a value helper for a given field
	 * 
	 * @param valuehelper
	 * @boolean main
	 */
	public void setChildHelper(MultichildValueHelper<E, ?, F> valuehelper, boolean main) {
		this.fullvaluehelpers.add(valuehelper);
		if (main) {
			this.maincriteriavaluehelper = valuehelper;
		} else {
			this.secondarycriteriavaluehelpers.add(valuehelper);
		}
	}

	/**
	 * sets a value helper for a given field
	 * 
	 * @param valuehelper
	 */
	public void setChildHelper(MultichildValueHelper<E, ?, F> valuehelper) {
		this.setChildHelper(valuehelper, false);
	}

	public void setPayloadHelper(MultichildValueHelper<E,?,F> payloadvaluehelper) {
		this.payloadvaluehelper = payloadvaluehelper;
	}
	
	public MultichildValueHelper<E,?,F> getPayloadValueHelper() {
		return this.payloadvaluehelper;
	}
	
	
	/**
	 * @return the number of value helpers
	 */
	public int getValueHelperNumber() {
		return this.fullvaluehelpers.size();
	}

	/**
	 * 
	 * 
	 * @param number a number between 0 (included) and getValueHelperNumber
	 *               (excluded)
	 * @return the value helper for corresponding index
	 */
	public MultichildValueHelper<E, ?, F> getValueHelper(int number) {
		return fullvaluehelpers.get(number);
	}

	/**
	 * @return the consolidator
	 */
	public BiConsumer<E, E> getConsolidator() {
		return this.consolidator;
	}


	/**
	 * @return the main value helper
	 */
	public MultichildValueHelper<E, ?, F> getMainValueHelper() {
		return this.maincriteriavaluehelper;
	}

	/**
	 * @return the secondary value helper
	 */
	public ArrayList<MultichildValueHelper<E, ?, F>> getSecondaryValueHelpers() {
		return this.secondarycriteriavaluehelpers;
	}

	

	/**
	 * Specific treatment for new lines.
	 * 
	 * @param newline
	 * @return true if the element has at least one valid criteria and null criterias (no invalid criteria)
	 * @since 1.15
	 */
	public boolean isValidOrVoid(E newline) {
		boolean hasonevalidelement=false;
		for (int i=0;i<this.secondarycriteriavaluehelpers.size();i++) {
			MultichildValueHelper<E, ?, F> helper = this.secondarycriteriavaluehelpers.get(i);
			if (!helper.allowUserValue()) if (!helper.allowothervalues()) {
				boolean isvalid = helper.isValid(newline);
				if (isvalid) hasonevalidelement=true;
				if (!isvalid) if (helper.get(newline)!=null) return false;
			}
		}
		if (hasonevalidelement) return true;
		return false;
	}
	
	/**
	 * Multiply the blank objects
	 * 
	 * @param thisline
	 * @param previouschildren
	 * @return
	 * @since 1.15
	 */
	public ArrayList<E> multiplyforvoidfields(E thisline, E[] previouschildren) {
		ArrayList<E> newlines = new ArrayList<E>();
		boolean hasonenullcriteria=false;
		for (int i=0;i<this.secondarycriteriavaluehelpers.size();i++) {
			MultichildValueHelper<E, ?, F> helper = this.secondarycriteriavaluehelpers.get(i);
			if (helper.get(thisline)==null) hasonenullcriteria=true;
		}
		if (!hasonenullcriteria) newlines.add(thisline);
		if (hasonenullcriteria) {
			
			newlines.add(thisline);
			for (int i=0;i<this.secondarycriteriavaluehelpers.size();i++) {
				MultichildValueHelper<E, ?, F> helper = this.secondarycriteriavaluehelpers.get(i);
				if (helper.get(thisline)==null) {
					SecondaryValueSelection<E, ?, F> secondaryvalueselection = helper.getSecondaryValueSelectionForField();
					secondaryvalueselection.GetAllValuesFromExistingObjects(previouschildren);
					newlines = secondaryvalueselection.createclonesforallvalues(newlines);
				}
			}
		}
		return newlines;
		
	}
	
	/**
	 * @param a child object
	 * @return true if invalid
	 * @since 1.12
	 */
	public boolean isInvalid(E optionalorinvalid) {
		for (int i=0;i<this.secondarycriteriavaluehelpers.size();i++) {
			MultichildValueHelper<E, ?, F> helper = this.secondarycriteriavaluehelpers.get(i);
			if (!helper.allowUserValue()) if (!helper.allowothervalues()) {
				boolean isvalid = helper.isValid(optionalorinvalid);
				if (!isvalid) return true;
			}
		}
		return false;
	}

	/**
	 * @param thisoptional
	 * @param childrenbykey a map of children by key (generated by the helper
	 * @return the children to add
	 * @since 1.12
	 */
	public ArrayList<E> getOtherPrimaryelements(E thisoptional, HashMap<String, E> childrenbykey) {
		return this.maincriteriavaluehelper.getMissingElementsForKey(thisoptional,childrenbykey,this,this.payloadvaluehelper);
		
	}

	

	

	

}
