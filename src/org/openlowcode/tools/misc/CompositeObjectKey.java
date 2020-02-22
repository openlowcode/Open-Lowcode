/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A composite key made of the DataObjectId of an object, and additional
 * attributes. This is used to create a classification inside a report tree
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object type of the data object id in the key
 */
public class CompositeObjectKey<E extends DataObject<E>> {
	private DataObjectId<E> objectid;
	private String[] additionalattributes;

	/**
	 * @param objectid
	 * @param additionalattributes
	 */
	public CompositeObjectKey(DataObjectId<E> objectid, String[] additionalattributes) {
		this.objectid = objectid;
		this.additionalattributes = additionalattributes;
	}

	@Override
	public String toString() {

		StringBuffer key = new StringBuffer();
		key.append((objectid != null ? objectid.toString() : "NULL").replaceAll("\\|", "\\|\\|"));
		key.append("|");
		if (additionalattributes != null)
			for (int i = 0; i < additionalattributes.length; i++) {
				key.append(
						(additionalattributes[i] != null ? additionalattributes[i] : "NULL").replace("\\|", "\\|\\|"));
			}
		return key.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof CompositeObjectKey))
			return false;
		return this.toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}

}
