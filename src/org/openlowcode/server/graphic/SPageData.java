/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic;

import java.io.IOException;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DataElt;

/**
 * The business data of a page. Open Lowcode pages separate strictly business
 * data and layout. The business data is contained in this class
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SPageData {
	private NamedList<DataElt> dataelements;
	private String usermessage;
	private boolean popup = false;

	/**
	 * creates a new empty set of pagedata
	 */
	public SPageData() {
		super();
		dataelements = new NamedList<DataElt>();

	}

	/**
	 * @param usermessage creates a user message
	 */
	public void setMessage(String usermessage) {
		this.usermessage = usermessage;
		this.popup = false;
	}

	/**
	 * @param usermessage creates a user message to be shown as popup
	 */
	public void setPopupMessage(String usermessage) {
		this.usermessage = usermessage;
		this.popup = true;
	}

	/**
	 * @param elt adds an element to the pagedata.
	 */
	public void addDataElt(DataElt elt) {
		this.dataelements.add(elt);
	}

	/**
	 * writes to an OpenLowcode message the payload of the page
	 * 
	 * @param writer writer
	 * @throws IOException if any problem is encountered sending the data
	 */
	public void writeToCML(MessageWriter writer) throws IOException {
		writer.startStructure("PAGEDATAS");
		for (int i = 0; i < dataelements.getSize(); i++) {
			writer.startStructure("PAGEDATA");
			dataelements.get(i).writeToMessage(writer, null);
			writer.endStructure("PAGEDATA");
		}

		writer.endStructure("PAGEDATAS");
		writer.addStringField("USM", usermessage);
		writer.addBooleanField("POP", popup);
	}

	/**
	 * @return the number of elements
	 */
	public int size() {
		return dataelements.getSize();
	}

	/**
	 * gets the attribute at given index
	 * 
	 * @param index an index between 0 (included) and size() (excluded)
	 * @return the data element, or throws an exception if out of range
	 */
	public DataElt getAttribute(int index) {
		return dataelements.get(index);
	}

	/**
	 * This method allows to add a prefix to all data element names
	 * 
	 * @param prefix prefix
	 */
	public void addPrefixToVariables(String prefix) {
		String finalprefix = prefix.toUpperCase() + ".";
		for (int i = 0; i < dataelements.getSize(); i++) {
			DataElt element = dataelements.get(i);
			element.changeName(finalprefix + element.getName());
		}
	}
}
