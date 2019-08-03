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
 * A field holding a string information
 * @author Open Lowcode SAS
 *
 */
public class MessageStringField extends MessageField<MessageFieldTypeString> {
public static final char STRING_LIMITER = '"';
public static final String NULL_STRING = "NS";
private String fieldcontent;
	


	/**
	 * @param fieldname name of the field (to be checked at parsing time)
	 * @param fieldcontent payload (java string)
	 */
	public MessageStringField(String fieldname, String fieldcontent) {
		super(fieldname);
		this.fieldcontent = fieldcontent;
	}


	/**
	 * @return the payload of the String Field (may be null)
	 */
	public String getFieldcontent() {
		return fieldcontent;
	}


	@Override
	public String serializepayload(String contextstring) {
		return serializeStringPayload(fieldcontent,contextstring);
	}
	/**
	 * Will serialize a String, managing the string delimiters and escape characters.
	 * Strings are delimited by double quote (") and double quote inside the strings
	 * are escaped as double double quoted ("")
	 * @param content the string to serialize
	 * @param contextstring context of calling this helper function (this is used
	 * for providing context to a potential excception
	 * @return
	 */
	public static String serializeStringPayload(String content,String contextstring) {
		StringBuffer buffer = new StringBuffer();
		if (content == null)  {
			buffer.append(NULL_STRING);
			}
		else {
		
		buffer.append(STRING_LIMITER);
		for (int i=0;i<content.length();i++) {
			char currentchar = content.charAt(i);
			if (currentchar == STRING_LIMITER) {
				buffer.append(STRING_LIMITER);
				buffer.append(STRING_LIMITER);
			} else {
				buffer.append(currentchar);
			}
		}
		buffer.append(STRING_LIMITER);
		}
		return buffer.toString();
	}
}
