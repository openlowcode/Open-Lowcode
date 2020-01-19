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
import org.openlowcode.server.data.properties.DataObjectMasterId;
import org.openlowcode.tools.structure.ObjectIdInterface;
import org.openlowcode.tools.structure.ObjectMasterIdDataElt;

/**
 * a typed class of ObjectMasterIdDataElt to allow easier manipulation on the
 * server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class TObjectMasterIdDataElt<E extends DataObject<E>>
		extends
		ObjectMasterIdDataElt {

	private DataObjectMasterId<E> masterid;

	/**
	 * creates a typed master id data element
	 * 
	 * @param name     name of the data element
	 * @param masterid master id of the correct type
	 */
	public TObjectMasterIdDataElt(String name, DataObjectMasterId<E> masterid) {
		super(name, new ObjectIdInterface() {

			@Override
			public String getId() {
				return masterid.getId();
			}

			@Override
			public String getObjectId() {
				return masterid.getObjectId();
			}

		});
		this.masterid = masterid;
	}

	/**
	 * @return gets the typed master id
	 */
	public DataObjectMasterId<E> getTMasterId() {
		return this.masterid;
	}
}
