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

import java.util.Date;

/**
 * Represents a page address that is kept in the history of pages visited.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AddressLink {
	private String clink;
	private String title;
	private Date date;
	private int visitnumber;

	/**
	 * @return the link to the action generating the page (typically a show object)
	 */
	public String getClink() {
		return clink;
	}

	/**
	 * @return the title of the page (as shown in the history widget)
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the date of the last visit
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the number of times the page was visited
	 */
	public int getVisitnumber() {
		return visitnumber;
	}

	/**
	 * @param clink the string link (includes the server address and the page action
	 *              address)
	 * @param title title of the page
	 */
	public AddressLink(String clink, String title) {
		super();
		this.clink = clink;
		this.title = title;
		this.date = new Date();
		this.visitnumber = 1;
	}

	/**
	 * updates the link with the fact it was just visisted. This helps order the
	 * pages visited by relevancy in the widget
	 */
	public void visit() {
		this.visitnumber++;
		this.date = new Date();
	}

}
