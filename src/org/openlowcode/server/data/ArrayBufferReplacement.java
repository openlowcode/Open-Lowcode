/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.ArrayList;

import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A static helper to replace objects by the objects in trigger buffer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ArrayBufferReplacement {
	/**
	 * replaces objects in the original array by objects in the trigger buffer if
	 * they exist
	 * 
	 * @param originalarray
	 * @return array with replacements
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E>> E[] replaceBufferDataInArray(E[] originalarray) {
		if (originalarray == null)
			return null;
		ArrayList<E> newtable = new ArrayList<E>();
		for (int i = 0; i < originalarray.length; i++) {
			E objectone = originalarray[i];
			E objectoneinbuffer = OLcServer.getServer().getObjectInTriggerUpdateBuffer(objectone.getId());
			if (objectoneinbuffer != null)
				objectone = objectoneinbuffer;
			newtable.add(objectone);
		}
		return newtable.toArray(originalarray);
	}

	/**
	 * replaces objects in the original two objects array by objects in the trigger
	 * buffer if they exist
	 * 
	 * @param originaltwoobjectsarray
	 * @return two object arrays with replacements
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & UniqueidentifiedInterface<F>> TwoDataObjects<
					E, F>[] replaceBufferDataInTwoObjectsArray(TwoDataObjects<E, F>[] originaltwoobjectsarray) {
		if (originaltwoobjectsarray == null)
			return null;
		ArrayList<TwoDataObjects<E, F>> newtable = new ArrayList<TwoDataObjects<E, F>>();
		for (int i = 0; i < originaltwoobjectsarray.length; i++) {
			TwoDataObjects<E, F> oldtwoobjects = originaltwoobjectsarray[i];
			E objectone = oldtwoobjects.getObjectOne();
			F objecttwo = oldtwoobjects.getObjectTwo();
			E objectoneinbuffer = OLcServer.getServer().getObjectInTriggerUpdateBuffer(objectone.getId());
			F objecttwoinbuffer = OLcServer.getServer().getObjectInTriggerUpdateBuffer(objecttwo.getId());
			if (objectoneinbuffer != null)
				objectone = objectoneinbuffer;
			if (objecttwoinbuffer != null)
				objecttwo = objecttwoinbuffer;
			newtable.add(new TwoDataObjects<E, F>(objectone, objecttwo));
		}
		return newtable.toArray(originaltwoobjectsarray);

	}
}
