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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * A message array line is providing a payload (potentially null) for all the
 * columns declared in the MessageArrayStart
 * 
 * @author Open Lowcode SAS
 * @see org.openlowcode.tools.messages.MessageArrayStart
 */
public class MessageArrayLine extends MessageElement {
	private ArrayList<Object> payload;
	private MessageArrayStart messagearraystart;
	private static final String LINE_STARTER = "(";
	private static final String LINE_ENDER = ")";
	private static final String OBJECT_SEPARATOR = ",";

	/**
	 * @param messagearraystart the fully configured MessageArrayStart
	 * @param payload
	 * @see org.openlowcode.tools.messages.MessageArrayStart
	 */
	public MessageArrayLine(MessageArrayStart messagearraystart, Object[] payload) {
		this.payload = new ArrayList<Object>(Arrays.asList(payload));
		messagearraystart.validateArrayLine(this);
	}

	protected MessageArrayLine(Object[] payload) {
		this.payload = new ArrayList<Object>(Arrays.asList(payload));
	}

	@Override
	public String serialize(String padding, boolean firstattribute) {
		StringBuffer string = new StringBuffer();
		string.append('\n');
		string.append(padding);
		string.append(LINE_STARTER);
		for (int i = 0; i < payload.size(); i++) {
			if (i > 0)
				string.append(OBJECT_SEPARATOR);
			string.append(printObject(messagearraystart.getFieldSpecAt(i), payload.get(i)));
		}
		string.append(LINE_ENDER);
		return string.toString();
	}

	private String printObject(MessageFieldSpec fieldspec, Object object) {
		if (object == null)
			return fieldspec.generateNullContent();
		if (object instanceof String)
			return MessageStringField.serializeStringPayload((String) object, "");
		if (object instanceof Integer)
			return MessageIntegerField.serialize(((Integer) (object)).intValue());
		if (object instanceof BigDecimal)
			return MessageDecimalField.serializeDecimal((BigDecimal) object);
		if (object instanceof Boolean)
			return MessageBooleanField.serializeBoolean(((Boolean) (object)).booleanValue());
		if (object instanceof Date)
			return MessageDateField.serializeDate(((Date) (object)));

		throw new RuntimeException("Object " + object.getClass() + " not supported in GML Compact Array");
	}

	/**
	 * @return the number of objects stored in the array line. It is guaranteed to
	 *         be always the same number of objects that defined in the
	 *         MessageArrayStart, though some elements may be null
	 */
	public int getObjectNumber() {
		return payload.size();
	}

	/**
	 * @param index
	 * @return the object for column indicated by index
	 */
	public Object getPayloadAt(int index) {
		return payload.get(index);
	}

	protected void setArrayStart(MessageArrayStart messagearraystart) {
		this.messagearraystart = messagearraystart;

	}

}
