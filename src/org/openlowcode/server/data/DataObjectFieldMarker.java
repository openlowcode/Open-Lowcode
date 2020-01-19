/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedInterface;

/**
 * this class allows to designate a field of an object in a GUI. As an example,
 * it can be used to hide a field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the definition of the object the field is a member of
 */
public class DataObjectFieldMarker<E extends DataObject<E>>
		extends
		Named
		implements
		NamedInterface {
	private DataObjectDefinition<E> objectdef;

	/**
	 * creates a field marker of the data object
	 * 
	 * @param objectdef definition of the data object
	 * @param fieldname name of the field
	 */
	protected DataObjectFieldMarker(DataObjectDefinition<E> objectdef, String fieldname) {
		super(fieldname);
		this.objectdef = objectdef;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	@Override
	public int hashCode() {
		return (objectdef.getName() + "/" + this.getName()).hashCode();
	}

}
