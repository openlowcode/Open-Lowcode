/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.Date;
import java.util.Hashtable;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.openlowcode.module.system.action.generated.AbsCreatesessionforuserAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.module.system.data.choice.AuthtypeChoiceDefinition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.tools.enc.OLcEncrypter;

/**
 * Creates a new session for the user after login with provided user and
 * password
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CreatesessionforuserAction
		extends
		AbsCreatesessionforuserAction {

	private static final long FREEZE_USER_MISS = 5000; // wait 5 seconds in case of bad user or password

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public CreatesessionforuserAction(SModule parent) {
		super(parent);
	}

	@Override
	public Usersession executeActionLogic(
			String user,
			String password,
			String clientip,
			String clientcid,
			Function<TableAlias, QueryFilter> datafilter) {
		Logger logger = Logger.getLogger("");
		logger.info("received connection attempt from user = " + user + ", password = " + password + ", clientip = "
				+ clientip + ", client cid = " + clientcid);
		Appuser[] userobjectarray = Appuser.getobjectbynumber(user);

		if (userobjectarray.length != 1) { // usernot existing
			// user not existing, return null after waiting
			try {
				Thread.sleep(FREEZE_USER_MISS);
				logger.info("request on user " + user + ", not existing");

			} catch (InterruptedException e) {
				logger.severe("Waiting after faulty login interrupted "+e.getMessage());
			}
			return null;

		}
		Appuser userobject = userobjectarray[0];
		// 1A - check password
		boolean ldap = false;
		if (userobject.getAuthtype() != null)
			if (userobject.getAuthtype().getStorageCode().equals(AuthtypeChoiceDefinition.get().LDAP.getStorageCode()))
				ldap = true;

		if (!ldap) {
			if (userobject.getPassword().compareTo(OLcEncrypter.getEncrypter().encryptStringOneWay(password)) != 0) {
				try {
					Thread.sleep(FREEZE_USER_MISS);
					logger.info("request on user " + user + ", wrong password");

				} catch (InterruptedException e) {

				}
				return null;

			}
		} else {
			try {
				if (password == null)
					return null;
				if (password.length() == 0)
					return null;
				Hashtable<String, Object> env = new Hashtable<String, Object>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				env.put(Context.PROVIDER_URL, OLcServer.getServer().getSecuritymanager().getLDAPConnectionString());
				env.put(Context.SECURITY_AUTHENTICATION, "simple");
				env.put(Context.SECURITY_PRINCIPAL, userobject.getLdapfullname());
				env.put(Context.SECURITY_CREDENTIALS, password);
				@SuppressWarnings("unused")
				DirContext ctx = new InitialDirContext(env);
			} catch (NamingException e) {
				try {
					Thread.sleep(FREEZE_USER_MISS);
					logger.info("request on user " + user + ", wrong password in LDAP " + e.getMessage());
					logger.info("full qualified name = '" + userobject.getLdapfullname() + "'");
					logger.info("LDAP connection string = '"
							+ OLcServer.getServer().getSecuritymanager().getLDAPConnectionString() + "'");

				} catch (InterruptedException e2) {

				}
				return null;
			}
		}
		// 2 - close other sessions
		logger.finer("Temporary log : appuser name = " + userobject.getNr() + ", " + userobject.getName());
		logger.finer("Temporary log : appuser id string = " + userobject.getId());

		Usersession[] previoussessions = Usersession.getallchildrenforsessionuser(userobject.getId(),
				QueryFilter.get(new SimpleQueryCondition<Date>(null,
						Usersession.getDefinition().getEndtimeFieldSchema(), new QueryOperatorEqual<Date>(), null)));
		if (previoussessions != null)
			for (int i = 0; i < previoussessions.length; i++) {
				Usersession thissession = previoussessions[i];
				if (thissession.getEndtime() == null) {
					thissession.setEndtime(thissession.getLastaction());
					thissession.update();
					logger.info("closed session for user " + thissession.getLinkedtoparentforsessionuserid()
							+ " session originally opened at " + thissession.getStarttime() + " on machine "
							+ thissession.getClientip());
				}
			}

		// 3 - create new usersession and return it
		Usersession newsession = new Usersession();
		newsession.setClientip(clientip);
		newsession.setClientpid(clientcid);
		newsession.setStarttime(new Date());
		newsession.setActions(1);
		newsession.setLastaction(new Date());
		newsession.setparentforsessionuser(userobject.getId());
		newsession.insert();

		return newsession;
	}

	@Override
	public SPage choosePage(Usersession usersession) {

		return OLcServer.getServer().getMainmodule().getDefaultPage();
	}

}
