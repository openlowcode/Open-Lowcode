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
 * The message element starting a message. A message is a list of
 * message elements that constitute an input for the other party
 * to take an action on.<br>
 * @author Open Lowcode SAS
 *
 */
public class MessageStart extends MessageElement {
	private static String MESSAGE_START="{";
	
	@Override
	public String serialize(String padding,boolean firstattribute) {
		// TODO Auto-generated method stub
		return MESSAGE_START;
	}
	
	public MessageStart() {
		
	}
	
}
