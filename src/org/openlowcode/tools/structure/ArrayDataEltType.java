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
 * Type for ArrayDataElement
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 * @param <E> a data element type
 */
/**
 * @author demau
 *
 * @param <E>
 */
public class ArrayDataEltType<E extends DataEltType> extends DataEltType {
	private E payloadtype;
	public ArrayDataEltType(E payloadtype) {
		this.payloadtype = payloadtype;
	}
	@Override
	public String printType() {
		
		return "ARR/"+payloadtype.printType();
	}
	/**
	 * This class returns null for a normal ArrayDataEltType. It can
	 * be overridden by children classes to provide stronger typing
	 * @return
	 */
	public String getObjectName() {
		if (payloadtype instanceof ObjectDataEltType) 
			return ((ObjectDataEltType)payloadtype).getObjectNameForStructure();
		return null;

	}
	/**
	 * @return the payload type
	 */
	public E getPayloadType() {
		return this.payloadtype;
	}
}
