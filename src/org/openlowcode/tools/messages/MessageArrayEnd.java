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
 * This message is used at the end of a compact array. Its graphical
 * representation is two closing brackets ')'
 * 
 * @author Open Lowcode SAP
 *
 */
public class MessageArrayEnd extends MessageElement {
	private String name;

	/**
	 * @return the name of the compact array
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name sets or change the name of the compact array.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * creates a new message array end. The MessageWriter will automatically add the
	 * message array name.
	 */
	public MessageArrayEnd() {

	}

	private static final String ARRAY_END_STRUCTURE = ")";

	@Override
	public String serialize(String padding, boolean firstattribute) {
		return " " + ARRAY_END_STRUCTURE + ARRAY_END_STRUCTURE; // one more space due to path implementation
	}

}
