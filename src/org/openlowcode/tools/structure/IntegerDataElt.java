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
import org.openlowcode.tools.messages.MessageFieldTypeInteger;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * a data element storing an integer payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IntegerDataElt extends SimpleDataElt {
	private Integer payload;

	/**
	 * creates an integer data element with empty payload
	 * 
	 * @param name name of the element
	 */
	public IntegerDataElt(String name) {
		super(name, new IntegerDataEltType());

	}

	/**
	 * @param name    name of the element
	 * @param payload integer payload (can not be null)
	 */
	public IntegerDataElt(String name, Integer payload) {
		super(name, new IntegerDataEltType());
		if (payload==null) throw new RuntimeException("For Field "+name+", null integer value is provided, but it is not valid");
		this.payload = payload;
		

	}

	protected void updateContent(Integer payload) {
		this.payload = payload;
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addIntegerField("PLD", payload);

	}

	@Override
	protected Object getMessageArrayValue() {
		return payload;
	}

	@Override
	public String defaultTextRepresentation() {
		if (payload == null)
			return "";
		return payload.toString();
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextIntegerField("PLD");

	}

	public Integer getPayload() {
		return payload;
	}

	@Override
	public SimpleDataElt cloneElt() {
		return new IntegerDataElt(this.getName(), this.payload);
	}

	@Override
	public void forceContent(String constraintvalue) {
		throw new RuntimeException("not yet implemented");

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof IntegerDataElt))
			return false;
		IntegerDataElt parseddataelt = (IntegerDataElt) other;
		return (this.payload.equals(parseddataelt.payload));
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeInteger.singleton);
	}
}
