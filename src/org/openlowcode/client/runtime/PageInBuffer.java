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

/**
 * A page information as stored in the page buffer. The page buffer is
 * used to avoid asking the server to send presentation information on
 * the page to display 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class PageInBuffer {
	private String module;
	private String action;
	private String completepage;

	private int completepagehashcode;
	private int pagesize;
	/**
	 * @param module the Open Lowcode module name
	 * @param action the Open Lowcode module action
	 * @param completepage the text in Open Lowcode ML of the page received
	 * with the action
	 */
	public PageInBuffer(String module, String action, String completepage) {
		super();
		this.module = module;
		this.action = action;
		this.completepage = completepage;
		
		this.completepagehashcode = completepage.hashCode();
		this.pagesize=completepage.length();
	}
	/**
	 * 
	 * @return a key uniquely combining module and action
	 */
	public String generateSearchKey() {
		return generateSearchKey(module,action);
	}
	/**
	 * generates the key as used internally in the PageInBuffer. This is useful
	 * to query if a page exits when storing the object in a hashmap
	 * @param module name of the module
	 * @param action name of the action
	 * @return
	 */
	protected static String generateSearchKey(String module,String action) {
		return module+"."+action;
	}
	/**
	 * @return the name of the module
	 */
	public String getModule() {
		return module;
	}
	/**
	 * @return the name of the action
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @return the source of the complete page
	 */
	public String getCompletepage() {
		return completepage;
	}
	
	/**
	 * @return a hashcode for the page source
	 */
	public int getCompletepagehashcode() {
		return completepagehashcode;
	}
	/**
	 * @return the size of the page source
	 */
	public int getPagesize() {
		return pagesize;
	}
	
}

