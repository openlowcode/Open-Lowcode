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
import org.openlowcode.server.data.properties.UniqueidentifiedInterface.MassiveDelete;

/**
 * the interface all versioned objects comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public interface VersionedInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
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
	 * revises the latest version of an object
	 * 
	 * @return the new version created
	 */
	public E revise();

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
		public E[] revise(E[] objectbatch);
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
