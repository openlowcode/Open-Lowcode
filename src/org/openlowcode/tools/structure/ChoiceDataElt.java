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
import java.util.ArrayList;

import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * A data element providing one of a defined list of choices
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 * @param <E>
 */
/**
 * @author demau
 *
 * @param <E>
 */
public class ChoiceDataElt<E extends Choice> extends SimpleDataElt {
	private static Logger logger = Logger.getLogger(ChoiceDataElt.class.getName());
	private String storedvalue;
	private boolean restriction;
	private ArrayList<String> storedauthorizedvalues;

	/**
	 * @return true if the choice has restrictions for last value
	 */
	public boolean isRestriction() {
		return restriction;
	}

	/**
	 * @return the authorized values
	 */
	public String[] getAuthorizedValues() {
		if (storedauthorizedvalues == null)
			return null;
		return storedauthorizedvalues.toArray(new String[0]);
	}

	/**
	 * @param restriction sets the restriction flag
	 */
	public void setRestriction(boolean restriction) {
		this.restriction = restriction;
	}

	/**
	 * @param eltname element name
	 * @param value   value to store
	 */
	public ChoiceDataElt(String eltname, E value) {
		super(eltname, new ChoiceDataEltType());
		if (value == null) {
			this.storedvalue = "";

		} else {
			this.storedvalue = value.getStorageCode();
		}

	}

	/**
	 * @param value new payload
	 */
	public void changePayload(E value) {
		if (value == null) {
			this.storedvalue = "";

		} else {
			this.storedvalue = value.getStorageCode();
		}
	}

	private ChoiceDataElt(String name, String storedvalue, boolean restriction,
			ArrayList<String> storedauthorizedvalues) {
		super(name, new ChoiceDataEltType());
		this.storedvalue = storedvalue;
		this.restriction = restriction;
		this.storedauthorizedvalues = storedauthorizedvalues;
	}

	/**
	 * @param name             name of the lement
	 * @param value            current value
	 * @param authorizedvalues authorized values for change
	 */
	public ChoiceDataElt(String name, E value, ChoiceWithTransition authorizedvalues) {
		this(name, value);
		this.restriction = true;
		this.storedvalue = value.getStorageCode();
		storedauthorizedvalues = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		E[] authorizedtransitions = (E[]) authorizedvalues.getAuthorizedTransitions();
		for (int i = 0; i < authorizedtransitions.length; i++) {
			storedauthorizedvalues.add(authorizedtransitions[i].getStorageCode());
		}
	}

	/**
	 * generates a choice data element with no restriction on the values of the
	 * field
	 * 
	 * @param name  name of the field
	 * @param value the value currently selected
	 */
	public ChoiceDataElt(String name, ChoiceWithTransition value) {
		super(name, new ChoiceDataEltType());
		if (value != null) {
			this.storedvalue = value.getStorageCode();
			if (!value.isTransitionrestrictions()) {
				this.restriction = false;
			} else {
				logger.finer(" !!**!! restrictions in initialization of choice data element");
				this.restriction = true;
				this.storedauthorizedvalues = new ArrayList<String>();
				for (int i = 0; i < value.getAuthorizedTransitions().length; i++) {
					logger.finest("!!**!! adding transition " + i + " -- "
							+ value.getAuthorizedTransitions()[i].getStorageCode());
					this.storedauthorizedvalues.add(value.getAuthorizedTransitions()[i].getStorageCode());
				}
			}

		} else {
			this.storedvalue = "";
			this.restriction = false;
		}

	}

	/**
	 * 
	 * @param name name of the element
	 */
	public ChoiceDataElt(String name) {
		super(name, new ChoiceDataEltType());
		this.restriction = false;
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addStringField("STV", storedvalue);
		writer.addBooleanField("RST", restriction);
		if (restriction) {
			writer.startStructure("ATVS");
			for (int i = 0; i < storedauthorizedvalues.size(); i++) {
				writer.startStructure("ATV");
				writer.addStringField("VAL", storedauthorizedvalues.get(i));
				writer.endStructure("ATV");
			}
			writer.endStructure("ATVS");
		}

	}

	@Override
	protected Object getMessageArrayValue() {
		return storedvalue;
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		if (restriction)
			throw new RuntimeException("Restricted Choice Value not supported by compact array");
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeString.singleton);
	}

	@Override
	public String defaultTextRepresentation() {
		return this.storedvalue;

	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.storedvalue = reader.returnNextStringField("STV");
		this.restriction = reader.returnNextBooleanField("RST");
		if (restriction) {
			storedauthorizedvalues = new ArrayList<String>();
			reader.startStructureArray("ATV");
			while (reader.structureArrayHasNextElement("ATV")) {
				storedauthorizedvalues.add(reader.returnNextStringField("VAL"));
				reader.returnNextEndStructure("ATV");
			}
		}
	}

	public String getStoredValue() {
		return this.storedvalue;
	}

	@Override
	public ChoiceDataElt<E> cloneElt() {
		ArrayList<String> cloneauthorizedvalue = new ArrayList<String>();
		if (this.storedauthorizedvalues != null)
			for (int i = 0; i < this.storedauthorizedvalues.size(); i++) {
				cloneauthorizedvalue.add(this.storedauthorizedvalues.get(i));
			}
		return new ChoiceDataElt<E>(this.getName(), this.storedvalue, this.restriction, cloneauthorizedvalue);
	}

	@Override
	public void forceContent(String constraintvalue) {
		this.storedvalue = constraintvalue;
	}

	public boolean defineRestriction(ArrayList<String> constraint) {
		this.restriction = true;
		this.storedauthorizedvalues = constraint;
		if (storedauthorizedvalues == null)
			return false;
		if (storedauthorizedvalues.size() == 0)
			return false;
		boolean valid = false;
		for (int i = 0; i < constraint.size(); i++)
			if (this.storedvalue != null)
				if (this.storedvalue.equals(constraint.get(i)))
					valid = true;
		if (valid)
			return false;
		// current value not found, is reset.
		this.storedvalue = null;
		return true;

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof ChoiceDataElt))
			return false;
		@SuppressWarnings("rawtypes")
		ChoiceDataElt choicedataelt = (ChoiceDataElt) other;
		if (this.storedvalue == null)
			return false;
		return (this.storedvalue.equals(choicedataelt.storedvalue));
	}
}
