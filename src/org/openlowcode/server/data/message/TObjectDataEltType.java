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
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.tools.structure.ObjectDataEltType;

/**
 * Definition of an element of a message types to a special data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class TObjectDataEltType<E extends DataObject<E>> extends ObjectDataEltType {
	private DataObjectDefinition<E> definition;

	/**
	 * creates the defintion of a types object data element type
	 * @param definition definition of the data object
	 */
	public TObjectDataEltType(DataObjectDefinition<E> definition) {
		this.definition = definition;
	}

	/**
	 * gets the object type
	 * @return object type
	 */
	public String getObjectType() {
		return definition.getName();
	}

	@Override
	public String getObjectNameForStructure() {
		return getObjectType();
	}

}
