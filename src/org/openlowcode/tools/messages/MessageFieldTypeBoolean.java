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
 * A field to store a 'technical' binary with only true
 * or false values allowed. This should be typically used
 * for logic inside the messaging or technical flag, but not for business
 * boolean that can typically have as values true or false or unset.<br>
 * To use this field type, just use the singleton
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeBoolean extends MessageFieldType {
	private static final String BOOLEAN_LETTER = "O";
	@Override
	public String getMessageFieldAcronym() {
		return BOOLEAN_LETTER;
	}
	public static MessageFieldTypeBoolean singleton = new MessageFieldTypeBoolean();
	private MessageFieldTypeBoolean() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof Boolean)) 
			throw new RuntimeException("Object for "+contextname+" is not a boolean but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		throw new RuntimeException("Null Value not supported");
	}
}
