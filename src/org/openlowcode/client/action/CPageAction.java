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
import java.util.logging.Logger;

import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;

/**
 * An action declared on a page
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPageAction extends Named {
	private static Logger logger = Logger.getLogger(CPageAction.class.getName());
	private NamedList<CActionDataLoc> businessdataloc;
	private String module;

	/**
	 * @return name of the module of the action
	 */
	public String getModule() {
		return module;
	}

	public CPageAction(MessageReader reader) throws OLcRemoteException, IOException {
		super(reader.returnNextStringField("NAME"));
		this.module = reader.returnNextStringField("MODULE");
		businessdataloc = new NamedList<CActionDataLoc>();
		reader.startStructureArray("ACTIONDATA");
		while (reader.structureArrayHasNextElement("ACTIONDATA")) {
			CActionDataLoc dataloc = new CActionDataLoc(reader);
			businessdataloc.add(dataloc);
			logger.fine("parsed Action Data " + dataloc.getName());
			reader.returnNextEndStructure("ACTIONDATA");

		}
		reader.returnNextEndStructure("ACTION");

	}

	/**
	 * @param page the page to extract data from
	 * @return the data of this action, that is extracted from the page
	 */
	public CActionData getDataContent(CPage page) {
		CActionData result = new CActionData();
		for (int i = 0; i < businessdataloc.getSize(); i++) {
			CActionDataLoc thisbusinessdataloc = businessdataloc.get(i);
			if (thisbusinessdataloc.getPath() != null) {
				CPageNode originnode = page.getNodeAtSignificantPath(thisbusinessdataloc.getPath());
				DataElt businessdataelt = originnode.getDataElt(
						DataEltType.getDataEltType(thisbusinessdataloc.getType()), thisbusinessdataloc.getName(),
						thisbusinessdataloc.getObjectField());
				result.addActionAttribute(businessdataelt);
			} else {
				DataElt nullbusinessdataelt = DataEltType.createNullDataElt(thisbusinessdataloc.getName(),
						thisbusinessdataloc.getType());
				result.addActionAttribute(nullbusinessdataelt);
			}
		}
		return result;
	}

	/**
	 * @return a signature of type MODULE.NAME
	 */
	public String key() {
		return this.module + "." + this.getName();
	}

	/** checks if the nodepath exists in one of the business data locations
	 * @param nodepath the nodepath to check
	 * @return true
	 */
	public boolean includesnodepath(String nodepath) {
		for (int i = 0; i < businessdataloc.getSize(); i++) {
			CActionDataLoc thisbusinessdataloc = businessdataloc.get(i);
			logger.fine("            - " + thisbusinessdataloc.getPath() + " - " + nodepath);
			if (thisbusinessdataloc.getPath() != null) {
				if (nodepath.equals(thisbusinessdataloc.getPath()))
					return true;
			}
		}
		return false;
	}
}
