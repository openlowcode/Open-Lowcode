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
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.server.data.properties.multichild.MultichildValueHelper;
import org.openlowcode.server.data.properties.multichild.MultidimensionchildHelper;

/**
 * A helper to manage the loading of objects with the Hasmultidimensionalchild
 * property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent for the multi-child relationship
 * @param <F> child for the multi-child relationship
 * @since 1.11
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

	/**
	 * @param payloadhelper
	 */
	public void setPayloadHelper(MultichildValueHelper<F, ?, E> payloadhelper) {
		this.payloadhelper = payloadhelper;
	}

	/**
	 * @return
	 */
	public MultichildValueHelper<F, ?, E> getPayloadHelper() {
		return this.payloadhelper;
	}

	/**
	 * @return
	 */
	public MultichildValueHelper<F, ?, E> getMainValueHelper() {
		return this.mainvaluehelper;
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
				
				String[] extraattributes = null;
				if (columnattributes.length > 2) {
					extraattributes = new String[columnattributes.length - 2];
					for (int i = 0; i < columnattributes.length - 2; i++)
						extraattributes[i] = columnattributes[i + 2];
				}

				return mainvaluehelper.new MainValueFlatFileLoader(this, locale, maincolumnvalue, payloadhelper,
						extraattributes);
			}
		}
		if (maincolumnattribute.equals("#TOTAL#")) {
			logger.finest("  >>> get total flat file loader");
			String[] extraattributes = null;
			if (columnattributes.length > 1) {
				extraattributes = new String[columnattributes.length - 1];
				for (int i = 0; i < columnattributes.length - 1; i++)
					extraattributes[i] = columnattributes[i + 1];
			}
			return this.payloadhelper.new MainValueTotalFlatFileLoader(this, locale, payloadhelper, extraattributes);
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

		Object[] mainvalues = mainvaluehelper.getMandatoryValues();
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

	public String getDebugForLineAndColumnKey(String linekey, String columnkey) {
		if (childrenbykey.get(linekey) == null) {
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

	public ArrayList<F> getChildrenForLine(String linekey) {
		Iterator<F> values = childrenbykey.get(linekey).values().iterator();
		ArrayList<F> valueslist = new ArrayList<F>();
		while (values.hasNext())
			valueslist.add(values.next());
		return valueslist;

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

	private void putChildInKeyMap(E currentobject,F child) {
		String keyforchild = this.helper.generateKeyForObject(child, true);

		HashMap<String, F> currentobjectsforkey = childrenbykey.get(keyforchild);
		if (currentobjectsforkey == null) {
			currentobjectsforkey = new HashMap<String, F>();
			childrenbykey.put(keyforchild, currentobjectsforkey);
			logger.finest("--- adding child map for key " + keyforchild);
		}
		String mainvalue = this.helper.getMainValueHelper().getAndPrint(child);
		F current = currentobjectsforkey.get(mainvalue);
		if (current != null)
			logger.warning("  -- Duplicate child for secondary key = " + keyforchild + " for main value "
					+ mainvalue + " for object = " + currentobject.dropIdToString());
		if (current == null) {
			currentobjectsforkey.put(mainvalue, child);
			logger.finest("      adding element for primary " + keyforchild + " secondary " + mainvalue + " for id "
					+ child.dropIdToString());
		}
	}
	
	public void initColumnsForObject(E currentobject) {
		F[] children = hasmultidimensionalchilddefinition.getChildren(currentobject.getId());
		logger.finest(" ---------------- Generating keys for " + currentobject.dropIdToString() + " - "
				+ (children != null ? children.length : "NULL") + " child(ren)---------------------------");
		childrenbykey = new HashMap<String, HashMap<String, F>>();
		for (int i = 0; i < children.length; i++) {
			F child = children[i];
			this.putChildInKeyMap(currentobject, child);
		}

	}

	public String[] generateKeyAndLoadExistingData(E currentobject) {

		// Find why two logics with children in initColumn and method below
		initColumnsForObject(currentobject);
		F[] children = hasmultidimensionalchilddefinition.getChildren(currentobject.getId());
		HashMap<String, String> classificationkeys = new HashMap<String, String>();
		for (int i = 0; i < children.length; i++) {
			F child = children[i];
			String keyforchild = this.helper.generateKeyForObject(child, true);
			classificationkeys.put(keyforchild, keyforchild);
		}
		Set<String> keys = classificationkeys.keySet();
		Iterator<String> keyiterator = keys.iterator();
		logger.finest(" ---------------- key drop -------------------");
		while (keyiterator.hasNext())
			logger.finest("         key " + keyiterator.next());
		logger.finest(" ----------------------------------------");
		return keys.toArray(new String[0]);

	}

	private ArrayList<String> secondaryvalues = new ArrayList<String>();
	private E contextobject = null;
	@SuppressWarnings("unused")
	private FlatFileLoader<?> parentflatfileloader;

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
		boolean same = false;
		if (contextobject != null) {
			if (object.getId().equals(contextobject.getId())) {
				same = true;
			}
		}
		if (!same) {
			contextobject = object;
			this.initColumnsForObject(contextobject);
		}

	}

	/**
	 * generates a new row of objects for the given context, and inserts them.
	 * 
	 * @param parent          parent object for generating new row
	 * @param extraattributes
	 * @param applocale
	 * @return true if the new row for context could be generated, false else
	 */
	public boolean generateNewRowForContext(
			E parent,
			ChoiceValue<ApplocaleChoiceDefinition> applocale,
			String[] extraattributes) {
		boolean iscontextvalid = true;
		logger.fine("---------------------- generate new row for context ----------------");
		for (int i = 0; i < secondaryvalues.size(); i++) {
			String value = secondaryvalues.get(i);
			logger.fine("   > Evaluating value "+value);
			MultichildValueHelper<F, ?, E> helper = secondvaluehelpers.get(i);
			helper.setContext(parent);
			boolean valid = helper.isTextValid(value, applocale, extraattributes);
			if (!valid) {
				logger.fine("      !!! INVALID !!! ");
				iscontextvalid = false;
			}
		}
		
		
		
		if (!iscontextvalid)
			return false;
		DataObjectDefinition<F> childdefinition = this.hasmultidimensionalchilddefinition
				.getRelatedDefinitionLinkedFromChildren().getChildObjectDefinition();
		F firstblank = childdefinition.generateBlank();
		for (int i = 0; i < secondaryvalues.size(); i++) {
			String value = secondaryvalues.get(i);
			MultichildValueHelper<F, ?, E> helper = secondvaluehelpers.get(i);
			helper.fillWithValue(firstblank, value, applocale, extraattributes);
		}
		logger.fine(" -> First blank = "+firstblank.dropToString());
		
		ArrayList<F> newobjects = mainvaluehelper.generateElementsForAllMandatory(firstblank,parent,helper,childrenbykey);
		logger.fine("Generating elements number = "+(newobjects==null?"null":newobjects.size()));
		if (newobjects!=null) if (newobjects.size()>0) {
			for (int i=0;i<newobjects.size();i++) {
				F newobject = newobjects.get(i);
				newobject.setmultidimensionparentidwithoutupdate(parent.getId());
				putChildInKeyMap(parent,newobject);
			}
			newobjects.get(0).getMassiveInsert().insert(newobjects.toArray(childdefinition.generateArrayTemplate()));
			
		}
		return true;
	}

	@Override
	public void setContextLoader(FlatFileLoader<?> flatfileloader) {
		this.parentflatfileloader = flatfileloader;
		
	}

	

}
