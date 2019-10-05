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

/**
 * This is an interface to a class implementing the catalog of widgets for the
 * client. The interface has been built for the sake of modularity
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface CPageNodeCatalog {

	/**
	 * @param code   code of the page node
	 * @param reader message reader
	 * @param path   current path in page parsing
	 * @return the parsed page node
	 * @throws IOException        if anything bad happens during the transmission
	 * @throws OLcRemoteException if anything bad happens on the server while
	 *                            treating the request
	 */
	public CPageNode getNodeFromCode(String code, MessageReader reader, CPageSignifPath path)
			throws IOException, OLcRemoteException;
}
