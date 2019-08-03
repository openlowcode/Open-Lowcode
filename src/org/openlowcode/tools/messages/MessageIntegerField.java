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
 * This message type allows to specify a field holding a java Integer.
 * It is possible for the field payload to be null.
 * @author Open Lowcode SAS
 *
 */
public class MessageIntegerField extends MessageField<MessageFieldTypeInteger> {

	private int payload;
	private MessageIntegerField(String fieldname,int payload) {
		super(fieldname);
		this.payload = payload;
	}

	public static  String serialize(int value) {
		return ""+value;
	}
	
	@Override
	public String serializepayload(String contextstring) {
		return serialize(payload);
	}
	public static MessageIntegerField getCSPMessageIntegerField(String fieldname,int payload) {
		return new MessageIntegerField(fieldname,payload);
	}
	public int getFieldContent() {
		return payload;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+this.getFieldName()+":"+this.payload+"]";
	}
	
}
