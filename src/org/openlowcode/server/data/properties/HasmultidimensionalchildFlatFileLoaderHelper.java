/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.server.data.properties.multichild.MultichildValueHelper;
import org.openlowcode.server.data.properties.multichild.MultidimensionchildHelper;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent for the multi-child relationship
 * @param <F> child for the multi-child relationship
 */
public class HasmultidimensionalchildFlatFileLoaderHelper<
		E extends DataObject<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & UniqueidentifiedInterface<F> & MultidimensionchildInterface<F, E>>
		implements
		CustomloaderHelper<E> {
	private static Logger logger = Logger.getLogger(HasmultidimensionalchildFlatFileLoaderHelper.class.getName());
	private HasmultidimensionalchildDefinition<E, F> hasmultidimensionalchilddefinition;
	private MultidimensionchildHelper<F, E> helper;
	private MultichildValueHelper<F, ?, E> mainvaluehelper;
	private MultichildValueHelper<F, ?, E> payloadhelper;
	private ArrayList<MultichildValueHelper<F, ?, E>> secondvaluehelpers;

	private HashMap<String, HashMap<String, F>> childrenbykey;

	public void setPayloadHelper(MultichildValueHelper<F, ?, E> payloadhelper) {
		this.payloadhelper = payloadhelper;
	}

	public MultichildValueHelper<F, ?, E> getPayloadHelper() {
		return this.payloadhelper;
	}

	/**
	 * @param hasmultidimensionalchilddefinition
	 */
	public HasmultidimensionalchildFlatFileLoaderHelper(
			HasmultidimensionalchildDefinition<E, F> hasmultidimensionalchilddefinition) {
		this.hasmultidimensionalchilddefinition = hasmultidimensionalchilddefinition;
		this.helper = this.hasmultidimensionalchilddefinition.getHelper();
		mainvaluehelper = helper.getMainValueHelper();
		secondvaluehelpers = helper.getSecondaryValueHelpers();
		payloadhelper = helper.getPayloadValueHelper();
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columnattributes == null)
			throw new RuntimeException("No column attribute for multichild");
		if (columnattributes.length == 0)
			throw new RuntimeException("Needs at least one column attribute for multichild");
		String maincolumnattribute = columnattributes[0];
		StringBuffer potentialattributes = new StringBuffer();
		for (int i = 0; i < secondvaluehelpers.size(); i++) {
			MultichildValueHelper<F, ?, E> secondvaluehelper = secondvaluehelpers.get(i);
			if (secondvaluehelper.getFieldName().equals(maincolumnattribute)) {
				return secondvaluehelper.new SecondValueFlatFileLoader(this, locale, i, (i == 0 ? true : false));
			} else {
				if (potentialattributes.length() > 0)
					potentialattributes.append(',');
				potentialattributes.append(secondvaluehelper.getFieldName());
			}
		}
		if (columnattributes.length >= 2) {
			if (maincolumnattribute.equals(this.mainvaluehelper.getFieldName())) {
				String maincolumnvalue = columnattributes[1];
				
				return mainvaluehelper.new MainValueFlatFileLoader(this, locale, maincolumnvalue,payloadhelper);
			}
		}
		potentialattributes.append(',');
		potentialattributes.append(mainvaluehelper.getFieldName());
		throw new RuntimeException("No Loader found for attribute " + maincolumnattribute + ", potential attributes "
				+ potentialattributes.toString());
	}

	@Override
	public String[] getLoaderFieldList() {
		ArrayList<String> loaderfieldlist = new ArrayList<String>();
		for (int i = 0; i < secondvaluehelpers.size(); i++) {
			MultichildValueHelper<F, ?, E> secondvaluehelper = secondvaluehelpers.get(i);
			loaderfieldlist.add(secondvaluehelper.getFieldName());
		}

		Object[] mainvalues = mainvaluehelper.getMinimumvalues();
		if (mainvalues != null)
			for (int i = 0; i < mainvalues.length; i++) {
				loaderfieldlist.add(mainvaluehelper.getFieldName() + "&" + mainvalues[i].toString());
			}

		return loaderfieldlist.toArray(new String[0]);
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return new String[] { "NOT IMPLEMENTED", "NOT IMPLEMENTED", "NOT IMPLEMENTED", "NOT IMPLEMENTED" };
	}

	@Override
	public void executeAtEndOfLine(E objecforprocessing) {
		// nothing to do

	}

	public String getDebugForLineAndColumnKey(String linekey,String columnkey) {
		if (childrenbykey.get(linekey)==null) {
			StringBuffer droplinekey = new StringBuffer(" Line key not existing, given value = ");
			droplinekey.append(linekey);
			droplinekey.append(", valid keys = ");
			Iterator<String> keyiterator = childrenbykey.keySet().iterator();
			while (keyiterator.hasNext()) {
				droplinekey.append(keyiterator.next());
				droplinekey.append(' ');
			}
			return droplinekey.toString();
		}
		HashMap<String, F> key = childrenbykey.get(linekey);
		StringBuffer droplinekey = new StringBuffer(" Main value not existing, linekey value = ");
		droplinekey.append(linekey);
		droplinekey.append(", given main value = ");
		droplinekey.append(columnkey);
		droplinekey.append(", possible values =  ");
		Iterator<String> mainvalueiterator = key.keySet().iterator();
		while (mainvalueiterator.hasNext()) {
			droplinekey.append(mainvalueiterator.next());
			droplinekey.append(' ');
		}
		return droplinekey.toString();
		
		
	}
	
	public F getChildForLineAndColumnKey(String linekey, String columnkey) {
		if (childrenbykey == null) {
			StringBuffer error = new StringBuffer("Children list not initiated for linekey = " + linekey
					+ ", columnkey = " + columnkey + " list of helpers:");
			error.append(" MAINVALUEHELPER:");
			error.append(this.mainvaluehelper);
			error.append(" PAYLOADHELPER: ");
			error.append(this.payloadhelper);
			for (int i = 0; i < this.secondvaluehelpers.size(); i++) {
				error.append(" SECONDVALUEHELPER" + i + ":" + this.secondvaluehelpers.get(i));
			}
			throw new RuntimeException(error.toString());
		}
		HashMap<String, F> linedata = childrenbykey.get(linekey);
		if (linedata == null)
			return null;
		return linedata.get(columnkey);
	}

	public F getFirstChildForLineKey(String key) {
		return childrenbykey.get(key).values().iterator().next();
	}

	public void initColumnsForObject(E currentobject) {
		F[] children = hasmultidimensionalchilddefinition.getChildren(currentobject.getId());
		logger.finest(" ---------------- Generating keys for "+currentobject.dropIdToString()+" ---------------------------");
		childrenbykey = new HashMap<String, HashMap<String, F>>();
		for (int i = 0; i < children.length; i++) {
			F child = children[i];
			String keyforchild = this.helper.generateKeyForObject(child, true);

			HashMap<String, F> currentobjectsforkey = childrenbykey.get(keyforchild);
			if (currentobjectsforkey == null) {
				currentobjectsforkey = new HashMap<String, F>();
				childrenbykey.put(keyforchild, currentobjectsforkey);
				logger.finest("--- adding child map for key "+keyforchild);
			}
			String mainvalue = this.helper.getMainValueHelper().getAndPrint(child);
			F current = currentobjectsforkey.get(mainvalue);
			if (current != null)
				logger.warning("  -- Duplicate child for secondary key = " + keyforchild + " for main value "
						+ mainvalue + " for object = " + currentobject.dropIdToString());
			if (current == null) {
				currentobjectsforkey.put(mainvalue, child);
				logger.finest("      adding element for primary "+keyforchild+" secondary "+mainvalue+" for id "+child.dropIdToString());
			}
		}

	}

	public String[] generateKeyAndLoadExistingData(E currentobject) {
		
		// Find why two logics with children in initColumn and method below
		initColumnsForObject(currentobject);
		F[] children = hasmultidimensionalchilddefinition.getChildren(currentobject.getId());
		HashMap<String,String> classificationkeys = new HashMap<String,String>();
		for (int i = 0; i < children.length; i++) {
			F child = children[i];
			String keyforchild = this.helper.generateKeyForObject(child, true);
			classificationkeys.put(keyforchild,keyforchild);
		}
		Set<String> keys = classificationkeys.keySet();
		Iterator<String> keyiterator = keys.iterator();
		logger.finest(" ---------------- key drop -------------------");
		while (keyiterator.hasNext()) logger.finest("         key "+keyiterator.next());
		logger.finest(" ----------------------------------------");
		return keys.toArray(new String[0]);

	}

	private ArrayList<String> secondaryvalues = new ArrayList<String>();
	private E contextobject = null;
	
	
	public void setSecondaryValueForLoading(int index, String string) {
		if (secondaryvalues.size() <= index)
			for (int i = secondaryvalues.size(); i < index + 1; i++)
				secondaryvalues.add(null);
		secondaryvalues.set(index, string);

	}

	public String getContextKey() {
		ArrayList<String> keyvalues = new ArrayList<String>();
		for (int i = 0; i < secondaryvalues.size(); i++) {
			keyvalues.add(secondaryvalues.get(i));
		}
		return MultidimensionchildHelper.generateKey(keyvalues);
	}

	public void setContext(E object) {
		boolean same=false;
		if (contextobject!=null) {
			if (object.getId().equals(contextobject.getId())) {
				same=true;
			}
		}
		if (!same) {
			contextobject=object;
			this.initColumnsForObject(contextobject);
		}
		
	}

}
