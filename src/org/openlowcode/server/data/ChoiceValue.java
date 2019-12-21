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

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.Choice;
import org.openlowcode.tools.structure.ChoiceWithTransition;

/**
 * a choice value is a possible value for choice fields. It may have transition
 * restrictions to represent a workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the field choice definition
 */
public class ChoiceValue<E extends FieldChoiceDefinition> extends Named implements Choice, ChoiceWithTransition {
	private ArrayList<ChoiceValue<E>> authorizedtransitions;
	private boolean transitionrestrictions;
	private String displayvalue;
	private String tooltip;
	private boolean activechoice;

	@Override
	public String getStorageCode() {
		return this.getName();
	}

	/**
	 * @return the display value of the choice
	 */
	public String getDisplayValue() {
		return this.displayvalue;
	}

	/**
	 * @return the tooltip of the choice
	 */
	public String getTooltip() {
		return this.tooltip;
	}

	/**
	 * checks if the transition is authorized
	 * 
	 * @param newvalue the new value to transition to
	 * @return true if transition is authorized, false else
	 */
	public boolean isAuthorizedTransitions(ChoiceValue<E> newvalue) {
		if (transitionrestrictions == false)
			return true;
		if (authorizedtransitions == null)
			return false;
		for (int i = 0; i < authorizedtransitions.size(); i++) {
			if (authorizedtransitions.get(i).getStorageCode().compareTo(newvalue.getStorageCode()) == 0)
				return true;
		}
		return false;
	}

	@Override
	public boolean isTransitionrestrictions() {
		return transitionrestrictions;
	}

	/**
	 * @param storagecode            the text stored in the dabatase, typically
	 *                               short, without special characters and in
	 *                               English for multi-lingual application
	 * @param displayvalue           the value as displayed, typically in clear
	 *                               human language, but quite short
	 * @param tooltip                the whole story about the value
	 * @param activechoice           false if the choice is available in theory but
	 *                               not in the current context
	 * @param transitionrestrictions true if transictions are restricted
	 */
	public ChoiceValue(String storagecode, String displayvalue, String tooltip, boolean activechoice,
			boolean transitionrestrictions) {
		super(storagecode);
		this.displayvalue = displayvalue;
		this.tooltip = tooltip;
		this.activechoice = activechoice;
		this.transitionrestrictions = false;
		if (transitionrestrictions)
			setTransitionRestrictions();
	}

	/**
	 * @param storagecode  the text stored in the dabatase, typically short, without
	 *                     special characters and in English for multi-lingual
	 *                     application
	 * @param displayvalue the value as displayed, typically in clear human
	 *                     language, but quite short
	 * @param tooltip      the whole story about the value
	 * @param activechoice false if the choice is available in theory but not in the
	 *                     current context
	 */
	public ChoiceValue(String storagecode, String displayvalue, String tooltip, boolean activechoice) {
		super(storagecode);
		this.displayvalue = displayvalue;
		this.tooltip = tooltip;
		this.activechoice = activechoice;
		transitionrestrictions = false;
	}

	/**
	 * specifies that this choice value has transiction restrictions, and will allow
	 * as next value only some choice values
	 */
	public void setTransitionRestrictions() {
		this.transitionrestrictions = true;
		if (this.authorizedtransitions == null) {
			this.authorizedtransitions = new ArrayList<ChoiceValue<E>>();
		} else {
			this.authorizedtransitions.clear();
		}
	}

	/**
	 * some choice values restrict transition to a new choice value
	 * 
	 * @param targetchoice the authorized target choice for a field where current
	 *                     value is this choice
	 */
	public void addTransition(ChoiceValue<E> targetchoice) {
		if (this.transitionrestrictions == false)
			transitionrestrictions = true;
		if (authorizedtransitions == null)
			authorizedtransitions = new ArrayList<ChoiceValue<E>>();
		authorizedtransitions.add(targetchoice);
	}

	@SuppressWarnings("unchecked")
	public ChoiceValue<E>[] getAuthorizedTransitions() {
		if (this.authorizedtransitions == null)
			throw new RuntimeException("Transitions not defined for value " + this.getName());
		return this.authorizedtransitions.toArray(new ChoiceValue[0]);
	}

	/**
	 * writes the definition of this choice value on the OpenLowcode message
	 * 
	 * @param writer the writer of the message
	 * @throws IOException if an exception is encountered transmitting the message
	 */
	public void WritePayloadToCDL(MessageWriter writer) throws IOException {
		writer.startStructure("CHV");
		writer.addStringField("STC", this.getName());
		writer.addStringField("DIV", displayvalue);
		writer.addStringField("TLT", tooltip);
		writer.addBooleanField("ACT", activechoice);
		writer.endStructure("CHV");
	}

	@Override
	public int hashCode() {
		return this.getStorageCode().hashCode();
	}

	@Override
	public String toString() {
		return "[CHOICEVALUE:" + this.getName() + ":" + this.getStorageCode() + "]";
	}

}
