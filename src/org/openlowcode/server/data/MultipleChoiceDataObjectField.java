/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.MultiStringEncoding;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

import org.openlowcode.server.data.storage.StoredField;

/**
 * a field of a data object holding several choice values. A list of codes is
 * persisted in the databases separated by "|"
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> choice definition
 * @param <F> parent data object
 */
public class MultipleChoiceDataObjectField<E extends FieldChoiceDefinition<E>, F extends DataObject<F>>
		extends
		DataObjectField<MultipleChoiceDataObjectFieldDefinition<E, F>, F> {
	private static Logger logger = Logger.getLogger(MultipleChoiceDataObjectField.class.getName());
	private ArrayList<ChoiceValue<E>> choices;
	protected StoredField<String> storedvalue;
	private E choicedefinition;

	/**
	 * creates a multi-choice field
	 * 
	 * @param definition       definition of the multi-choice field
	 * @param parentpayload    payload of the parent data object
	 * @param choicedefinition definition of the choice
	 */
	@SuppressWarnings("unchecked")
	public MultipleChoiceDataObjectField(
			MultipleChoiceDataObjectFieldDefinition<E, F> definition,
			DataObjectPayload parentpayload,
			E choicedefinition) {
		super(definition, parentpayload);
		this.choices = new ArrayList<ChoiceValue<E>>();
		storedvalue = (StoredField<String>) this.field.get(0);
		this.choicedefinition = choicedefinition;
		if (storedvalue == null)
			throw new RuntimeException("Could not find stored field");

	}

	@SuppressWarnings("unchecked")
	@Override
	public SimpleDataElt getDataElement() {
		MultipleChoiceDataElt<ChoiceValue<E>> element = new MultipleChoiceDataElt<ChoiceValue<E>>(this.getName());
		element.addChoices(choices.toArray(new ChoiceValue[0]));
		return element;
	}

	private void decodePayload() {
		String[] values = MultiStringEncoding.parse(storedvalue.getPayload());
		for (int i = 0; i < values.length; i++) {
			if (values[i].length() > 0)
				choices.add(choicedefinition.parseChoiceValue(values[i]));
			if (values[i].length() == 0)
				logger.warning("One multiple choice definition is null, index = " + i + ", full string = "
						+ storedvalue.getPayload() + ", name = " + this.getName());
		}
	}

	/**
	 * sets the content of the field according to storage string (codes of values
	 * separated by '|')
	 * 
	 * @param storagestring storage string
	 */
	public void loadNewStorageString(String storagestring) {
		if (storagestring == null)
			throw new RuntimeException("storage string cannot be null (can be zero length string though)");
		storedvalue.setPayload(storagestring);
		decodePayload();
	}

	/**
	 * adds a choice inside this field
	 * 
	 * @param choice choice to add
	 */
	public void addChoice(ChoiceValue<E> choice) {
		choices.add(choice);
		this.storedvalue.setPayload(getStorageString());
	}

	/**
	 * @return get the number of choices
	 */
	public int getSelectedChoiceNr() {
		return choices.size();
	}

	/**
	 * get the choices at index
	 * 
	 * @param index an index between 0 (included) and SelectedChoiceNr (excluded)
	 * @return
	 */
	public ChoiceValue<E> getSelectedChoiceAt(int index) {
		return choices.get(index);
	}

	/**
	 * @return a unique storage string listing the selected values by alphabetical
	 *         orders, separated by pipe
	 */
	public String getStorageString() {
		ArrayList<String> storagestring = new ArrayList<String>();
		for (int i = 0; i < choices.size(); i++)
			storagestring.add(choices.get(i).getStorageCode());
		Collections.sort(storagestring);
		return MultiStringEncoding.encode(storagestring);

	}

	/**
	 * empty the content of the multiple field
	 */
	public void reset() {
		this.choices = new ArrayList<ChoiceValue<E>>();
		this.storedvalue.setPayload("");

	}

	/**
	 * set the payload of this field to the given list of choices
	 * 
	 * @param choices list of choices
	 */
	public void setValue(ChoiceValue<E>[] choices) {
		reset();
		this.choices.addAll(Arrays.asList(choices));
		this.storedvalue.setPayload(getStorageString());
	}

	/**
	 * sets the value of the field to this payload
	 * 
	 * @param thischoicefield a multiple choice field
	 */
	public void setValue(MultipleChoiceDataElt<?> thischoicefield) {
		reset();
		for (int i = 0; i < thischoicefield.getSelectedChoicesNumber(); i++) {
			this.choices.add(choicedefinition.parseChoiceValue(thischoicefield.getSelectedChoiceAt(i)));
		}
		this.storedvalue.setPayload(getStorageString());

	}

	@Override
	public void postTreatmentAfterInitFromDB() {
		if (storedvalue.getPayload() != null)
			if (storedvalue.getPayload().compareTo("") != 0) {
				decodePayload();
			}
	}

	@SuppressWarnings("unchecked")
	public ChoiceValue<E>[] getValue() {
		return this.choices.toArray(new ChoiceValue[0]);
	}

}
