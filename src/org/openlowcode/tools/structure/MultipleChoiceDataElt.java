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
import java.util.Arrays;

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.MultiStringEncoding;
/**
 * A data element to transport a multi-value choice
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 * @param <E>
 */
public class MultipleChoiceDataElt<E extends Choice> extends SimpleDataElt {
	public MultipleChoiceDataElt(String name) {
		super(name, new MultipleChoiceDataEltType());
		this.selectedchoices = new ArrayList<String>();
	}
	
	public void addChoice(E choice) {
		this.selectedchoices.add(choice.getStorageCode());
	}
	public void addChoices(E[] choices) {
		if (choices!=null) for (int i=0;i<choices.length;i++)
			this.selectedchoices.add(choices[i].getStorageCode());
	}
	 
	public void addChoice(String choice) {
		this.selectedchoices.add(choice);
	}
	public void addChoices(String[] choices) {
		if (choices!=null) this.selectedchoices.addAll(Arrays.asList(choices));
	}

	private ArrayList<String> selectedchoices;
	
	public int getSelectedChoicesNumber() {
		return this.selectedchoices.size();
	}
	public String getSelectedChoiceAt(int index) {
		return this.selectedchoices.get(index);
	}
	
	@Override
	public SimpleDataElt cloneElt() {
		@SuppressWarnings("rawtypes")
		MultipleChoiceDataElt clone = new MultipleChoiceDataElt(this.getName());
		clone.addChoices(selectedchoices.toArray(new String[0]));
		return clone;
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addStringField("MLC",MultiStringEncoding.encode(selectedchoices.toArray(new String[0])));

	}

	@Override
	public String defaultTextRepresentation() {
		return MultiStringEncoding.encode(selectedchoices.toArray(new String[0]));
	}

	@Override
	public void forceContent(String constraintvalue) {
		this.selectedchoices = new ArrayList<String>();
		this.selectedchoices.addAll(Arrays.asList(MultiStringEncoding.parse(constraintvalue)));
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeString.singleton);
	}

	@Override
	protected Object getMessageArrayValue() {
		return MultiStringEncoding.encode(selectedchoices.toArray(new String[0]));
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		String[] choices = MultiStringEncoding.parse(reader.returnNextStringField("MLC"));
		if (choices!=null) for (int i=0;i<choices.length;i++) this.selectedchoices.add(choices[i]);
	}

}
