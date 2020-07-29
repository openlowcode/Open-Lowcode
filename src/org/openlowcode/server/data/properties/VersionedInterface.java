/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.function.Consumer;

import org.openlowcode.server.data.DataObject;

/**
 * the interface all versioned objects comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public interface VersionedInterface<E extends DataObject<E>>
		extends
		UniqueidentifiedInterface<E> {
	/**
	 * @return gets the last version indicator 'Y' or 'N'
	 */
	public String getLastversion();

	/**
	 * @return the version
	 */
	public String getVersion();

	/**
	 * @return the master id
	 */
	public DataObjectMasterId<E> getMasterid();

	/**
	 * initializes the version for objects created before the versioned property was
	 * added. Triggered automatically by a migrator
	 */
	public void initversion();

	/**
	 * @return a function to perform a massive init version
	 */
	public MassiveInitversion<E> getMassiveInitversion();

	/**
	 * interface for an performing massive init version for an object. This is used
	 * for the migrator to initiate version
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> data object
	 */
	public interface MassiveInitversion<E extends DataObject<E>> {
		/**
		 * performs init version on an object batch
		 * 
		 * @param objectbatch object batcj
		 */
		public void initversion(E[] objectbatch);
	}

	/**
	 * revises the latest version of an object
	 * @param consumer a consumer to execute before persistence of the new version (optional)
	 * 
	 * @return the new version created
	 */

	public E revise(Consumer<E> consumer);

	/**
	 * @return the previous version if it exists
	 */
	public E getpreviousversion();

	/**
	 * @return a function allowing massive revise of the type
	 */
	public MassiveRevise<E> getMassiveRevise();

	/**
	 * This function allows the revise of a group of objects of the type,
	 * considering all business rules. This is used for other property massive
	 * updates that may depend on it
	 * 
	 * @param <E> the object type
	 */
	public interface MassiveRevise<E extends DataObject<E>> {
		
		/**
		 * @param objectbatch batch of objects
		 * @param consumer consumer to execute before persistence of a new version (optional)
		 * @return the revised objects
		 */
		public E[] revise(E[] objectbatch,Consumer<E> consumer);
	}

	/**
	 * This function allows the massive selection of last version of object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> the object type
	 */
	public interface MassiveGetlastversion<E extends DataObject<E>> {

		/**
		 * @param masteridbatch the list of master ids on which to query
		 * @return the corresponding list of object
		 */
		public E[] getlastversion(DataObjectMasterId<E>[] masteridbatch);
	}

	/**
	 * @return a function allowing massive get last version of this object
	 */
	public MassiveGetlastversion<E> getMassiveGetlastversion();

}
