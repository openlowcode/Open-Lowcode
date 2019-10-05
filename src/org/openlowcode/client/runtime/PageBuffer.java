/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.StringExtremityPrinter;

/**
 * A buffer holding all pages received by the client, so that it does not 
 * have to be sent again by the server, to save bandwidth.
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */

public class PageBuffer {

	private long totalbuffersize;
	private static Logger logger = Logger.getLogger(PageBuffer.class.getName());

	/**
	 * gets from the buffer the page with the matching properties
	 * 
	 * @param module   name of the module
	 * @param action   name of the action
	 * @param hashcode hashcode of the page source
	 * @param size     size of the page source
	 * @return the page found. If no page is found, a RuntimeException is thrown as
	 *         this is not a normal behaviour
	 */
	public PageInBuffer getBufferPageFor(String module, String action, int hashcode, int size) {

		String searchkey = PageInBuffer.generateSearchKey(module, action);
		ArrayList<PageInBuffer> bufferforaction = buffercontent.get(searchkey);
		if (bufferforaction == null)
			throw new RuntimeException("Did not find a buffer for module = " + module + " for action = " + action);
		for (int i = 0; i < bufferforaction.size(); i++) {
			PageInBuffer thispage = bufferforaction.get(i);
			if ((thispage.getCompletepagehashcode() == hashcode) && (thispage.getPagesize() == size)) {
				return thispage;
			} else {
				logger.severe("    * Page for (" + module + "/" + action + ") no match hashcode = "
						+ thispage.getCompletepagehashcode() + "/" + hashcode + ", size = " + thispage.getPagesize()
						+ "/" + size);
			}
		}
		throw new RuntimeException("Did not find a page for module " + module + " for action = " + action
				+ ", looked at " + bufferforaction.size() + " buffered pages ");
	}

	private HashMap<String, ArrayList<PageInBuffer>> buffercontent;

	/**
	 * Creates an empty page buffer
	 */
	public PageBuffer() {
		totalbuffersize = 0;
		buffercontent = new HashMap<String, ArrayList<PageInBuffer>>();
	}

	/**
	 * Adds the page to the buffer. Note: the buffer does not have a mechanism to
	 * ensure a duplicate is added. This should be done by the caller.
	 * 
	 * @param page the page to be added
	 */
	public void addPageToBuffer(PageInBuffer page) {
		String searchkey = page.generateSearchKey();
		ArrayList<PageInBuffer> pages = buffercontent.get(searchkey);
		if (pages == null) {
			pages = new ArrayList<PageInBuffer>();
			buffercontent.put(searchkey, pages);
		}
		logger.fine(" *  Page Buffer : for action " + searchkey + " : adds page hashcode="
				+ page.getCompletepagehashcode() + ", size=" + page.getPagesize() + " at index " + pages.size());

		logger.fine(StringExtremityPrinter.printextremity(page.getCompletepage(), 15));
		logger.fine("----------------------------------------------------");
		for (int i = 0; i < pages.size(); i++)
			logger.fine("      * old page index = " + i + " hashcode = " + pages.get(i).getCompletepagehashcode()
					+ ", size = " + pages.get(i).getPagesize());
		pages.add(page);
		totalbuffersize += page.getPagesize();
	}

	/**
	 * writes a description of all pages in the buffer for the given module and
	 * action, in a way that can be understood by the Open Lowcode server
	 * 
	 * @param actionmodule name of the action
	 * @param actionname   name of the module
	 * @param writer       writer to put elements to
	 * @throws IOException if any communication issue happens
	 */
	public void writeBufferedPages(String actionmodule, String actionname, MessageWriter writer)
			throws IOException {
		String key = PageInBuffer.generateSearchKey(actionmodule, actionname);
		ArrayList<PageInBuffer> pages = buffercontent.get(key);
		writer.startStructure("PAGBUFS");
		if (pages != null)
			for (int i = 0; i < pages.size(); i++) {
				PageInBuffer thispagebuffer = pages.get(i);
				writer.startStructure("PAGBUF");
				writer.addIntegerField("HSH", thispagebuffer.getCompletepagehashcode());
				writer.addIntegerField("SIZ", thispagebuffer.getPagesize());

				writer.endStructure("PAGBUF");
			}
		writer.endStructure("PAGBUFS");

	}

	/**
	 * This method is used to display the total size of the buffer. As it is held in
	 * memory, it could crash the client if it became too big
	 * 
	 * @return the size all pages in buffer in bytes
	 */
	public long getTotalBufferSize() {
		return this.totalbuffersize;
	}
}
