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

import java.util.logging.Logger;

import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * A field storing an element of a list of value.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the FieldChoiceDefinition
 * @param <F> the parent data object
 */
public class ChoiceDataObjectField<E extends FieldChoiceDefinition<E>, F extends DataObject<F>>
		extends DataObjectField<ChoiceDataObjectFieldDefinition<E, F>, F> {
	private final static Logger logger = Logger.getLogger(ChoiceDataObjectField.class.getName());
	private E choicedefinition;
	protected StoredField<String> storedvalue;
	private ChoiceValue<E> currentvalue;
	private ChoiceValue<E> transitionrestrictions;

	/**
	 * Creates a ChoiceDataObjectField from the object payload
	 * 
	 * @param definition       field definition
	 * @param parentpayload    object payload
	 * @param maxlength        length of the storage
	 * @param choicedefinition definition of the field choice (list of values)
	 */
	@SuppressWarnings("unchecked")
	public ChoiceDataObjectField(ChoiceDataObjectFieldDefinition<E, F> definition, DataObjectPayload parentpayload,
			int maxlength, E choicedefinition) {
		super(definition, parentpayload);
		this.choicedefinition = choicedefinition;
		storedvalue = (StoredField<String>) this.field.get(0);
		currentvalue = choicedefinition.parseChoiceValue(storedvalue.getPayload());
	}

	/**
	 * Creates a ChoiceDataObjectField from the object payload with transition
	 * restrictions
	 * 
	 * @param definition
	 * @param parentpayload
	 * @param maxlength
	 * @param choicedefinition
	 * @param transitionrestrictions
	 */
	public ChoiceDataObjectField(ChoiceDataObjectFieldDefinition<E, F> definition, DataObjectPayload parentpayload,
			int maxlength, E choicedefinition, ChoiceValue<E> transitionrestrictions) {
		this(definition, parentpayload, maxlength, choicedefinition);
		this.transitionrestrictions = transitionrestrictions;

	}

	@Override
	public SimpleDataElt getDataElement() {
		if (transitionrestrictions == null)
			return new ChoiceDataElt<ChoiceValue<E>>(this.getName(), currentvalue);
		return new ChoiceDataElt<ChoiceValue<E>>(this.getName(), currentvalue, transitionrestrictions);
	}

	/**
	 * @param newchoice sets a new value, not checking the transiction restrictions
	 */
	public void setValue(ChoiceValue<E> newchoice) {
		this.currentvalue = newchoice;
		if (currentvalue != null)
			this.storedvalue.setPayload(currentvalue.getName());
		if (currentvalue == null)
			this.storedvalue.setPayload("");
	}

	/**
	 * @return gets the current value of the field
	 */
	public ChoiceValue<E> getValue() {
		return currentvalue;
	}

	/**
	 * sets the value of this field from a data structure element
	 * 
	 * @param element a dat aelement
	 */
	protected void setValue(ChoiceDataElt<ChoiceValue<E>> element) {
		if (element.getStoredValue() == null) {
			choicedefinition = null;
			this.storedvalue.setPayload("");
			return;
		}
		if (element.getStoredValue().length() == 0) {
			choicedefinition = null;
			this.storedvalue.setPayload("");
			return;
		}
		ChoiceValue<E> feedback = choicedefinition.parseChoiceValue(element.getStoredValue());
		if (feedback == null) {
			logger.severe("value not found for choicefield " + this.getName() + ", stored value searched = "
					+ element.getStoredValue() + " forcing to empty choice");
			choicedefinition = null;
			this.storedvalue.setPayload("");
			return;

		}

		currentvalue = feedback;
		this.storedvalue.setPayload(feedback.getStorageCode());
	}

	@Override
	public void postTreatmentAfterInitFromDB() {

		if (storedvalue.getPayload() != null)
			if (storedvalue.getPayload().compareTo("") != 0) {
				ChoiceValue<E> feedback = choicedefinition.parseChoiceValue(storedvalue.getPayload());
				if (feedback == null) {
					logger.severe("value not found for choicefield " + this.getName() + ", stored value searched = "
							+ storedvalue.getPayload() + " forcing to empty choice");

					return;

				}
				currentvalue = feedback;
			}
	}

}
