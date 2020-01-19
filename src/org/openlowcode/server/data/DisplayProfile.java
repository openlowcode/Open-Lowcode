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

/**
 * A display profile provides a filter on the attributes to shown
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
public class DisplayProfile<E extends DataObject<E>>
		extends
		Named {
	private DataObjectDefinition<E> definition;

	protected DisplayProfile(String name, DataObjectDefinition<E> definition) {
		super(name);
		this.definition = definition;
	}

	@Override
	public String toString() {

		return "[" + definition.getName() + "," + this.getName() + "]";
	}

}
