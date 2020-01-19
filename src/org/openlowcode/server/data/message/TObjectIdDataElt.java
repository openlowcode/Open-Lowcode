/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.message;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdInterface;

/**
 * a typed class of ObjecIdDataElt to allow easier manipulation on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class TObjectIdDataElt<E extends DataObject<E>>
		extends
		ObjectIdDataElt {

	private DataObjectId<E> objectid;

	/**
	 * creates a types object id element
	 * 
	 * @param name     name of the element
	 * @param objectid object id of the given type
	 */
	public TObjectIdDataElt(String name, DataObjectId<E> objectid) {
		super(name, new ObjectIdInterface() {

			@Override
			public String getId() {
				return objectid.getId();
			}

			@Override
			public String getObjectId() {
				return objectid.getObjectId();
			}

		});
		this.objectid = objectid;
	}

	/**
	 * gets the typed object id contained in this element
	 * 
	 * @return typed object id
	 */
	public DataObjectId<E> getTObjectId() {
		return this.objectid;
	}
}
