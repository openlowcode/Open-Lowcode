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
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;

import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A total security manager grants total access to the given authorities to the
 * action without any additional condition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ActionTotalSecurityManager extends ActionSecurityManager {
	private ArrayList<String> relevantauthorities; // stores the authorities number
	private static final Logger logger = Logger.getLogger("");
	private HashMap<String, String> orderedauthorities;

	@Override
	public boolean queryObjectData() {
		return false;
	}

	@Override
	public String toString() {
		String returnstring = "ActionTotalSecurityManager:";
		for (int i = 0; i < relevantauthorities.size(); i++)
			returnstring += relevantauthorities.get(i) + ":";
		return returnstring;
	}

	@Override
	public boolean filterObjectData() {

		return false;
	}

	/**
	 * Creates a total security manager with the set authorities defined in the
	 * fillRelevant Authorities method
	 */
	public ActionTotalSecurityManager() {
		relevantauthorities = new ArrayList<String>();
		fillRelevantAuthorities(relevantauthorities);
		this.orderedauthorities = new HashMap<String, String>();
		for (int i = 0; i < relevantauthorities.size(); i++) {
			this.orderedauthorities.put(relevantauthorities.get(i), relevantauthorities.get(i));
		}
	}

	@Override
	public boolean isAuthorizedForCurrentUser(String context, SActionData input, SecurityBuffer buffer) {
		logger.info(" --- for " + context
				+ " checking authorization for current user through CSPActionTotalSecurityManager class "
				+ this.getClass().getName() + "---");
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();

		if (userauthorities == null) {
			logger.info("no authority returned for current session, not authorized");
			return false;
		}
		for (int i = 0; i < userauthorities.length; i++) {
			if (isRelevantAuthority(userauthorities[i].getNr())) {
				logger.info("--- access granted through authority " + userauthorities[i].getNr());
				return true;
			}

		}
		logger.info("--- despite checking user " + userauthorities.length + " authorities against "
				+ this.orderedauthorities.size() + ", no match was found");
		logger.fine("     - user autorities " + buildListSummary(userauthorities));
		logger.fine("     - authoritized " + buildListSummary(relevantauthorities));
		return false;
	}

	public boolean isRelevantAuthority(String authoritynumber) {
		return this.orderedauthorities.containsKey(authoritynumber);
	}

	/**
	 * Storage of allowed authorities is performed by number. While this could be
	 * more types, the number string is a compromise as it does not require a query
	 * to initialize the object and is independent of runtime (contrary to id).
	 * 
	 * @param relevantauthorities an empty arraylist created by superclassthat has
	 *                            to be filled by the method
	 */
	public abstract void fillRelevantAuthorities(ArrayList<String> relevantauthorities);

	@Override
	public Function<TableAlias, QueryFilter> getOutputFilterCondition() {

		return null;
	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer) {
		boolean authorized = false;
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();

		if (userauthorities != null)
			for (int i = 0; i < userauthorities.length; i++) {
				if (isRelevantAuthority(userauthorities[i].getNr())) {
					logger.info("--- access granted through authority " + userauthorities[i].getNr());
					authorized = true;
				}
			}

		if (authorized)
			if (dataarray != null)
				for (int i = 0; i < dataarray.length; i++)
					dataarray[i].setUnfrozen();
	}

}
