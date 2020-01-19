/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
 * A key including the data object id and some additional attributes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class CompositeObjectKey<E extends DataObject<E>> {
	private DataObjectId<E> objectid;
	private String[] additionalattributes;

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
