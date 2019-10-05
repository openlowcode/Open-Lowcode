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
import java.math.BigDecimal;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeDecimal;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * A data element storing a decimal (java BigDecimal)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DecimalDataElt extends SimpleDataElt {
	private BigDecimal payload;
	private static Logger logger = Logger.getLogger(DecimalDataElt.class.toString());

	public DecimalDataElt(String name, BigDecimal payload) {
		super(name, new DecimalDataEltType());
		this.payload = payload;
		this.locked = false;
	}

	/**
	 * @param name    name of the element
	 * @param payload payload
	 * @param locked  true to freeze the value
	 */
	public DecimalDataElt(String name, BigDecimal payload, boolean locked) {
		super(name, new DecimalDataEltType());
		this.payload = payload;
		this.locked = locked;
	}

	public boolean islocked() {
		return this.locked;
	}

	private boolean locked;

	/**
	 * @param locked change the value of the locked flag
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * creates a decimal data element
	 * 
	 * @param name name of the element
	 */
	public DecimalDataElt(String name) {
		super(name, new DecimalDataEltType());
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addDecimalField("PLD", this.payload);

	}

	@Override
	protected Object getMessageArrayValue() {
		return payload;
	}

	@Override
	public String defaultTextRepresentation() {
		if (payload != null)
			return payload.toString();
		return "";
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextDecimalField("PLD");

	}

	public BigDecimal getPayload() {
		return this.payload;
	}

	public void updatePayload(BigDecimal payload) {
		this.payload = payload;
	}

	@Override
	public DecimalDataElt cloneElt() {
		return new DecimalDataElt(this.getName(), this.payload, this.locked);
	}

	public void lockToValue(String value) {
		BigDecimal decimal = null;
		if (value != null)
			if (value.length() > 0)
				decimal = new BigDecimal(value);
		this.payload = decimal;
		this.locked = true;
	}

	public void unlockValue() {
		if (this.locked)
			logger.info("Unlocking field " + this.getName() + ", value = " + this.payload);
		this.locked = false;
	}

	@Override
	public void forceContent(String constraintvalue) {
		BigDecimal decimal = null;
		if (constraintvalue != null)
			if (constraintvalue.length() > 0)
				decimal = new BigDecimal(constraintvalue);
		this.payload = decimal;

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof DecimalDataElt))
			return false;
		DecimalDataElt parseddataelt = (DecimalDataElt) other;
		if (this.payload == null) {
			if (parseddataelt.payload == null)
				return true;
			if (parseddataelt.payload != null)
				return false;

		}
		return (this.payload.equals(parseddataelt.payload));
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeDecimal.singleton);
	}
}
