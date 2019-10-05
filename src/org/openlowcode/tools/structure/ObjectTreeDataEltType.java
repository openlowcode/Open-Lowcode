/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

/**
 * a type of tree with object data elements
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class ObjectTreeDataEltType<E extends ObjectDataEltType> extends DataEltType {
	private E payloadtype;

	public ObjectTreeDataEltType(E payloadtype) {
		this.payloadtype = payloadtype;
	}

	@Override
	public String printType() {
		return "OBT/" + payloadtype.printType();
	}
}
