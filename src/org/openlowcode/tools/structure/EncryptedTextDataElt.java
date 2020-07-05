/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;

/**
 * builds a text data element that is encrypted during the network transmission
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class EncryptedTextDataElt
		extends
		TextDataElt {

	private static Logger logger = Logger.getLogger(EncryptedTextDataElt.class.getName());

	private String payload;

	/**
	 * @param name    name of the element
	 * @param payload clear payload
	 */
	public EncryptedTextDataElt(String name, String payload) {

		super(name, new EncryptedTextDataEltType());
		this.payload = payload;

	}

	public EncryptedTextDataElt(String name) {
		super(name, new EncryptedTextDataEltType());
		this.payload = "";
	}

	@Override
	public String defaultTextRepresentation() {

		return "[ENCRYPTEDDATA]";

	}

	public String getPayload() {
		return payload;

	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		try {
			writer.addLongBinaryField("PLD", new SFile("PLD", writer.getAESCommunicator().zipandencrypt(payload)));
		} catch (Exception e) {
			String loggerstring = "Error in AES encryption " + e.getClass() + " - " + e.getMessage();
			logger.severe("Error in AES encryption " + e.getClass() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				if (i == 10)
					break;
				logger.severe("    at " + e.getStackTrace()[i]);
			}
			throw new RuntimeException(loggerstring);
		}
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		try {
		this.payload = reader.getAESCommunicator().decryptandunzip(reader.returnNextLargeBinary("PLD").getContent());
		} catch (Exception e) {
			String loggerstring = "Error in AES encryption " + e.getClass() + " - " + e.getMessage();
			logger.severe("Error in AES encryption " + e.getClass() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				if (i == 10)
					break;
				logger.severe("    at " + e.getStackTrace()[i]);
			}
			throw new RuntimeException(loggerstring);
		}

	}
}