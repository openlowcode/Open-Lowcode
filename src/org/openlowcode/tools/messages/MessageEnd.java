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
 * Signals the end of a message. Typically, during communications, the receiving
 * party is assumed to be able to conduct a transaction on its side based on the
 * content of a single message.
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageEnd extends MessageElement {

	private static String MESSAGE_END = "}";

	@Override
	public String serialize(String padding, boolean firstattribute) {
		// TODO Auto-generated method stub
		return "\n" + MESSAGE_END + "\n";
	}

	/**
	 * Creates a message end class
	 */
	public MessageEnd() {

	}
}
