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

import java.math.BigDecimal;

/**
 * A field type to store a java big decimal. This type should be appropriate for most
 * business and scientific numbers.<br>
 * To use the type, just use the singleton.
 * @author Open Lowcode SAS
 *
 */
public class MessageFieldTypeDecimal extends MessageFieldType {
	private static final String DECIMAL_LETTER = "X";
	@Override
	public String getMessageFieldAcronym() {
		return DECIMAL_LETTER;
	}
	public static MessageFieldTypeDecimal singleton = new MessageFieldTypeDecimal();
	private MessageFieldTypeDecimal() {
		
	}
	@Override
	protected void validatePayload(String contextname,Object payload) {
		if (payload!=null) if (!(payload instanceof BigDecimal)) 
			throw new RuntimeException("Object for "+contextname+" is not a BigDecimal but "+payload.getClass().toString()+" - "+payload.toString());
		
	}
	@Override
	protected String generateNullContent() {
		return "X";
	}
}
