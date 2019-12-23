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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.CreatesessionforuserAction;
import org.openlowcode.module.system.action.GetsessionforclientAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.runtime.OLcServer;

/**
 * The central class ensuring users connected are registered
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SecurityManager extends Thread {
	Logger logger = Logger.getLogger("");
	private static long TIMEOUTINMS = 60000;
	private String ldapconnectionstring = null;
	private String ldapuser = null;
	private String ldappassword = null;
	public HashMap<String, ServerSession> sessionsbyip;

	/**
	 * @return the LDAP connection string
	 */
	public String getLDAPConnectionString() {
		return this.ldapconnectionstring;
	}

	/**
	 * @return the LDAP user
	 */
	public String getLDAPUser() {
		return this.ldapuser;
	}

	/**
	 * @return the LDAP password
	 */
	public String getLDAPPassword() {
		return this.ldappassword;
	}

	/**
	 * creates a blank security manager with coordinates for connection to LDAP when
	 * required (typically to check password, LDAP user password is not stored on
	 * the database). There should be one instance of the security manager per
	 * server. This class will launch a background daemon refreshing regularly the
	 * security cache.
	 * 
	 * @param ldapconnectionstring connection string to the enterprise LDAP
	 * @param ldapuser             service user to connect to the enterprise LDAP
	 * @param ldappassword         service password to connect to the enterprise
	 *                             LDAP
	 */
	public SecurityManager(String ldapconnectionstring, String ldapuser, String ldappassword) {
		sessionsbyip = new HashMap<String, ServerSession>();
		ServerSecurityBuffer.getUniqueInstance();
		this.ldapconnectionstring = ldapconnectionstring;
		this.ldapuser = ldapuser;
		this.ldappassword = ldappassword;
	}

	/**
	 * @return a list of authorities for the current user, null if user is not
	 *         authorized
	 */
	public Authority[] getAuthoritiesForCurrentUser() {

		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();

		if (userid == null)
			return null;
		// TODO maybe better to format with user id
		Usergroup[] usergroups = ServerSecurityBuffer.getUniqueInstance().getGroupsForUser(userid);
		if (usergroups == null)
			return null;
		logger.info(" --- found " + usergroups.length + " groups for userid = " + userid.getId());
		ArrayList<Authority> authoritieslist = new ArrayList<Authority>();
		for (int i = 0; i < usergroups.length; i++) {
			Usergroup thisgroup = usergroups[i];
			Authority[] authoritiesforgroup = ServerSecurityBuffer.getUniqueInstance()
					.getAuthoritiesForGroup(thisgroup.getId());
			if (authoritiesforgroup != null)
				for (int j = 0; j < authoritiesforgroup.length; j++) {
					authoritieslist.add(authoritiesforgroup[j]);
				}
		}
		logger.info(" --- found " + authoritieslist.size() + " authorities for userid = " + userid.getId());
		return authoritieslist.toArray(new Authority[0]);
	}

	/**
	 * This method is low performance way to get the user, and check that there is a
	 * valid session. This method should only be called from the GalliumConnection
	 * management of user authorization for action. Indeed, the method is low
	 * performance and
	 * 
	 * @param ipaddress the ip address of the connection
	 * @return null String if no session is found, else returns the user id
	 */
	public DataObjectId<Appuser> isValidSession(String ipaddress, String cid) {
		Usersession session = GetsessionforclientAction.get().executeActionLogic(ipaddress, cid, null);

		if (session == null) {
			logger.info("for ip address = " + ipaddress + ", no session exists");
			OLcServer.getServer().setUserIdForConnection(null);
			return null;
		}
		// TODO - check if good way to return user info
		DataObjectId<Appuser> userid = session.getLinkedtoparentforsessionuserid();
		OLcServer.getServer().setUserIdForConnection(userid);
		return userid;
	}

	/**
	 * creates a session if user is valid
	 * 
	 * @param ipaddress address the user is connecting from
	 * @param cid       id of the client on the user machine (to manage several
	 *                  connections for the same user, especially during tests)
	 * @param user      user the client want to login with
	 * @param password  password of the client user
	 * @return the session if user / password valid, null else
	 */
	public Usersession createSession(String ipaddress, String cid, String user, String password) {
		Usersession session = CreatesessionforuserAction.get().executeActionLogic(user, password, ipaddress, cid, null);
		if (session != null)
			OLcServer.getServer().setUserIdForConnection(session.getLinkedtoparentforsessionuserid());
		if (session == null)
			OLcServer.getServer().setUserIdForConnection(null);
		return session;
	}

	@Override
	public void run() {
		try {

			while (true) {
				// separate in 2 to ensure their are not done at same time as mail
				Thread.sleep(TIMEOUTINMS / 2);
				try {
					ServerSecurityBuffer.getUniqueInstance().refreshData();

				} catch (Exception e) {
					logger.warning("-- Security buffer refresh: fatal error " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.warning("   " + e.getStackTrace()[i]);
				}

				Thread.sleep(TIMEOUTINMS / 2);

			}
		} catch (InterruptedException e) {
			logger.warning("Security buffer refresh interrupted: " + e.getMessage());
		}

	}
}
