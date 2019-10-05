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
 * Feedback from trying to display a pgage. Wraps the potential error message,
 * the page text, and the message length
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DisplayPageFeedback {
	private String errormessage;
	private long messagelength;
	private String pagecontenttoarchive;

	/**
	 * @return the page source
	 */
	public String getPagecontenttoarchive() {
		return pagecontenttoarchive;
	}

	/**
	 * @return source of the page
	 */
	public String getErrormessage() {
		return errormessage;
	}

	/**
	 * @return length of the message in bytes
	 */
	public long getMessagelength() {
		return messagelength;
	}

	/**
	 * @param errormessage         error message (or null if no error)
	 * @param messagelength        length of the message in bytes
	 * @param pagecontenttoarchive source of the page
	 */
	public DisplayPageFeedback(String errormessage, long messagelength, String pagecontenttoarchive) {
		super();
		this.errormessage = errormessage;
		this.messagelength = messagelength;
		this.pagecontenttoarchive = pagecontenttoarchive;
	}

}