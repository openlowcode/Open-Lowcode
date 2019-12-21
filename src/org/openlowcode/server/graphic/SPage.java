/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic;

import org.openlowcode.tools.misc.Named;

/**
 * a page to display on the client. The page is defined on the server as SPage,
 * and the layout is described. The layout is then sent to the client to be
 * displayed, similarly to an HTML client.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SPage extends Named {
	private SPageSignifPath rootpath;
	private SPageAddon addon;
	private String address;
	private String title;

	/**
	 * @param title the title of the page, typically shown in the frame of the
	 *              client
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * defines the address of the page. Can be used to call the page directly from
	 * the client
	 * 
	 * @param address the string representing the address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * recreates the root path of the page
	 */
	public void resetPath() {
		this.rootpath = new SPageSignifPath("ROOT", null, null, new SPageNode[0]);
	}

	/**
	 * creates a SPage with empty content
	 * 
	 * @param name name of the page (unique id in the module)
	 */
	public SPage(String name) {
		super(name);
		this.title = null;
		this.address = null;
		this.rootpath = new SPageSignifPath("ROOT", null, null, new SPageNode[0]);

	}

	/**
	 * Creates a SPage with name and title
	 * 
	 * @param name  name of the page (unique id in the module)
	 * @param title title to display
	 */
	public SPage(String name, String title) {
		super(name);
		this.title = title;
		this.rootpath = new SPageSignifPath("ROOT", null, null, new SPageNode[0]);
		this.address = null;
	}

	/**
	 * Creates a SPage with name and title and address
	 * 
	 * @param name    name of the page (unique id in the module)
	 * @param title   title to display
	 * @param address address to call the page
	 */
	public SPage(String name, String title, String address) {
		super(name);
		this.title = title;
		this.address = address;
		this.rootpath = new SPageSignifPath("ROOT", null, null, new SPageNode[0]);
	}

	/**
	 * a Page addon is a frame on which the main page payload is added. Typically,
	 * the page add-on adds the application menu to the given page
	 * 
	 * @param addon the page add-on
	 */
	public void addAddon(SPageAddon addon) {
		this.addon = addon;
		this.addon.setInternalContent(this);
	}

	/**
	 * This method has to be implemented 
	 * @return all the page attributes
	 */
	protected abstract SPageData getAllPageAttributes();

	/**
	 * @return all the page attributes, including the add-on attributes
	 */
	public SPageData getAllFinalPageAttributes() {
		if (addon == null)
			return getAllPageAttributes();
		return addon.getAllPageAttributes();
	}

	/**
	 * @return the content of the page
	 */
	protected abstract SPageNode getContent();

	/**
	 * @return the content of the page and the page add-on
	 */
	public SPageNode getFinalContent() {
		SPageNode answer;

		if (addon == null) {
			answer = getContent();
		} else {
			answer = addon.getContent();
		}
		answer.populateDown(this.getRootPath(), new SPageNode[0]);
		return answer;
	}

	/**
	 * @return the root path of the page
	 */
	public SPageSignifPath getRootPath() {
		return this.rootpath;
	}

	/**
	 * title is a short string giving an identification of the displayed object when
	 * relevant
	 * 
	 * @return a string
	 * 
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * an address is a string that allows direct access to a page. this method
	 * returns whether this page has an address or not (this is not always possible)
	 * 
	 * @return true if an address exists for this page, false if no address exists
	 *         for this page
	 * 
	 */
	public boolean hasAdress() {
		if (this.address == null)
			return false;
		return true;
	}

	/**
	 * an address is a string that allows direct access to a page. this method
	 * returns the address is hasaddress is true, and throws an exception if called
	 * else
	 * 
	 * @return the address string, not including the server IP address.
	 * 
	 */
	public String getAddress() {
		if (this.address == null)
			throw new RuntimeException("address is requested although it is null");
		return this.address;
	}

}
