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

import java.util.HashMap;

/**
 * A message field type represent a type of data
 * supported for sending inside a message field or a
 * message array
 * @author Open Lowcode
 *
 */
public abstract class MessageFieldType {
	public abstract String getMessageFieldAcronym();

	private static HashMap<String,MessageFieldType> typespercode;
	
	/**
	 * will return the type for the code, or an exception if the code is not existing
	 * @param code the code for the type
	 * @return
	 */
	public static MessageFieldType getType(String code) {
			if (typespercode==null) {
				MessageFieldType[] alltypes = new MessageFieldType[] {
						MessageFieldTypeBinary.singleton,
						MessageFieldTypeBoolean.singleton,
						MessageFieldTypeDate.singleton,
						MessageFieldTypeDecimal.singleton,
						MessageFieldTypeInteger.singleton,
						MessageFieldTypeString.singleton};
	
				HashMap<String,MessageFieldType> candidatetypespercode = new HashMap<String,MessageFieldType>();
				for (int i=0;i<alltypes.length;i++) {
					candidatetypespercode.put(
							alltypes[i].getMessageFieldAcronym(),
							alltypes[i]);
				}
				typespercode = candidatetypespercode;
			}
			MessageFieldType typeforcode = typespercode.get(code);
			if (typeforcode!=null) return typeforcode;
		throw new RuntimeException("Code "+code+" does not correspond to a registered type.");
	}


	/**
	 * each message field type should be able to validate is the payload is correct.
	 * @param objectcontext context of call (used for exception messages for traceability)
	 * @param payload the payload to test.
	 */
	protected abstract void validatePayload(String objectcontext,Object payload);


	/**
	 * Null content depending on the type of objet is represented differently
	 * in the OLc Message manguage. This method will return for each type the way to 
	 * represent the nullcontent as payload
	 * @return
	 */
	protected abstract String generateNullContent();
}
