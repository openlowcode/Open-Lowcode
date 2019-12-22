/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.misc.StringDecoder;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;

/**
 * Definition of a multiple choice field. This defines what values are possible.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 *
 */
public abstract class FieldChoiceDefinition<E extends FieldChoiceDefinition<E>> implements StringDecoder {
	private static Logger logger = Logger.getLogger(FieldChoiceDefinition.class.getName());
	private NamedList<ChoiceValue<E>> choicevalues;
	private HashMap<String, ChoiceValue<E>> indexbydisplayvalue;
	private ChoiceValue<E> defaultchoice;
	private int storagesize;

	/**
	 * parses a MultipleChoiceDataElt to get the different choices included
	 * 
	 * @param element a multiple choice element
	 * @return the list of parsed choice values
	 */
	@SuppressWarnings("unchecked")
	public ChoiceValue<E>[] parseMultipleChoiceElt(@SuppressWarnings("rawtypes") MultipleChoiceDataElt element) {
		ArrayList<ChoiceValue<E>> choices = new ArrayList<ChoiceValue<E>>();
		for (int i = 0; i < element.getSelectedChoicesNumber(); i++) {
			String storagecode = element.getSelectedChoiceAt(i);
			ChoiceValue<E> value = choicevalues.lookupOnName(storagecode);
			if (value == null)
				throw new RuntimeException("Inconsistent storage code of " + storagecode);
			choices.add(value);
		}
		return choices.toArray(new ChoiceValue[0]);
	}

	/**
	 * gets all the possible choice values
	 * 
	 * @return get all the possible choice values for this definition
	 */
	@SuppressWarnings("unchecked")
	public ChoiceValue<E>[] getChoiceValue() {

		return (ChoiceValue<E>[]) choicevalues.getFullList().toArray(new ChoiceValue[0]);
	}

	/**
	 * @return the default choice defined
	 */
	public ChoiceValue<E> getDefaultChoice() {
		return defaultchoice;
	}

	/**
	 * @return the number of characters needed to store in the persistence layer the
	 *         code corresponding to one ChoiceValue
	 */
	public int getStorageSize() {
		return storagesize;
	}

	/**
	 * @param valueasstored the code of the value
	 * @return the value corresponding to the code
	 */
	protected ChoiceValue<E> parseChoiceValue(String valueasstored) {
		return choicevalues.lookupOnName(valueasstored);
	}

	/**
	 * @param value a display value
	 * @return the first choice value that has precisely this display value. In case
	 *         there are several fields with the same display value, the first one
	 *         is taken in the order of the module design file.
	 */
	public ChoiceValue<E> lookUpByDisplayValue(String value) {
		return this.indexbydisplayvalue.get(value);
	}

	/**
	 * @param newvalue when defining this fieldchoice, adds a possible choice value
	 */
	protected void addChoiceValue(ChoiceValue<E> newvalue) {
		if (newvalue.getStorageCode().length() > storagesize)
			throw new RuntimeException("value too long for field choice definition with storagesize = " + storagesize
					+ ", new value = '" + newvalue.getStorageCode() + "'");
		this.choicevalues.add(newvalue);

		if (this.indexbydisplayvalue.get(newvalue.getDisplayValue()) == null)
			this.indexbydisplayvalue.put(newvalue.getDisplayValue(), newvalue);
	}

	/**
	 * @param newvalue when defining this fieldchoice, adds a possible choice value
	 *                 as a default value (note: there can be only a single default
	 *                 value)
	 */
	protected void addChoiceValueasDefault(ChoiceValue<E> newvalue) {
		if (newvalue.getStorageCode().length() > storagesize)
			throw new RuntimeException("value too long for field choice definition with storagesize = " + storagesize
					+ ", new value = '" + newvalue.getStorageCode() + "'");
		this.choicevalues.add(newvalue);
		this.defaultchoice = newvalue;
		if (this.indexbydisplayvalue.get(newvalue.getDisplayValue()) == null)
			this.indexbydisplayvalue.put(newvalue.getDisplayValue(), newvalue);
	}

	/**
	 * by default, the choicevalue will be null
	 */
	protected void setDefaultAsNull() {
		this.defaultchoice = null;
	}

	/**
	 * creates a new FieldChoiceDefinition with the defined storage size to store a
	 * single code in the database
	 * 
	 * @param storagesize the storage size expressed in number of characters
	 */
	public FieldChoiceDefinition(int storagesize) {
		this.choicevalues = new NamedList<ChoiceValue<E>>();
		this.indexbydisplayvalue = new HashMap<String, ChoiceValue<E>>();
		this.storagesize = storagesize;

	}

	/**
	 * @param defaultchoice sets as default a ChoiceValue that was already added
	 */
	public void setDefaultChoice(ChoiceValue<E> defaultchoice) {
		this.defaultchoice = defaultchoice;
	}

	/**
	 * @param code code of a choicevalue in this fieldchoicedefinition
	 * @return the display of the choicevalue with the given code, or an error
	 *         string if invalid
	 */
	public String showDisplay(String code) {
		if (code == null)
			return "";
		if (code.trim().length() == 0)
			return "";
		ChoiceValue<E> value = choicevalues.lookupOnName(code);
		if (value == null)
			return code + " (invalid)";
		return value.getDisplayValue();
	}

	@Override
	public String toString() {
		StringBuffer allstoredvalues = new StringBuffer("Values [");
		for (int i = 0; i < choicevalues.getSize(); i++) {
			if (i > 0)
				allstoredvalues.append(",");
			allstoredvalues.append(choicevalues.get(i).getStorageCode());
			allstoredvalues.append(":");
			allstoredvalues.append(choicevalues.get(i).getDisplayValue());

		}
		allstoredvalues.append("]");
		return allstoredvalues.toString();
	}

	@Override
	public String decode(String storedvalue) {

		ChoiceValue<E> choice = choicevalues.lookupOnName(storedvalue);
		if (choice != null)
			logger.finer("Found choice " + choice.getDisplayValue() + " for code " + storedvalue);
		if (choice == null)
			logger.finer("Did not find choice for code " + storedvalue + ", valicodes = " + this.toString());
		if (choice != null)
			return choice.getDisplayValue();
		return storedvalue;
	}

}
