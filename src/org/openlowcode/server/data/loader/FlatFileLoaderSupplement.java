/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * A flat file loader supplements will fill some data on the objects processes
 * by the flat file loader.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> the data object on which the loader will act
 */

public abstract class FlatFileLoaderSupplement<E extends DataObject<E> & UniqueidentifiedInterface<E>> {
	/**
	 * Allows the loader supplement to specify a criteria for selecting objects.
	 * 
	 * @return null if no criteria should be added for selecting objects
	 */
	public abstract FlatFileLoaderColumn.LinePreparationExtra<E> getSupplement();

	/**
	 * @param objectforprocessing object to process. The object has just been
	 *                            initialized as a blank.
	 */
	protected abstract void initializeNewObject(E objectforprocessing);

}
