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

import java.io.IOException;

import org.openlowcode.tools.messages.MessageWriter;

import org.openlowcode.tools.structure.DataEltType;

/**
 * this object allows setting to null an action input
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data element
 */
public class SNullActionInputDataRef<E extends DataEltType> extends SActionInputDataRef<E> {

	/**
	 * Creates a null action input data reference
	 * 
	 * @param name  name of the action
	 * @param type  type of data
	 * @param order order
	 */
	protected SNullActionInputDataRef(String name, E type, int order) {
		super(name, type, order);

	}

	/**
	 * writes the reference to the action reference
	 * 
	 * @param writer message writer
	 * @throws IOException if anything bad happens
	 */
	public void WriteToCDL(MessageWriter writer) throws IOException {
		writer.startStructure("ACTIONDATA");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		writer.addStringField("PTH", null);
		writer.endStructure("ACTIONDATA");
	}

}
