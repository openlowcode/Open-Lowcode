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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.openlowcode.module.system.action.generated.AbsQueryforuserAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Ldapuser;
import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Action querying for a user in the LDAP directory
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class QueryforuserAction
		extends
		AbsQueryforuserAction {
	private static Logger logger = Logger.getLogger(QueryforuserAction.class.getName());

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public QueryforuserAction(SModule parent) {
		super(parent);
	}

	@Override
	public Ldapuser[] executeActionLogic(
			String userid,
			String userpasword,
			String ldapbase,
			String lastnamestartsby,
			String idstartsby,
			Function<TableAlias, QueryFilter> datafilter) {
		Hashtable<String, String> env = new Hashtable<String, String>();
		Systemattribute[] attribute = Systemattribute.getobjectbynumber(LaunchadduserAction.LDAPBASEPARAMETER);
		boolean exists = false;
		if (attribute != null)
			if (attribute.length == 1) {
				exists = true;
				Systemattribute thisattribute = attribute[0];
				if (!thisattribute.getValue().equals(ldapbase)) {
					thisattribute.setValue(ldapbase);
					thisattribute.setupdatenote("Update from QueryforuserAction");
					thisattribute.update();
				}

			}
		if (!exists) {
			Systemattribute newattribute = new Systemattribute();
			newattribute.setobjectnumber(LaunchadduserAction.LDAPBASEPARAMETER);
			newattribute.setValue(ldapbase);
			newattribute.setupdatenote("Creation by QueryforuserAction");
			newattribute.insert();
		}

		String sp = "com.sun.jndi.ldap.LdapCtxFactory";
		env.put(Context.INITIAL_CONTEXT_FACTORY, sp);
		String ldapUrl = OLcServer.getServer().getSecuritymanager().getLDAPConnectionString();
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		String connectioncontext = OLcServer.getServer().getSecuritymanager().getLDAPUser();
		if (userid != null)
			if (userid.length() > 0) {
				Appuser[] user = Appuser.getobjectbynumber(userid);
				if (user != null)
					if (user.length == 1)
						connectioncontext = user[0].getLdapfullname();
			}
		String finaluserpassword = OLcServer.getServer().getSecuritymanager().getLDAPPassword();
		if (userpasword != null)
			if (userpasword.length() > 0)
				finaluserpassword = userpasword;
		env.put(Context.SECURITY_PRINCIPAL, connectioncontext);
		env.put(Context.SECURITY_CREDENTIALS, finaluserpassword);
		DirContext ldapContext;
		try {
			ldapContext = new InitialDirContext(env);
		} catch (NamingException ne) {
			throw new RuntimeException("Cannot connect to LDAP with user '" + connectioncontext
					+ "' and given password, LDAP Naming Exception = " + ne.getMessage());

		}
		ArrayList<Ldapuser> userlist = new ArrayList<Ldapuser>();
		String base = ldapbase;
		String search = "(&(mailNickname=" + idstartsby + "*)(sn=" + lastnamestartsby + "*))";
		try {

			SearchControls sc = new SearchControls();
			String[] attributeFilter = { "cn", "mail", "mailNickname", "distinguishedName", "department", "homePhone",
					"givenName", "sn", "roomNumber", "mobile" };
			sc.setReturningAttributes(attributeFilter);
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

			@SuppressWarnings("rawtypes")
			NamingEnumeration results = ldapContext.search(base, search, sc);
			while (results.hasMore()) {

				SearchResult sr = (SearchResult) results.next();
				Attributes attrs = sr.getAttributes();
				Ldapuser newuser = new Ldapuser();
				newuser.setDistinguishedname(extractAttributeIfPresent(attrs, "distinguishedName"));
				newuser.setId(extractAttributeIfPresent(attrs, "mailNickname"));
				newuser.setMail(extractAttributeIfPresent(attrs, "mail"));
				newuser.setLastname(extractAttributeIfPresent(attrs, "sn"));
				newuser.setFirstname(extractAttributeIfPresent(attrs, "givenName"));
				newuser.setDepartment(extractAttributeIfPresent(attrs, "department"));
				newuser.setLocation(extractAttributeIfPresent(attrs, "roomNumber"));
				newuser.setFixphone(extractAttributeIfPresent(attrs, "homePhone"));
				newuser.setMobilephone(extractAttributeIfPresent(attrs, "mobile"));
				userlist.add(newuser);

			}
			return userlist.toArray(new Ldapuser[0]);
		} catch (NamingException e) {
			logger.severe("-------------------------------------------------------------------");
			logger.severe("Naming Exception while querying LDAP " + e.getExplanation());
			logger.severe("Explanation " + e.getExplanation());
			logger.severe("Remaining name " + e.getRemainingName());
			logger.severe("Resolved name " + e.getResolvedName());
			logger.severe("Resolved Object " + e.getResolvedObj());
			logger.severe("Cause: " + e.getCause());
			logger.severe("Root Cause: " + e.getRootCause());
			logger.severe("---------------------------------------------------------------------");
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe("   " + e.getStackTrace()[i]);

			throw new RuntimeException("Error during user query for base = '" + base + "' & search key = '" + search
					+ "' at " + e.getStackTrace()[0] + " - " + e.getMessage());
		}

	}

	/**
	 * A utility class extracting LDAP attributes, not throwing an exception if the
	 * attribute does not exist
	 * 
	 * @param attrs         list of attributes
	 * @param attributename attribute name
	 * @return the attribute if it exists, empty string else
	 */
	public static String extractAttributeIfPresent(Attributes attrs, String attributename) {
		try {
			Attribute attribute = attrs.get(attributename);
			if (attribute != null) {
				Object attributepayload = attribute.get();
				if (attributepayload != null)
					return attributepayload.toString();
			}
		} catch (NamingException e) {
			logger.severe("Exception getting attribute " + attributename + " - " + e.getMessage());
		}
		return "";
	}

	@Override
	public SPage choosePage(Ldapuser[] ldapusers) {
		throw new RuntimeException("This page should only be accessed through inline query");
	}

}
