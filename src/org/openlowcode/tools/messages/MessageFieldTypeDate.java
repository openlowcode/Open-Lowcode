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

import java.util.Date;

/**
 * The message field type date stores a java simple date.typically, dates are transported
 * as local time, and assume client and server are on the same timezone.<br>
 * To use this MessageFieldType, just reference the singleton.
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeDate extends MessageFieldType {
	private static final String DATE_LETTER = "D";
	@Override
	public String getMessageFieldAcronym() {
		return DATE_LETTER;
	}
	public static MessageFieldTypeDate singleton = new MessageFieldTypeDate();
	private MessageFieldTypeDate() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof Date)) 
			throw new RuntimeException("Object for "+contextname+" is not a Date but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		return "D";
	}
}
