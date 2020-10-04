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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object used as autolink
 * @param <F> the data object being referenced by the autolink
 */
public interface AutolinkobjectInterface<
		E extends DataObject<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & HasidInterface<F>>
		extends
		UniqueidentifiedInterface<E> {
	/**
	 * @return the id of the right object for the link
	 */
	public DataObjectId<F> getRgid();

	/**
	 * @return the id of the left object for the link
	 */
	public DataObjectId<F> getLfid();

	/**
	 * @param leftobjectid sets the id of the left object
	 */
	public void setleftobject(DataObjectId<F> leftobjectid);

	/**
	 * @param rightobjectid sets the id of the right object
	 */
	public void setrightobject(DataObjectId<F> rightobjectid);

	/**
	 * exchanges the left and the right fields. This is needed when processing
	 * symetric link
	 */
	public void exchangeleftandrightfields();
}
