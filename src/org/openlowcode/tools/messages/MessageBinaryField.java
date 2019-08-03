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

import java.util.Base64;

/**
 * a binary element contains a payload that will be transported from client to
 * server It is transported as pure binary content after a 'D' and then an
 * integer precising the size of the binary content in bytes and then a ':' and
 * then the binary content. e.g B1000:[BinaryContent length = 1000 encoded in
 * base64].When empty, just sent B0:;
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageBinaryField extends MessageField<MessageFieldTypeBinary> {
	private static Base64.Encoder base64encoder = Base64.getEncoder();
	private byte[] payload;
	private String filename;

	public MessageBinaryField(String fieldname) {
		super(fieldname);

	}

	/**
	 * @param fieldname   name of the field. It is recommended to check the field
	 *                    name when parsing the message to ensure no
	 *                    misunderstanding
	 * @param payloadfile the file payload
	 */
	public MessageBinaryField(String fieldname, SFile payloadfile) {
		super(fieldname);

		payload = payloadfile.getContent();
		this.filename = payloadfile.getFileName();
	}

	public MessageBinaryField(String attributename, byte[] binary, String filename) {
		super(attributename);
		this.payload = binary;
		this.filename = filename;
	}

	@Override
	public String serializepayload(String contextstring) {
		if (payload == null) {
			return "B0:";
		}
		if (payload.length == 0) {
			return "B0:";
		}
		String base64payload = base64encoder.encodeToString(payload);

		String filenameencoded = MessageStringField.serializeStringPayload(filename, null);
		return "B" + base64payload.length() + ":" + filenameencoded + ":" + base64payload;

	}

	public SFile getFieldContent() {
		return new SFile(filename, payload);
	}

}
