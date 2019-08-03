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
 * Gathers all fields that are transported as text in a CML Message.
 * 
 * @author Open Lowcode SAS
 *
 */
public abstract class MessageField<E extends MessageFieldType> extends MessageElement {
	protected final static String CONTENT_SEPARATOR = "=";
	protected final static String FIELD_SEPARATOR = ",";
	protected final static String STRUCTURE_SEPARATOR = ":";

	private String fieldname;

	public String getFieldName() {
		return fieldname;
	}

	/**
	 * Creates a new MessageField
	 * 
	 * @param fieldname the field name, assumed to be checked by the receiver for
	 *                  consistency.
	 */
	public MessageField(String fieldname) {
		this.fieldname = fieldname;
	}

	/**
	 * A field is serialized with
	 * <ul>
	 * <li>':' (column) if first field, ',' (comma) if second or later field</li>
	 * <li>the field name</li>
	 * <li>'=' (equal)</li>
	 * <li>payload (detailed in each subclass)</li>
	 * </ul>
	 * 
	 * @return serialization of the field
	 */
	@Override
	public String serialize(String padding, boolean firstattribute) {
		String fieldnamecontext = "Field name:" + this.getFieldName();
		if (firstattribute)
			return MessageField.STRUCTURE_SEPARATOR + this.getFieldName() + MessageField.CONTENT_SEPARATOR
					+ serializepayload(fieldnamecontext);
		return MessageField.FIELD_SEPARATOR + this.getFieldName() + MessageField.CONTENT_SEPARATOR
				+ serializepayload(fieldnamecontext);
	}

	/**
	 * @param contextstring a string that will be included in the exception in case
	 *                      of error
	 * @return a value in string, potentially multi-line, of the field payload
	 */
	public abstract String serializepayload(String contextstring);
}
