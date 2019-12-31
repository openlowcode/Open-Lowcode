/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action;

import java.io.IOException;

import org.openlowcode.server.graphic.widget.SActionInlineDataLoc;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;

/**
 * A reference to an inline action, i.e. an action that will make an interaction
 * to the server and display more data on the current page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SInlineActionRef extends SActionRef {

	private boolean local;
	@SuppressWarnings("rawtypes")
	private NamedList<SActionInlineDataLoc> outputactiondata;

	/**
	 * creates an inline action reference
	 * 
	 * @param name               name of the action
	 * @param module             name of the module
	 * @param inputargumentindex index of the argument for the action if local
	 * @param local              true if action is local, meaning the action is
	 *                           actually transfering some data on the client from a
	 *                           widget to another
	 */
	protected SInlineActionRef(String name, String module, int inputargumentindex, boolean local) {
		this(name, module, inputargumentindex);
		this.local = local;
	}

	/**
	 * creates a non-local inline action reference
	 * 
	 * @param name               name of the action
	 * @param module             name of the module
	 * @param inputargumentindex index of the argument for the action
	 */
	@SuppressWarnings("rawtypes")
	public SInlineActionRef(String name, String module, int inputargumentindex) {
		super(name, module, inputargumentindex);
		this.local = false;
		outputactiondata = new NamedList<SActionInlineDataLoc>();
	}

	/**
	 * @param dataelt adds a output data for the action
	 */
	@SuppressWarnings("rawtypes")
	public void addActionOutputBusinessData(SActionInlineDataLoc dataelt) {
		outputactiondata.add(dataelt);
	}

	@Override
	public void writeToCML(MessageWriter writer) throws IOException {

		writer.startStructure("INLINEACTION");
		writer.addStringField("NAME", this.getName());
		writer.addStringField("MODULE", this.getModule());
		writer.addBooleanField("LOCAL", this.local);
		writer.startStructure("ACTIONDATAS");
		writeAttributesInOrder(writer);
		writer.endStructure("ACTIONDATAS");
		writer.startStructure("OUTPUTDATAS");
		for (int i = 0; i < this.outputactiondata.getSize(); i++) {
			outputactiondata.get(i).WriteToCDL(writer);
		}
		writer.endStructure("OUTPUTDATAS");
		writer.endStructure("INLINEACTION");
	}

	/**
	 * writes the reference of action to the output location
	 * 
	 * @param writer  writer
	 * @param dataref data reference
	 * @throws IOException if anything wrong happens while sending the message
	 */
	@SuppressWarnings("rawtypes")
	public void writeReferenceToOutputCLM(MessageWriter writer, SActionOutputDataRef dataref) throws IOException {
		writer.startStructure("INLINEACTION");
		writer.addStringField("NAME", this.getName());
		writer.addStringField("MODULE", this.getModule());
		writer.startStructure("RELFLD");
		dataref.writeReferenceToCML(writer);
		writer.endStructure("RELFLD");
		writer.endStructure("INLINEACTION");
	}
}
