/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.io.IOException;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedInterface;

/**
 * an interface common to all property interfaces
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface DataObjectInterface {
	/**
	 * writes the object content
	 * 
	 * @param writer       writer to write
	 * @param hiddenfields list of hidden fields
	 * @param uid          unique id of the object
	 * @throws IOException if any transmission error is found during the
	 *                     transmission
	 */
	public void writeObjectContent(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields, String uid)
			throws IOException;
}
