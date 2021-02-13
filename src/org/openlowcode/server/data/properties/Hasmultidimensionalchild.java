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
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.multichild.MultidimensionchildHelper;

/**
 * Property for the parent object having a multi dimension child
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of parent object
 * @param <F> type of child object
 * @since 1.11
 */
public class Hasmultidimensionalchild<
		E extends DataObject<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & UniqueidentifiedInterface<F> & MultidimensionchildInterface<F, E>>
		extends
		DataObjectProperty<E> {
	@SuppressWarnings("unused")
	private Linkedfromchildren<E, F> relatedpropertylinkedfromchildren;
	private HasmultidimensionalchildDefinition<E, F> casteddefinition;

	private DataObjectDefinition<F> childobjectdefinition;

	private static Logger logger = Logger.getLogger(Hasmultidimensionalchild.class.getName());

	/**
	 * @param relatedpropertylinkedfromchildren linked from children property the
	 *                                          HasMultiDimensionChild is enriching
	 */
	public void setDependentPropertyLinkedfromchildren(Linkedfromchildren<E, F> relatedpropertylinkedfromchildren) {
		this.relatedpropertylinkedfromchildren = relatedpropertylinkedfromchildren;
	}

	/**
	 * Creates the property
	 * 
	 * @param definition    definition of the property
	 * @param parentpayload payload of the parent object
	 */
	public Hasmultidimensionalchild(
			HasmultidimensionalchildDefinition<E, F> definition,
			DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.casteddefinition = definition;
		childobjectdefinition = definition.getRelatedDefinitionLinkedFromChildren().getChildObjectDefinition();
	}

	/**
	 * pre-treatment of the object before insert
	 * 
	 * @param objectbatch    batch of object
	 * @param versionedbatch corresponding batch of HasMultiDimensionalChild
	 *                       properties
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & UniqueidentifiedInterface<F> & MultidimensionchildInterface<
					F, E>> void postprocStoredobjectInsert(
							E[] objectbatch,
							Hasmultidimensionalchild<E, F>[] multidimensionalchildbatch) {
		logger.warning("Not yet developed for massive");
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (multidimensionalchildbatch == null)
			throw new RuntimeException("versioned batch is null");
		if (objectbatch.length != multidimensionalchildbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with versioned batch length " + objectbatch.length);
		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				multidimensionalchildbatch[i].postprocStoredobjectInsert(objectbatch[i]);
			}
		}

	}

	/**
	 * This method add all children objects. It will create all line objects for the
	 * given template objects
	 * 
	 * @param object   parent object
	 * @param newlines template objects providing secondary values to be used. For
	 *                 each template object provided, all objects corresponding to
	 *                 all columns will be created
	 */
	public void addlines(E object, F[] newlines) {
		MultidimensionchildHelper<F, E> helper = this.casteddefinition.getHelper();
		helper.setContext(object);
		// get existing children
		F[] previouschildren = this.casteddefinition.getChildren(object.getId());
		// check if new line templates collide with existing, delete the ones that exist
		HashMap<String, F> childrenbykey = new HashMap<String, F>();
		for (int i = 0; i < previouschildren.length; i++) {
			F thischild = previouschildren[i];
			String key = helper.generateKeyForObject(thischild);
			childrenbykey.put(key, thischild);
		}
		ArrayList<F> allobjectstoinsert = new ArrayList<F>();
		
		for (int i = 0; i < newlines.length; i++) {
			F thisline = newlines[i];
			boolean valid = helper.isValidOrVoid(thisline);
			if (valid) {
				ArrayList<F> multipliedlines = helper.multiplyforvoidfields(thisline,previouschildren);
				for (int j=0;j<multipliedlines.size();j++) {
					ArrayList<F> missingforthisoptional = helper.getOtherPrimaryelements(multipliedlines.get(j), childrenbykey);
					for (int k = 0; k < missingforthisoptional.size(); k++) {
						F thismissing = missingforthisoptional.get(k);
						allobjectstoinsert.add(thismissing);
						childrenbykey.put(helper.generateKeyForObject(thismissing), thismissing);
					}
				}
			}
		}

		for (int i = 0; i < allobjectstoinsert.size(); i++)
			allobjectstoinsert.get(i).setmultidimensionparentidwithoutupdate(object.getId());

		F[] objectstoinsert = allobjectstoinsert.toArray(childobjectdefinition.generateArrayTemplate());
		logger.finer("   inserting " + (objectstoinsert != null ? objectstoinsert.length : "null") + " objects");
		if (objectstoinsert != null)
			for (int i = 0; i < objectstoinsert.length; i++)
				logger.finest("             " + helper.generateKeyForObject(objectstoinsert[i]));
		if (objectstoinsert != null)
			if (objectstoinsert.length > 0) {
				objectstoinsert[0].getMassiveInsert().insert(objectstoinsert);
			}

	}

	/**
	 * This method will check all the children of the object, and
	 * <ul>
	 * <li>add missing mandatory values</li>
	 * <li>add missing primary values for any combination of secondary</li>
	 * <li>if some invalid combinations are present, and a consolidation possibility
	 * exists, consolidate the value</li>
	 * <li>If some invalid combination is present, and no consolidation possibility
	 * exists, either</li>
	 * <ul>
	 * <li>blows an error at the end (but do the possible consolidation)</li>
	 * <li>deletes the illegal value</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param object
	 */
	public void repair(E object, ChoiceValue<BooleanChoiceDefinition> deleteifinvalid) {
		// checks if blows or delete for unexpected data
		boolean blowsnotdelete = true;
		if (deleteifinvalid != null)
			if (BooleanChoiceDefinition.get().YES.equals(deleteifinvalid))
				blowsnotdelete = false;
		// gets previous children
		F[] previouschildren = this.casteddefinition.getChildren(object.getId());
		HashMap<String, F> childrenbykey = new HashMap<String, F>();
		
		// Creates a list of duplicates to be removed, newest children are stored here to be removed later
		
		ArrayList<F> duplicatechildren = new ArrayList<F>();
		
		// get MultiDimensionHelper
		MultidimensionchildHelper<F, E> multidimensionalchildhelper = this.casteddefinition.getHelper();
		multidimensionalchildhelper.setContext(object);
		for (int i = 0; i < previouschildren.length; i++) {
			F thischild = previouschildren[i];
			String key = multidimensionalchildhelper.generateKeyForObject(thischild);
			if (childrenbykey.containsKey(key)) {
				duplicatechildren.add(thischild);
				 
			}
				else {
			childrenbykey.put(key, thischild);
				}
		}

		// check missing mandatory

		ArrayList<F> blankobjects = multidimensionalchildhelper.generateObjectsForAllValueHelpers(object,
				childobjectdefinition);
		ArrayList<F> allobjectstoinsert = new ArrayList<F>();
		ArrayList<F> allobjectstodelete = new ArrayList<F>();
		HashMap<String, F> compulsorychildren = new HashMap<String, F>();
		logger.fine("      blank objects created = " + blankobjects.size());
		for (int i = 0; i < blankobjects.size(); i++) {
			F thisobject = blankobjects.get(i);
			String key = multidimensionalchildhelper.generateKeyForObject(thisobject);
			if (!childrenbykey.containsKey(key)) {
				childrenbykey.put(key, thisobject);
				allobjectstoinsert.add(thisobject);
			}
			compulsorychildren.put(key, thisobject);
		}

		
		
		// detects optionals and invalids

		ArrayList<F> optionalsandinvalids = new ArrayList<F>();
		ArrayList<F> optionals = new ArrayList<F>();
		ArrayList<F> invalids = new ArrayList<F>();

		for (int i = 0; i < previouschildren.length; i++) {
			F thischild = previouschildren[i];
			String key = multidimensionalchildhelper.generateKeyForObject(thischild);
			if (!compulsorychildren.containsKey(key))
				optionalsandinvalids.add(thischild);
		}

		// process optionals and invalid

		for (int i = 0; i < optionalsandinvalids.size(); i++) {
			F optionalorinvalid = optionalsandinvalids.get(i);
			boolean invalid = multidimensionalchildhelper.isInvalid(optionalorinvalid);
			if (invalid)
				invalids.add(optionalorinvalid);
			if (!invalid)
				optionals.add(optionalorinvalid);

		}

		// process invalids
		ArrayList<F> unconsolidatedinvalids = new ArrayList<F>();
		for (int i = 0; i < invalids.size(); i++) {
			F invalid = invalids.get(i);
			F potentialnewchild = invalid.deepcopy();
			String value = multidimensionalchildhelper.getKeyForConsolidation(potentialnewchild, object);
			if (value != null) {
				if (childrenbykey.get(value) != null) {
					F childtoconsolidateinto = childrenbykey.get(value);
					multidimensionalchildhelper.getConsolidator().accept(childtoconsolidateinto, potentialnewchild);
					logger.finer("          -> found object to consolidate into ");
				} else {
					allobjectstoinsert.add(potentialnewchild);
					childrenbykey.put(value, potentialnewchild);
					logger.finer("          -> adds value directly to consolidation");
				}
				// this invalid should be deleted
				allobjectstodelete.add(invalid);
			} else {
				// no consolidation
				unconsolidatedinvalids.add(invalid);
			}
		}
		// process duplicates, consolidates value
		
		for (int i=0;i<duplicatechildren.size();i++) {
			F thisduplicate = duplicatechildren.get(i);
			String keyforduplicate =  multidimensionalchildhelper.generateKeyForObject(thisduplicate);
			if (childrenbykey.get(keyforduplicate) != null) {
				F childtoconsolidateinto = childrenbykey.get(keyforduplicate);
				multidimensionalchildhelper.getConsolidator().accept(childtoconsolidateinto, thisduplicate);
				allobjectstodelete.add(thisduplicate);
				logger.finer("          -> found object to consolidate the duplicate into ");
			}
		}
		
		// Complete optionals with missing primary values

		for (int i = 0; i < optionals.size(); i++) {
			F thisoptional = optionals.get(i);
			ArrayList<F> missingforthisoptional = multidimensionalchildhelper.getOtherPrimaryelements(thisoptional,
					childrenbykey);
			for (int j = 0; j < missingforthisoptional.size(); j++) {
				F thismissing = missingforthisoptional.get(j);
				allobjectstoinsert.add(thismissing);
				childrenbykey.put(multidimensionalchildhelper.generateKeyForObject(thismissing), thismissing);

			}
		}

		// insert missing elements -- first
		for (int i = 0; i < allobjectstoinsert.size(); i++)
			allobjectstoinsert.get(i).setmultidimensionparentidwithoutupdate(object.getId());

		F[] objectstoinsert = allobjectstoinsert.toArray(childobjectdefinition.generateArrayTemplate());
		logger.finer("   inserting " + (objectstoinsert != null ? objectstoinsert.length : "null") + " objects");
		if (objectstoinsert != null)
			for (int i = 0; i < objectstoinsert.length; i++)
				logger.finest("             " + multidimensionalchildhelper.generateKeyForObject(objectstoinsert[i]));
		if (objectstoinsert != null)
			if (objectstoinsert.length > 0) {
				objectstoinsert[0].getMassiveInsert().insert(objectstoinsert);
			}
		if (!blowsnotdelete) {
			F[] objectstodelete = allobjectstodelete.toArray(childobjectdefinition.generateArrayTemplate());
			if (objectstodelete != null)
				if (objectstodelete.length > 0) {
					objectstodelete[0].getMassiveDelete().delete(objectstodelete);
				}
		} else {
			if (allobjectstodelete.size() > 0) {
				StringBuffer error = new StringBuffer();
				error.append("While cleaning object " + object + " got invalid children :");
				for (int j = 0; j < allobjectstodelete.size(); j++) {
					if (j > 0)
						error.append(" ");
					error.append("Key=" + multidimensionalchildhelper.generateKeyForObject(allobjectstodelete.get(j)));

				}
				throw new RuntimeException(error.toString());
			}
		}
	}

	/**
	 * post processing when an object is inserted. Creates the required children. If
	 * needed, get data from a previous version
	 * 
	 * @param object the object to pre-process
	 */
	public void postprocStoredobjectInsert(E object) {
		MultidimensionchildHelper<F, E> multidimensionalchildhelper = this.casteddefinition.getHelper();
		multidimensionalchildhelper.setContext(object);
		logger.fine(
				" ---------------- start treating object for insert " + object.dropIdToString() + " ---------------- ");
		// if versioned, get previous version children if it exists
		F[] previouschildren = null;
		if (object instanceof VersionedInterface<?>) {
			@SuppressWarnings("unchecked")
			VersionedInterface<E> versionedproperty = (VersionedInterface<E>) object;
			E previousversion = versionedproperty.getpreviousversion();
			if (previousversion != null)
				previouschildren = this.casteddefinition.getChildren(previousversion.getId());
			logger.fine("      got " + (previouschildren != null ? previouschildren.length : "null")
					+ " children from previous version");
		}
		// get the blank objects

		ArrayList<F> blankobjects = multidimensionalchildhelper.generateObjectsForAllValueHelpers(object,
				childobjectdefinition);
		logger.fine("      blank objects created = " + blankobjects.size());
		HashMap<String, F> objectsperkey = new HashMap<String, F>();
		for (int i = 0; i < blankobjects.size(); i++) {
			F thisobject = blankobjects.get(i);
			String key = multidimensionalchildhelper.generateKeyForObject(thisobject);
			logger.finest("                  " + key);
			objectsperkey.put(key, thisobject);
		}

		if (previouschildren != null) {
			// replace children that are part of the list of blank objects

			for (int i = 0; i < previouschildren.length; i++) {
				F oldchild = previouschildren[i];
				String childkey = multidimensionalchildhelper.generateKeyForObject(oldchild);
				if (objectsperkey.get(childkey) != null) {
					F newchild = oldchild.deepcopy();
					objectsperkey.put(childkey, newchild);
					logger.finest("              Replace key " + childkey + " with previous version");
				}
			}

			// Process children that are not part of the list of blank objects

			for (int i = 0; i < previouschildren.length; i++) {
				F oldchild = previouschildren[i];
				String childkey = multidimensionalchildhelper.generateKeyForObject(oldchild);

				if (objectsperkey.get(childkey) == null) {
					F potentialnewchild = oldchild.deepcopy();
					logger.finer(" -- Managing child in previous version key = " + childkey);
					String value = multidimensionalchildhelper.getKeyForConsolidation(potentialnewchild, object);
					logger.finer("          -> found new key for consolidation = " + value);
					if (value != null)
						if (objectsperkey.get(value) != null) {
							F childtoconsolidateinto = objectsperkey.get(value);
							multidimensionalchildhelper.getConsolidator().accept(childtoconsolidateinto,
									potentialnewchild);
							logger.finer("          -> found object to consolidate into ");
						} else {
							objectsperkey.put(value, potentialnewchild);
							logger.finer("          -> adds value directly to consolidation");
						}
					if (value == null)
						logger.finer("              -> Discarding old data with key " + childkey);
				}
			}

		}
		// ------------ complete and insert the objects
		Iterator<F> allobjectstoinsertiterator = objectsperkey.values().iterator();
		ArrayList<F> allobjectstoinsert = new ArrayList<F>();
		while (allobjectstoinsertiterator.hasNext()) {
			F nextobject = allobjectstoinsertiterator.next();
			nextobject.setmultidimensionparentidwithoutupdate(object.getId());
			allobjectstoinsert.add(nextobject);

		}

		F[] objectstoinsert = allobjectstoinsert.toArray(childobjectdefinition.generateArrayTemplate());
		logger.finer("   inserting " + (objectstoinsert != null ? objectstoinsert.length : "null") + " objects");
		if (objectstoinsert != null)
			for (int i = 0; i < objectstoinsert.length; i++)
				logger.finest("             " + multidimensionalchildhelper.generateKeyForObject(objectstoinsert[i]));
		if (objectstoinsert != null)
			if (objectstoinsert.length > 0) {
				objectstoinsert[0].getMassiveInsert().insert(objectstoinsert);
			}
	}

}
