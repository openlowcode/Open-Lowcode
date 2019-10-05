/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic;

import java.io.IOException;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.structure.DataEltType;


/**
 * The reference to page data present in a page node
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class CPageDataRef {
	private String name;
	private DataEltType type;
	private CPageDataRef(String name,DataEltType type) {
		this.name = name;
		this.type=type;
	}
	/**
	 * @param reader message reader
	 * @return a Page Data Reference
	 * @throws OLcRemoteException if anything bad happens on the server during the transmission
	 * @throws IOException if any error happens during transmission with server
	 */
	public static CPageDataRef parseCPageDataRef(MessageReader reader) throws OLcRemoteException, IOException {
		reader.returnNextStartStructure("DATAREF");
		String name = reader.returnNextStringField("NAM");
		DataEltType type = DataEltType.getDataEltType(reader.returnNextStringField("TYP"));
		reader.returnNextEndStructure("DATAREF");
		return new CPageDataRef(name,type);
	}
	/**
	 * @return name of the data element
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return type of the data element
	 */
	public DataEltType getType() {
		return type;
	}
	
}
