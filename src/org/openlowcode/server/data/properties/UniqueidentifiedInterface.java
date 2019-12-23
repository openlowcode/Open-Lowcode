/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.server.data.DataObject;

/**
 * 
 * The interface all objects with Uniqueidentified property comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public interface UniqueidentifiedInterface<E extends DataObject<E>> extends StoredobjectInterface<E> {
	/**
	 * @return the unique id of the object
	 */
	public DataObjectId<E> getId();

	/**
	 * persists the data of this object
	 */
	public void update();

	/**
	 * remove the dataobject from the persistence layer
	 */
	public void delete();

	/**
	 * @return a function allowing massive update of the type
	 */
	public MassiveUpdate<E> getMassiveUpdate();

	/**
	 * This function allows the update of a group of objects of the type,
	 * considering all business rules. This is used for batch processing.
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 * @param <E> the object type
	 */
	public interface MassiveUpdate<E extends DataObject<E>> {
		/**
		 * performs a batch update of the objects provided
		 * @param objectbatch batch of objects to update
		 */
		public void update(E[] objectbatch);
	}

	/**
	 * This function allows the delete of a group of objects of the type,
	 * considering all business rules. This is used for batch processing.
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> the object type
	 */
	public interface MassiveDelete<E extends DataObject<E>> {
		/**
		 * performs a batch delete of the objects provided
		 * @param objectbatch batch of objects to delete
		 */
		public void delete(E[] objectbatch);
	}

	/**
	 * @return a function allowing massive delete of the type
	 */
	public MassiveDelete<E> getMassiveDelete();

}
