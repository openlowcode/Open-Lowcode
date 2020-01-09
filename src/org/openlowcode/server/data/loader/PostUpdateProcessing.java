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

/**
 * This interface allows to perform some operations after the common update has
 * been performed for data loading
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the post update processing is
 */
@FunctionalInterface
public interface PostUpdateProcessing<E extends DataObject<E>> {
	/**
	 * performs some processing after object insert or update
	 * 
	 * @param object the object to post proceess
	 */
	public void postupdateprocess(E object);
}
