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
 * a simple field storing a real boolean (value true or false)
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageBooleanField extends MessageField<MessageFieldTypeBoolean> {
	private boolean payload;
	private static final String TRUE = "T";
	private static final String FALSE = "F";

	/**
	 * @param fieldname
	 * @param payload
	 */
	public MessageBooleanField(String fieldname, boolean payload) {
		super(fieldname);
		this.payload = payload;
	}

	/**
	 * @param b
	 * @return
	 */
	public static String serializeBoolean(boolean b) {
		if (b)
			return TRUE;
		return FALSE;
	}

	@Override
	public String serializepayload(String contextstring) {
		return serializeBoolean(payload);
	}

	public boolean getFieldContent() {
		return payload;
	}

}
