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
 * A field to store an Integer, that can potentially be null.<br>
 * To use the field type, just use the singleton.
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeInteger extends MessageFieldType {
	private static final String INTEGER_LETTER = "I";
	@Override
	public String getMessageFieldAcronym() {
		return INTEGER_LETTER;
	}
	public static MessageFieldTypeInteger singleton = new MessageFieldTypeInteger();
	private MessageFieldTypeInteger() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof Integer)) 
			throw new RuntimeException("Object for "+contextname+" is not a Integer but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		throw new RuntimeException("Null Value not supported");
	}
}
