/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.IOException;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * the spec of a page in buffer. It contains hashcode and size. Two pages with
 * hashcodes and sizes are assumed to be the same
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PageBufferSpec {
	private int hashcode;
	private int size;

	/**
	 * creates a page buffer spec from a message
	 * 
	 * @param reader reader stream on the message
	 * @throws IOException        if any communication issue happens
	 * @throws OLcRemoteException if something nasty happens on the other side of
	 *                            client server communication during the sending of
	 *                            the message
	 */
	public PageBufferSpec(MessageReader reader) throws IOException, OLcRemoteException {
		hashcode = reader.returnNextIntegerField("HSH");
		size = reader.returnNextIntegerField("SIZ");
		reader.returnNextEndStructure("PAGBUF");
	}

	/**
	 * @return gets the page hash code
	 */
	public int getContentHashcode() {
		return hashcode;
	}

	/**
	 * @return get the page size
	 */
	public int getSize() {
		return size;
	}

}
