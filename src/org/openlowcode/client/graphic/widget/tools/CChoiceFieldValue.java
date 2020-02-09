/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.tools;

import java.util.ArrayList;

import org.openlowcode.tools.structure.Choice;

/**
 * A choice value for use in the client. Includes ordering and display value for
 * the GUI
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CChoiceFieldValue
		implements
		Choice {

	private String storedvalue;
	private String displayvalue;
	private String valuehelper;
	private int sequence;
	private boolean frozen;

	/**
	 * @return true if value is frozen
	 */
	public boolean isFrozen() {
		return this.frozen;
	}

	/**
	 * @return a frozen version of the choice value
	 */
	public CChoiceFieldValue duplicateAsFrozen() {
		CChoiceFieldValue duplicate = new CChoiceFieldValue(storedvalue, displayvalue, valuehelper, sequence);
		duplicate.frozen = true;
		duplicate.setRestrictionsOnNextValues(this.getRestrictionsOnNextValues());
		return duplicate;
	}

	private ArrayList<String> restrictionsonnextvalue;

	/**
	 * Creates a choice value
	 * 
	 * @param storedvalue  unique stored value
	 * @param displayvalue display value (for use in GUI)
	 * @param valuehelper  a long description of the meaning of this value
	 * @param sequence     sequence of the choice value (used for ordering)
	 */
	public CChoiceFieldValue(String storedvalue, String displayvalue, String valuehelper, int sequence) {
		super();
		this.storedvalue = storedvalue;
		this.displayvalue = displayvalue;
		this.valuehelper = valuehelper;
		restrictionsonnextvalue = null;
		this.frozen = false;
		this.sequence = sequence;
	}

	/**
	 * creates an empty choice value
	 */
	public CChoiceFieldValue() {
		this.storedvalue = null;
		this.displayvalue = null;
		this.valuehelper = null;
		restrictionsonnextvalue = null;
		this.frozen = false;
		this.sequence = 0;

	}

	/**
	 * restrictions on next value allow only the transitions to a reduced sets of
	 * values. This is used for example for lifecycles
	 * 
	 * @param nextvalues list of next values allowed
	 */
	public void setRestrictionsOnNextValues(ArrayList<String> nextvalues) {
		this.restrictionsonnextvalue = nextvalues;
	}

	/**
	 * get the list of next values allowed
	 * 
	 * @return list of next values allowed
	 */
	public ArrayList<String> getRestrictionsOnNextValues() {
		return restrictionsonnextvalue;
	}

	@Override
	public String getStorageCode() {
		return storedvalue;
	}

	/**
	 * @return the display value
	 */
	public String getDisplayvalue() {
		return displayvalue;
	}

	/**
	 * @return the value helper
	 */
	public String getValuehelper() {
		return valuehelper;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return this.sequence;
	}

	@Override
	public boolean equals(Object arg0) {
		if (this == arg0)
			return true;
		if (arg0 == null || getClass() != arg0.getClass())
			return false;
		CChoiceFieldValue choice = (CChoiceFieldValue) arg0;
		if (this.storedvalue == null) {
			if (choice.storedvalue == null)
				return true;
			return false;
		}

		if (this.storedvalue.equals(choice.getStorageCode()))
			return true;
		return false;

	}

	@Override
	public int hashCode() {

		return storedvalue.hashCode();
	}

	@Override
	public String toString() {
		return displayvalue;
	}

}
