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

import org.openlowcode.tools.encrypt.EncrypterHolder;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * builds a text data element that is encrypted during the network transmission
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class EncryptedTextDataElt extends TextDataElt {
	private String payload;

	/**
	 * @param name    name of the element
	 * @param payload clear payload
	 */
	public EncryptedTextDataElt(String name, String payload) {

		super(name, EncrypterHolder.get().getEncrypter().encryptStringTwoWays(payload), new EncryptedTextDataEltType());
		this.payload = EncrypterHolder.get().getEncrypter().encryptStringTwoWays(payload);

	}

	public EncryptedTextDataElt(String name) {
		super(name, new EncryptedTextDataEltType());
		this.payload = "";
	}

	@Override
	public String defaultTextRepresentation() {

		return "[ENCRYPTED:" + this.getPayload() + "]";

	}

	public String getPayload() {
		if (this.payload.length() > 0)
			return EncrypterHolder.get().getEncrypter().decryptStringTwoWays(this.payload);
		return "";

	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addStringField("PLD", this.payload);

	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextStringField("PLD");

	}
}