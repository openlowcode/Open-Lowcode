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
import java.util.Iterator;
import java.util.Map.Entry;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.MultidimensionchildInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * A class holding a selection of secondary values 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> payload of the field
 * @param <G> type of the parent object (or any other object to be used)
 * @since 1.15
 */
public class SecondaryValueSelection<
E extends DataObject<E> & UniqueidentifiedInterface<E> & MultidimensionchildInterface<E, G>,
F extends Object,
G extends DataObject<G> & UniqueidentifiedInterface<G>> {
	private MultichildValueHelper<E,F,G> valuehelper;
	private HashMap<String,F> existingvalues;
	/**
	 * Creates a secondary value selection for the secondary field
	 * 
	 * @param valuehelper secondary field helper
	 */
	public SecondaryValueSelection(MultichildValueHelper<E,F,G> valuehelper) {
		this.valuehelper = valuehelper;
	}
	
	/**
	 * Initializes the object with all the values present in the existing objects
	 * 
	 * @param existingobjects all existing children of the parent for the Multidimensionchild
	 */
	public void GetAllValuesFromExistingObjects(E[] existingobjects) {
		existingvalues = new HashMap<String,F>();
		if (existingobjects!=null) for (int i=0;i<existingobjects.length;i++) {
			E existingobject = existingobjects[i];
			F value = valuehelper.get(existingobject);
			if (value!=null) existingvalues.put(valuehelper.print(value), value);
		}
	}
	
	/**
	 * multiplies the objects given in entry setting one value
	 * 
	 * @param entryvalues entry values
	 * @return one clone per value from existing object for the 
	 */
	public ArrayList<E> createclonesforallvalues(ArrayList<E> entryvalues) {
		ArrayList<E> newvalues = new ArrayList<E>();
		for (int i=0;i<entryvalues.size();i++) {
			E originvalue = entryvalues.get(i);
			Iterator<Entry<String, F>> valuesiterator = existingvalues.entrySet().iterator();
			while (valuesiterator.hasNext()) {
				F valueforfield = valuesiterator.next().getValue();
				E copy = originvalue.deepcopy();
				valuehelper.set(copy, valueforfield);
				newvalues.add(copy);
			}
			
		}
		return newvalues;
	}
	
}
