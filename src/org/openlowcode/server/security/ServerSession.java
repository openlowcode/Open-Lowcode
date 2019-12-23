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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ServerSession {
	private String ipaddress;
	private long timelastcontact;
	private String user;
	public String getIpaddress() {
		return ipaddress;
	}
	public long getTimelastcontact() {
		return timelastcontact;
	}
	public String getUser() {
		return user;
	}
	public ServerSession(String ipaddress, String user) {
		super();
		this.ipaddress = ipaddress;
		this.timelastcontact = new Date().getTime();
		this.user = user;
	}
	public void touch() {
		this.timelastcontact = new Date().getTime();
	}
	public boolean isTimeOut(long timeoutvalue) {
		long current = new Date().getTime();
		if (current-timelastcontact > timeoutvalue) return true;
		return false;
	}
	
}
