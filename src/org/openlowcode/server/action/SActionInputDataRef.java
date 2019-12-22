/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.DataEltType;


/**
 * A reference to business data declared as input attribute for an ActionExecution
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of data element
 */
public class SActionInputDataRef<E extends DataEltType> extends Named {
	private E type;
	private int order;
	/**
	 * @param name name of the attribute
	 * @param type type of the attribute
	 * @param order sequence in the list of attributes of the ActionExecution
	 */
	protected SActionInputDataRef(String name,E type,int order) {
		super(name);
		this.type = type;
		this.order =order;
	}
	/**
	 * @return the type of the attribute
	 */
	public E getType() {
		return this.type;
	}
	/**
	 * @return the sequence of the input attribute for the ActionExecution
	 */
	public int getOrder() {
		return this.order;
	}
}
