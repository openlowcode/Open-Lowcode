/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.action;

import java.io.IOException;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DataElt;

/**
 * CActionData represents a set of attributes that will be sent to the server as
 * a result of an action. This data is queried from a page.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CActionData {

	private NamedList<DataElt> actionattributes;

	/**
	 * Creates an empty CActionData without any argument
	 */
	public CActionData() {
		super();
		actionattributes = new NamedList<DataElt>();
	}

	/**
	 * @return the number of attributes in this CActionData
	 */
	public int getAttributesNumber() {
		return actionattributes.getSize();
	}

	/**
	 * @param number a number between 0 (included) and AttributeNumber (excluded
	 * @return the data element at the specified index.
	 */
	public DataElt getElementAt(int number) {
		return actionattributes.get(number);
	}

	/**
	 * Adds a data element at the end of the list of data elements for this action
	 * data
	 * 
	 * @param dataelement data element to be added
	 */
	public void addActionAttribute(DataElt dataelement) {
		actionattributes.add(dataelement);
	}

	/**
	 * Writes
	 * 
	 * @param writer an OpenLowcode Message writer
	 * @throws IOException if any exception is received trying to transmit the
	 *                     message
	 */
	public void writeToMessage(MessageWriter writer) throws IOException {
		writer.startStructure("ATTRIBUTES");
		for (int i = 0; i < actionattributes.getSize(); i++) {
			writer.startStructure("ATTRIBUTE");
			actionattributes.get(i).writeToMessage(writer, null);
			writer.endStructure("ATTRIBUTE");
		}
		writer.endStructure("ATTRIBUTES");
	}
}
