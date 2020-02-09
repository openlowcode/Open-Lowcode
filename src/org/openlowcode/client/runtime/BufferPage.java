/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

/**
 * A page buffer kept on the client.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class BufferPage {
	private String module;
	private String action;
	private String completepage;

	private int completepagehashcode;
	private int pagesize;

	/**
	 * create a Buffer Page
	 * 
	 * @param module       name of the module
	 * @param action       name of the action
	 * @param completepage string message of the complete page
	 */
	public BufferPage(String module, String action, String completepage) {
		super();
		this.module = module;
		this.action = action;
		this.completepage = completepage;

		this.completepagehashcode = completepage.hashCode();
		this.pagesize = completepage.length();
	}

	/**
	 * @return the search key of the page (made of module and action name)
	 */
	public String generateSearchKey() {
		return generateSearchKey(module, action);
	}

	/**
	 * search key for module and action name
	 * 
	 * @param module module name
	 * @param action action name
	 * @return generated single string search string
	 */
	public static String generateSearchKey(String module, String action) {
		return module + "." + action;
	}

	/**
	 * @return the module name
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @return the action name
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @return the complete page
	 */
	public String getCompletepage() {
		return completepage;
	}

	/**
	 * @return the complete page hash code
	 */
	public int getCompletepagehashcode() {
		return completepagehashcode;
	}

	/**
	 * @return the complete page size
	 */
	public int getPagesize() {
		return pagesize;
	}

}
