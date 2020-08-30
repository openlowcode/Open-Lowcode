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
	 * @param parentpayload pâyload of the parent object
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
	 * post processing when an object is inserted. Creates the required children. If
	 * needed, get data from a previous version
	 * 
	 * @param object the object to pre-process
	 */
	public void postprocStoredobjectInsert(E object) {
		MultidimensionchildHelper<F, E> multidimensionalchildhelper = this.casteddefinition.getHelper();
		multidimensionalchildhelper.setContext(object);

		// if versioned, get previous version children if it exists
		F[] previouschildren = null;
		if (object instanceof VersionedInterface<?>) {
			@SuppressWarnings("unchecked")
			VersionedInterface<E> versionedproperty = (VersionedInterface<E>) object;
			E previousversion = versionedproperty.getpreviousversion();
			if (previousversion != null)
				previouschildren = this.casteddefinition.getChildren(previousversion.getId());
		}
		// get the blank objects

		ArrayList<F> blankobjects = multidimensionalchildhelper.generateObjectsForAllValueHelpers(object,
				childobjectdefinition);
		HashMap<String, F> objectsperkey = new HashMap<String, F>();
		for (int i = 0; i < blankobjects.size(); i++)
			objectsperkey.put(multidimensionalchildhelper.generateKeyForObject(blankobjects.get(i)),
					blankobjects.get(i));

		if (previouschildren != null) {
			// replace children that are part of the list of blank objects

			for (int i = 0; i < previouschildren.length; i++) {
				F oldchild = previouschildren[i];
				String childkey = multidimensionalchildhelper.generateKeyForObject(oldchild);
				if (objectsperkey.get(childkey) != null) {
					F newchild = oldchild.deepcopy();
					objectsperkey.put(childkey, newchild);
				}
			}

			// Process children that are not part of the list of blank objects

			for (int i = 0; i < previouschildren.length; i++) {
				F oldchild = previouschildren[i];
				String childkey = multidimensionalchildhelper.generateKeyForObject(oldchild);

				if (objectsperkey.get(childkey) == null) {
					F potentialnewchild = oldchild.deepcopy();
					logger.severe(" -- Managing child in previous version key = "+childkey);
					String value = multidimensionalchildhelper.getKeyForConsolidation(potentialnewchild, object);
					logger.severe("          -> found new key for consolidation = "+value);
					if (value != null)
						if (objectsperkey.get(value) != null) {
							F childtoconsolidateinto = objectsperkey.get(value);
							multidimensionalchildhelper.getConsolidator().accept(childtoconsolidateinto,
									potentialnewchild);
							logger.severe("          -> found object to consolidate into ");
						} else {
							objectsperkey.put(value, potentialnewchild);
							logger.severe("          -> adds value directly to consolidation");
						}
					if (value == null)
						logger.severe ("              -> Discarding old data with key " + childkey);
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
		if (objectstoinsert != null)
			if (objectstoinsert.length > 0) {
				objectstoinsert[0].getMassiveInsert().insert(objectstoinsert);
			}
	}

}
