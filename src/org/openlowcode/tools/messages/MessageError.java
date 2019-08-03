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
 * a OLc Message Error can be sent at any point of a message transmission. When
 * a message error is transmitted by one party, the current transmission should
 * be aborted. This means the request message to the server should be
 * interrupted, and the client should stop reading any answer.
 * 
 * It is expected data rollback will be performed as much as possible from the
 * server side (though this is not guaranteed). However, it is recommended the
 * client queries again the data from the server.
 * 
 * MessageError is serialized as<ul>
 * <li>#</li>
 * <li>999</li>
 * <li>#</li>
 * <li>Error message</li>
 * </ul>
 * where 999 is an example of error code
 * @author Open Lowcode SAS
 *
 */
public class MessageError extends MessageElement {
	private int errorcode;
	private String errormessage;

	@Override
	public String serialize(String padding, boolean firstattribute) {
		StringBuffer messageerror = new StringBuffer();
		messageerror.append(padding);
		messageerror.append('#');
		messageerror.append(errorcode);
		messageerror.append(':');
		messageerror.append(MessageStringField.serializeStringPayload(errormessage, null));
		messageerror.append('#');
		messageerror.append('\n');
		return messageerror.toString();
	}

	/**
	 * Creates a message error, with an error code and an error message.
	 * @param errorcode an integer error code. If error code is not used,
	 * it is recommended to send 0
	 * @param errormessage the plain text error message. It will likely be logged
	 * or displayed by the other party, so should contain understandable content.
	 */
	public MessageError(int errorcode, String errormessage) {
		this.errorcode = errorcode;
		this.errormessage = errormessage;
	}

	/**
	 * @return the error code (if 0, this should mean error code
	 * is not significant)
	 */
	public int getErrorcode() {
		return errorcode;
	}

	/**
	 * @return the error message (plain text explanation for the erros)
	 */
	public String getErrormessage() {
		return errormessage;
	}

}
