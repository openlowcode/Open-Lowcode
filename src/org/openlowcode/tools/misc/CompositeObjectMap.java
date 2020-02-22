/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;


/**
 * A composite object map allows to order objects by a parent object id
 * and a number of classifiers, the classifiers being typically the value
 * of some fields of the object. This is used in the Smart Report, and
 * is also available for other actions, typically the ones for action
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CompositeObjectMap<E extends DataObject<E> & UniqueidentifiedInterface<E>,F extends DataObject<F> & UniqueidentifiedInterface<F>> {
		private HashMap<CompositeObjectKey<E>,ArrayList<F>> orderedobjectmap;
		private HashMap<DataObjectId<E>,ArrayList<F>> consolidatedordermap;
		private Function<F, DataObjectId<E>> parentidextractor;
		private Function<F, String>[] classifier;
	/**
	 * @param parentidextractor a function that gets the parent id from the object (expected to require no call to persistence layer)
	 * @param classifier a function to extract the value for each of the classifiers (expected to require no call to persistence layer)
	 */
	@SafeVarargs
	public CompositeObjectMap(Function<F,DataObjectId<E>> parentidextractor,Function<F,String>... classifier) {
		orderedobjectmap = new HashMap<CompositeObjectKey<E>,ArrayList<F> > ();
		consolidatedordermap = new HashMap<DataObjectId<E>,ArrayList<F>>();
		this.parentidextractor = parentidextractor;
		this.classifier = classifier;
	}
	/**
	 * @param objects the list of objects to classify. It is expected to be non null, though some of the
	 * classifier values may be null.
	 */
	public  void classifyObjects(F[] objects)  {
		if (objects!=null) for (int i=0;i<objects.length;i++) classifyObject(objects[i]);
	}
	/**
	 * @param object the object to classify. It is expected to be non null, though some of the
	 * classifier values may be null.
	 */
	public void classifyObject(F object)  {
		DataObjectId<E> parentid = parentidextractor.apply(object);
		String[] classvalue = new String[(classifier!=null?classifier.length:0)];
		if (classifier!=null) for (int i=0;i<classifier.length;i++) classvalue[i] = classifier[i].apply(object);
		CompositeObjectKey<E> key = new CompositeObjectKey<E>(parentid, classvalue);
		ArrayList<F> nodesalreadyclassified = orderedobjectmap.get(key);
		if (nodesalreadyclassified==null) {
			nodesalreadyclassified = new ArrayList<F>();
			orderedobjectmap.put(key, nodesalreadyclassified);
		}
		nodesalreadyclassified.add(object);
		ArrayList<F> consolidatednodesalreadyclassified = consolidatedordermap.get(parentid);
		if (consolidatednodesalreadyclassified==null) {
			consolidatednodesalreadyclassified = new ArrayList<F>();
			consolidatedordermap.put(parentid, consolidatednodesalreadyclassified);
		}
		consolidatednodesalreadyclassified.add(object);
	}
	
	/**
	 * gets all the objects for the given parent data object id
	 * 
	 * @param parentid id of the parent object
	 * @return the list of children objects for this parent
	 */
	public List<F> getObjectsForRootParentId(DataObjectId<E> parentid)  {
		return consolidatedordermap.get(parentid);
	}
	
	/**
	 * @param parentid parent id. This may be null
	 * @param classifier: it should have the number of values of the map, some values may be null
	 * @return a list if something exists, null else
	 */
	public List<F> getObjectsForValue(DataObjectId<E> parentid,String[]classifier)  {
		return orderedobjectmap.get(new CompositeObjectKey<E>(parentid,classifier));
	}
}
