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
import java.io.StringReader;

import org.openlowcode.client.runtime.PageBuffer;
import org.openlowcode.client.runtime.PageInBuffer;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.DataElt;

/**
 * A page of the application. It contains layout element (CPageNode) that can
 * hold data, and action that use the data on the page to either launch a new
 * page (action) or enrich the data on the page (inline action). The page is
 * sent by the server.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPage extends Named {
	private CPageNode node;
	private CPageData inputdata;
	private CPageSignifPath rootpath;
	private String pagedescription;
	private long buffereddataused = 0;

	public String getPagedescription() {
		return this.pagedescription;
	}

	/**
	 * @return the number of bytes of information for this page. This is used by the
	 *         management of buffer for pages
	 */
	public long getBufferedDataUsed() {
		return this.buffereddataused;
	}

	/**
	 * creates and parses a page, and its associated data
	 * 
	 * @param name       name of the page
	 * @param reader     Message reader that will be used to parse the page info
	 *                   from the Content tab
	 * @param module     module of the action calling the page
	 * @param action     name of the action calling the page
	 * @param pagebuffer the local page buffer from which the page content will be
	 *                   got from, in case the page is already in local buffer (in
	 *                   that case, the server sends characteristics (size,
	 *                   checksum) of the page to use
	 * @throws OLcRemoteException if anything bad happened on the server when
	 *                            sending the page
	 * @throws IOException        if anything bad happens during the transmission
	 */
	public CPage(String name, MessageReader reader, String module, String action, PageBuffer pagebuffer)
			throws OLcRemoteException, IOException {
		super(name);
		rootpath = new CPageSignifPath(this);
		buffereddataused = 0;
		reader.returnNextStartStructure("CONTENT");
		boolean hascontentbuffer = reader.returnNextBooleanField("CTB");
		if (hascontentbuffer) {

			int hashcode = reader.returnNextIntegerField("HSH");
			int size = reader.returnNextIntegerField("SIZ");
			PageInBuffer bufferedpage = pagebuffer.getBufferPageFor(module, action, hashcode, size);
			buffereddataused = bufferedpage.getPagesize();
			MessageReader bufferedreader = new MessageSimpleReader(new StringReader(bufferedpage.getCompletepage()));
			bufferedreader.returnNextMessageStart();
			this.node = CPageNode.parseNode(bufferedreader, rootpath);
			bufferedreader.returnNextEndMessage();

		} else {
			reader.startrecord();
			this.node = CPageNode.parseNode(reader, rootpath);
			pagedescription = reader.endrecord();
		}
		reader.returnNextEndStructure("CONTENT");

		inputdata = new CPageData(reader);

	}

	/**
	 * creates a blank page with the given name
	 * 
	 * @param name unique name of the page in the module
	 */
	public CPage(String name) {
		super(name);
		rootpath = new CPageSignifPath(this);
	}

	/**
	 * @param pagenode sets the root node of the page
	 */
	public void setPageNode(CPageNode pagenode) {
		this.node = pagenode;
	}

	/**
	 * @return the root node of the page
	 */
	public CPageNode getNode() {
		return node;
	}

	/**
	 * @return the data associated to the page
	 */
	public CPageData getAllInputData() {
		return this.inputdata;
	}

	/**
	 * @return the number of data elements on the page
	 */
	public int getInputDataSize() {
		return this.inputdata.getElementNumber();
	}

	/**
	 * @param index index of the data element to get
	 * @return the data element at index
	 */
	public DataElt getInputData(int index) {
		return this.inputdata.getDataElement(index);
	}

	/**
	 * @return the root page element
	 */
	public CPageSignifPath getRootPath() {
		return this.rootpath;
	}

	/**
	 * @param path a path
	 * @return the page node at this path
	 */
	public CPageNode getNodeAtSignificantPath(String path) {
		return rootpath.getNodeAtPath(path);
	}

	/**
	 * @param module  module of the action
	 * @param name    name of the action
	 * @param newdata new data to add to the page
	 */
	public void processInlineAction(String module, String name, CPageData newdata) {
		this.inputdata.processInlineAction(module, name, newdata);
	}
}
