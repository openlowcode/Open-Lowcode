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
 * This class is used as a character string content inside a MessageArray.
 * <br>To use the class, just use the singleton.
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeString extends MessageFieldType {
	private static final String STRING_LETTER = "S";
	@Override
	public String getMessageFieldAcronym() {
		return STRING_LETTER;
	}
	public static MessageFieldTypeString singleton = new MessageFieldTypeString();
	private MessageFieldTypeString() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof String)) 
			throw new RuntimeException("Object for "+contextname+" is not a String but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		return "NS";
	}
}
