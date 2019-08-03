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
 * This type is used to define a field holding a binary file
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeBinary extends MessageFieldType {
	private static final String BINARY_LETTER = "B";
	@Override
	public String getMessageFieldAcronym() {
		return BINARY_LETTER;
	}
	public static MessageFieldTypeBinary singleton = new MessageFieldTypeBinary();
	private MessageFieldTypeBinary() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof SFile)) 
			throw new RuntimeException("Object for "+contextname+" is not a SFile but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		
		return "B0:";
	}
}
