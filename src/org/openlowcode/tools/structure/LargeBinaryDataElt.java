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
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;

/**
 * a data element to transport a large binary, typically a file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LargeBinaryDataElt extends SimpleDataElt {
	public LargeBinaryDataElt(String name) {
		super(name, new LargeBinaryDataEltType());
	}

	/**
	 * @param name    name of the element
	 * @param payload file
	 */
	public LargeBinaryDataElt(String name, SFile payload) {
		super(name, new LargeBinaryDataEltType());
		this.payload = payload;
	}

	/**
	 * @return gets payload
	 */
	public SFile getPayload() {
		return payload;
	}

	private SFile payload;

	@Override
	public SimpleDataElt cloneElt() {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addLongBinaryField("PLD", payload);

	}

	@Override
	public String defaultTextRepresentation() {

		return "#BINARYCONTENT#";
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextLargeBinary("PLD");

	}

	@Override
	public void forceContent(String constraintvalue) {
		throw new RuntimeException("not yet implemented");

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof LargeBinaryDataElt))
			return false;
		LargeBinaryDataElt parseddataelt = (LargeBinaryDataElt) other;
		return (this.payload.equals(parseddataelt.payload));
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		throw new RuntimeException("Large Binary Type not supported in compact array");

	}

	@Override
	protected Object getMessageArrayValue() {
		throw new RuntimeException("Not yet implemented");
	}
}
