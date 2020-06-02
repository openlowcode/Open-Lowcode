/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Function;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * Utilities to transform object arrays into other objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.8
 *
 */
public class ObjectUtilities {
	/**
	 * generated an id array from an object array
	 * 
	 * @param objectarray the array of objects
	 * @return the array of ids
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E>> DataObjectId<E>[] generateIdTable(E[] objectarray) {
		if (objectarray == null)
			return null;
		@SuppressWarnings("unchecked")
		DataObjectId<E>[] idarray = (DataObjectId<E>[]) new DataObjectId<?>[objectarray.length];
		for (int i = 0; i < objectarray.length; i++)
			idarray[i] = objectarray[i].getId();
		return idarray;
	}

	/**
	 * Extract target objects from an array of origin objects and create an array of
	 * the target object
	 * 
	 * @param originarray     array of origin objects
	 * @param objectextractor extractor to get the target
	 * @return the array of target objects
	 */

	public static <
			E extends Object,
			F extends Object> ArrayList<F> extractObjectTable(E[] originarray, Function<E, F> objectextractor) {
		if (originarray == null)
			return null;
		ArrayList<F> returnarray = new ArrayList<F>();
		for (int i = 0; i < originarray.length; i++)
			returnarray.add(objectextractor.apply(originarray[i]));
		return returnarray;
	}

	/**
	 * Extract target objects from an array of origin objects and create an array of
	 * the target object
	 * 
	 * @param originarray     array of origin objects
	 * @param objectextractor extractor to get the target
	 * @return the array of target objects
	 */
	public static <E extends Object, F extends Object> ArrayList<F> extractUniqueObjectTable(
			E[] originarray,
			Function<E, F> objectextractor,
			Function<F, String> objectuniqueidextractor) {
		if (originarray == null)
			return null;
		
		HashSet<String> uniquekeys = new HashSet<String>();
		ArrayList<F> uniquevaluesarray = new ArrayList<F>();
		
		for (int i=0;i<originarray.length;i++) {
			E originobject = originarray[i];
			F targetobject = objectextractor.apply(originobject);
			String key = objectuniqueidextractor.apply(targetobject);
			boolean doesnotexist = uniquekeys.add(key);
			if (doesnotexist) {
				uniquevaluesarray.add(targetobject);
			}
			
		}
		return uniquevaluesarray;
	}

}
