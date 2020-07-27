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
import java.util.function.BiConsumer;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> type of parent object
 */
public class MultidimensionchildHelper<E extends DataObject<E>,F extends DataObject<F>> {
	private ArrayList<MultichildValueHelper<E, ?,F>> valuehelpers;
	private BiConsumer<E,E> consolidator;
	/**
	 * 
	 * 
	 * @param object one object
	 * @return a unique key with all value helpers
	 */
	public String generateKeyForObject(E object) {
		StringBuffer keybuffer = new StringBuffer();
		for (int i = 0; i < valuehelpers.size(); i++) {
			Object value = valuehelpers.get(i).get(object);
			keybuffer.append(value.toString().replace("@", "@@"));
			if (i > 0)
				keybuffer.append("@|@");
		}
		return keybuffer.toString();
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
	
	public String getKeyForConsolidation(E object,F parent) {
		for (int i = 0; i < valuehelpers.size(); i++) {
			MultichildValueHelper<E, ?,F> currentvaluehelper = valuehelpers.get(i);
			boolean discard = currentvaluehelper.replaceWithDefaultValue(object);
			if (discard)
				return null;
		}
		return generateKeyForObject(object);
	}

	/**
	 * @param parent
	 * @param objectdefinition
	 * @return
	 */
	public ArrayList<E> generateObjectsForAllValueHelpers(F parent,DataObjectDefinition<E> objectdefinition) {
		ArrayList<E> currentvalues = new ArrayList<E>();
		currentvalues.add(objectdefinition.generateBlank());
		for (int i = 0; i < valuehelpers.size(); i++) {
			currentvalues = generateObjectsForOneValueHelper(parent,currentvalues, objectdefinition, valuehelpers.get(i));
		}

		return currentvalues;
	}

	private <G extends Object> ArrayList<E> generateObjectsForOneValueHelper(
			F parent,
			ArrayList<E> valuesbeforehelper,
			DataObjectDefinition<E> objectdefinition,
			MultichildValueHelper<E, G,F> valuehelper) {
		ArrayList<E> newvaluearray = new ArrayList<E>();

		for (int i = 0; i < valuesbeforehelper.size(); i++) {
			E object = valuesbeforehelper.get(i);
			G[] values = valuehelper.getMinimumvalues();
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
	public MultidimensionchildHelper(BiConsumer<E,E> consolidator) {
		this.valuehelpers = new ArrayList<MultichildValueHelper<E, ?,F>>();
		this.consolidator = consolidator;
	}
	public void setContext(F parent) {
		for (int i=0;i<this.valuehelpers.size();i++) {
			this.valuehelpers.get(i).setContext(parent);
		}
	}
	/** 
	 * sets a value helper for a given field
	 * 
	 * @param valuehelper
	 */
	public void setChildHelper(MultichildValueHelper<E, ?,F> valuehelper) {
		this.valuehelpers.add(valuehelper);
	}

	/**
	 * @return the number of value helpers
	 */
	public int getValueHelperNumber() {
		return this.valuehelpers.size();
	}

	/**
	 * 
	 * 
	 * @param number a number between 0 (included) and getValueHelperNumber
	 *               (excluded)
	 * @return the value helper for corresponding index
	 */
	public MultichildValueHelper<E, ?,F> getValueHelper(int number) {
		return valuehelpers.get(number);
	}
	/**
	 * @return
	 */
	public BiConsumer<E,E> getConsolidator() {
		return this.consolidator;
	}
}
