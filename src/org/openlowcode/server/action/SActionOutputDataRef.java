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
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.DataEltType;

/**
 * A reference to business data declared as output attribute for an
 * ActionExecution
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of data
 */
public class SActionOutputDataRef<E extends DataEltType> extends Named {
	private E type;
	private int order;

	/**
	 * @param name  this represents the action name
	 * @param type  type of the element
	 * @param order order of the element
	 */
	protected SActionOutputDataRef(String name, E type, int order) {
		super(name);
		this.type = type;
		this.order = order;

	}

	/**
	 * @return the type of the attribute
	 */
	public E getType() {
		return this.type;
	}

	/**
	 * @return the sequence of the output attribute for the ActionExecution
	 */
	public int getOrder() {
		return this.order;
	}

	/**
	 * writes a reference to this action output
	 * 
	 * @param writer the message writer
	 * @throws IOException if any problem is encountered writing the message
	 */
	public void writeReferenceToCML(MessageWriter writer) throws IOException {
		writer.startStructure("INLOUTPUT");
		writer.addStringField("NAM", this.getName());
		writer.addIntegerField("ORD", order);
		writer.addStringField("TYP", type.printType());
		writer.endStructure("INLOUTPUT");

	}
}
