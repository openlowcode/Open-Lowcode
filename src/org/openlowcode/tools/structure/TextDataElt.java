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

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * a simple element storing a text information
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TextDataElt extends SimpleDataElt {
	private String payload;

	/**
	 * creates a text data element
	 * 
	 * @param name    name of the element
	 * @param payload payload
	 */
	public TextDataElt(String name, String payload) {
		super(name, new TextDataEltType());
		this.payload = payload;

	}

	/**
	 * changes the payload of the element
	 * 
	 * @param payload the new payload
	 */
	public void changePayload(String payload) {
		this.payload = payload;

	}

	protected TextDataElt(String name, String payload, TextDataEltType type) {
		super(name, type);
		this.payload = payload;
	}

	/**
	 * creates a text element with no payload
	 * 
	 * @param name name of the element
	 */
	public TextDataElt(String name) {
		super(name, new TextDataEltType());
	}

	protected TextDataElt(String name, TextDataEltType type) {
		super(name, type);
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addStringField("PLD", this.payload);

	}

	/**
	 * @return the payload
	 */
	public String getPayload() {
		return this.payload;
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextStringField("PLD");

	}

	@Override
	public String defaultTextRepresentation() {
		return this.payload;
	}

	@Override
	public TextDataElt cloneElt() {
		return new TextDataElt(this.getName(), this.payload);
	}

	@Override
	public void forceContent(String constraintvalue) {
		if (constraintvalue != null) {
			this.payload = constraintvalue;
		}
		throw new RuntimeException("Constraint Value is null.");

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof TextDataElt))
			return false;
		TextDataElt parseddataelt = (TextDataElt) other;
		return (this.payload.equals(parseddataelt.payload));
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeString.singleton);
	}

	@Override
	protected Object getMessageArrayValue() {
		return this.payload;
	}
}
