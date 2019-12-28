/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.security;

import java.util.Date;

/**
 * A session a user has with the server. The session will time-out if not used
 * for a given time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ServerSession {
	private String ipaddress;
	private long timelastcontact;
	private String user;

	/**
	 * @return the ip address the user connected from
	 */
	public String getIpaddress() {
		return ipaddress;
	}

	/**
	 * @return the time of the last contact
	 */
	public long getTimelastcontact() {
		return timelastcontact;
	}

	/**
	 * @return the user number
	 */
	public String getUser() {
		return user;
	}

	/**
	 * creates a ServerSession from the given user and ipaddress
	 * 
	 * @param ipaddress ip address the user connected from
	 * @param user      unique id of the user
	 */
	public ServerSession(String ipaddress, String user) {
		super();
		this.ipaddress = ipaddress;
		this.timelastcontact = new Date().getTime();
		this.user = user;
	}

	/**
	 * 
	 */
	public void touch() {
		this.timelastcontact = new Date().getTime();
	}

	/**
	 * @param timeoutvalue
	 * @return
	 */
	public boolean isTimeOut(long timeoutvalue) {
		long current = new Date().getTime();
		if (current - timelastcontact > timeoutvalue)
			return true;
		return false;
	}

}
