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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.ObjectIdDataElt;

/**
 * Data sent to the server to execute an action
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SActionData {
	private NamedList<DataElt> actiondata;

	/**
	 * creates a void SActionData set
	 */
	public SActionData() {
		actiondata = new NamedList<DataElt>();

	}

	/**
	 * actions that have as input only object ids can be sent to the server through
	 * a CLink (equivalent to an http query with attribute)
	 * 
	 * @param name    name of the attribute
	 * @param payload payload of the attribute
	 */
	public void addCLinkAttribute(String name, String payload) {
		actiondata.add(new ObjectIdDataElt(name, payload));
	}

	/**
	 * adds a data element to this action data set
	 * 
	 * @param element the element to add
	 */
	public void addData(DataElt element) {
		actiondata.add(element);
	}

	/**
	 * creates an action data from an incoming message
	 * 
	 * @param reader the reader to get payload from
	 * @throws OLcRemoteException exception to be sent back to the client
	 * @throws IOException        any IO exception while decoding the message
	 */
	public SActionData(MessageReader reader) throws OLcRemoteException, IOException {
		this();
		reader.startStructureArray("ATTRIBUTE");
		while (reader.structureArrayHasNextElement("ATTRIBUTE")) {
			DataElt dataelement = DataElt.readFromCML(reader);
			this.actiondata.add(dataelement);
			reader.returnNextEndStructure("ATTRIBUTE");
		}
	}

	/**
	 * @return the number of data elements in this action data set
	 */
	public int size() {
		return actiondata.getSize();
	}

	/**
	 * gets the attribute at the specified index
	 * 
	 * @param index a number between 0 (included) and size (excluded)
	 * @return the requested data element, or an exception if out of range
	 */
	public DataElt getAttribute(int index) {
		if (index>=actiondata.getSize()) {
			StringBuffer actiondatadrop = new StringBuffer();
			for (int i=0;i<actiondata.getSize();i++) {
				DataElt element = actiondata.get(i);
				if (element!=null) actiondatadrop.append("["+i+":"+element.getName()+":"+element.getType());
				if (element==null) actiondatadrop.append("["+i+":NULL]");
			}
			throw new RuntimeException("Request out of range ("+index+"/"+actiondata.getSize()+"), drop of elements "+actiondatadrop);
		}
		return actiondata.get(index);
	}

	/**
	 * gets an attribute by name
	 * 
	 * @param name name of the attribute
	 * @return the attribute searched for, or null if it does not exist
	 */
	public DataElt lookupAttributeOnName(String name) {
		return actiondata.lookupOnName(name);
	}
}
