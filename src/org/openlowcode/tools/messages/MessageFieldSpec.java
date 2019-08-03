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
 * A MessageFieldSpec allows to define the type of payload. It is currently
 * used only for MessageArrays. A MessageFieldSpec contains a name and a MessageFieldType
 * @author Open Lowcode SAS
 * @see org.openlowcode.tools.messages.MessageArrayStart
 * @see org.openlowcode.tools.messages.MessageFieldType
 *
 */
public class MessageFieldSpec {
	private String name;
	private MessageFieldType type;

	/**
	 * @return the name of the message field spec
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type of the field
	 */
	public MessageFieldType getType() {
		return type;
	}

	/**
	 * @param name name (unique over an array)
	 * @param type type of the field spec
	 */
	public MessageFieldSpec(String name,MessageFieldType type) {
		this.name = name;
		this.type = type;
	}
	
	/**
	 * @param name name (unique over an array)
	 * @param typecode Each MessageFieldType has a code, this
	 * constructor is a facility to create the MessageFieldSpec
	 * by entering the code
	 */
	public MessageFieldSpec(String name,String typecode) {
		this.name = name;
		this.type = MessageFieldType.getType(typecode);
	}
	
	

	/**
	 * @param columnnumber index of the column (this is used to construct
	 * potential error message)
	 * @param payload a payload object.
	 */
	public void validatePayload(int columnnumber,Object payload) {
		type.validatePayload("Column "+name+" index="+columnnumber,payload);
	}

	/**
	 * @return the correct serialization of a null content for the object type
	 */
	public String generateNullContent() {
		return type.generateNullContent();
	}
}
