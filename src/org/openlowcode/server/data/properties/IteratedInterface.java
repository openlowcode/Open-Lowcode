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

import org.openlowcode.server.data.DataObject;

/**
 * the interface all iterated objects comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public interface IteratedInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
	/**
	 * when archiving this iteration, a copy of the current data is stored and new
	 * iteration index for current data is generated
	 * 
	 * @return the new iteration index for current data
	 */
	public Integer archivethisiteration(/* discarded - OBJECTTOARCHIVE */);

	/**
	 * gets the iteration number for this record
	 * 
	 * @return iteration number
	 */
	public Integer getIteration();

	/**
	 * sets the new update note for the current iteration. This is stored on each
	 * object iteration and allows to understand context of updates
	 * 
	 * @param updatenote new update note
	 */
	public void setupdatenote(String updatenote);
}
