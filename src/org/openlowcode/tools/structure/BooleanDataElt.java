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
 * a boolean data element that can take values true, false, or unset
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
/**
 * @author demau
 *
 */
public class BooleanDataElt extends SimpleDataElt {
	private int payload;
	public static int BOOLEAN_UNSET=-1;
	public static int BOOLEAN_TRUE=1;
	public static int BOOLEAN_FALSE=0;
	public BooleanDataElt(String name, SimpleDataEltType type) {
		super(name,new BooleanDataEltType());
		this.payload = BOOLEAN_UNSET;
	}
	
	/**
	 * creates a boolean data element
	 * @param name name of the element
	 * @param payload value as defined in static integers in this class
	 */
	public BooleanDataElt(String name,int payload) {
		super(name, new BooleanDataEltType());
		boolean valid = false;
		if (payload == BOOLEAN_UNSET) valid=true;
		if (payload == BOOLEAN_TRUE) valid=true;
		if (payload == BOOLEAN_FALSE) valid=true;
		if (!valid) throw new RuntimeException("in creating booleandataelt "+name+", an incorrect value "+payload+" was provided. Please consult BooleanDataElt ");
		this.payload = payload;
	}
	/**
	 * creates a boolean data element
	 * @param name name of the element
	 * @param booleanpayload value as defined in static integers in this class
	 */
	public BooleanDataElt(String name,boolean booleanpayload) {
		super(name, new BooleanDataEltType());
		if (booleanpayload) this.payload = BOOLEAN_TRUE;
		this.payload = BOOLEAN_FALSE;
	}
	
	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addIntegerField("PLD",payload);

	}
	@Override
	protected Object getMessageArrayValue() {
		return new Integer(payload);
	}


	@Override
	public String defaultTextRepresentation() {
		if (payload == BOOLEAN_UNSET) return "UNSET";
		if (payload == BOOLEAN_TRUE) return "TRUE";
		if (payload == BOOLEAN_FALSE) return "FALSE";
		return "#Error";
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextIntegerField("PLD");

	}

	
	
	@Override
	public BooleanDataElt cloneElt()  {
		return new BooleanDataElt(this.getName(),this.payload);
		
	}

	@Override
	public void forceContent(String constraintvalue)  {
		throw new RuntimeException("not yet implemented");
		
	}
	@Override
	public boolean equals(Object other) {
		if (other==null) return false;
		if (!(other instanceof BooleanDataElt)) return false;
		BooleanDataElt otherbooleandataelt = (BooleanDataElt) other;
		return (this.payload==otherbooleandataelt.payload);
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(),MessageFieldTypeInteger.singleton);
	}

	
	
}
