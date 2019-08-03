/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

/**
 * A MessageEndStructure element is closing a structure element. During parsing,
 * the name of the structure being closed is indicated, and is assumed to be
 * checked by the receiving party.
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageEndStructure extends MessageElement {
	private static String MESSAGE_END_STRUCTURE = "]";
	private String name;

	@Override
	public String serialize(String padding, boolean firstattribute) {
		// TODO Auto-generated method stub
		return "\n" + padding + " " + MESSAGE_END_STRUCTURE; // one more space due to path implementation
	}

	public String serializesameline() {
		return MESSAGE_END_STRUCTURE;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public MessageEndStructure() {

	}
}
