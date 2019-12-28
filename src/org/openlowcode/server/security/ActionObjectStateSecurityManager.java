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

import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;

import org.openlowcode.server.data.properties.LifecycleInterface;
import org.openlowcode.server.data.storage.OrQueryCondition;

import org.openlowcode.server.data.storage.QueryConditionNever;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A security manager granting privileges on objects based on state
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the security manager applies to
 */
public abstract class ActionObjectStateSecurityManager<E extends DataObject<E> & LifecycleInterface<E, ?>>
		extends ActionObjectSecurityManager<E> {

	private static final Logger logger = Logger.getLogger(ActionObjectStateSecurityManager.class.getName());

	@Override
	public String toString() {
		String returnstring = "GalliumActionObjectStateSecurityManager:" + authority + "/";
		for (int i = 0; i < states.length; i++)
			returnstring += states[i];
		return returnstring;
	}

	public abstract E[] getInputObject(SActionData input, SecurityBuffer buffer);

	@Override
	public boolean isObjectAuthorized(E object) {
		boolean valid = false;
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
		if (userauthorities == null)
			return false;
		if (object == null)
			throw new RuntimeException(
					"Trying to use SecurityManager but object is not initialized, security manager" + this);
		for (int i = 0; i < userauthorities.length; i++) {
			String thisauthority = userauthorities[i].getNr();
			if (thisauthority.equals(authority)) {
				String objectstate = object.getState();
				for (int j = 0; j < states.length; j++) {
					logger.info("  --- comparing " + objectstate + "-" + states[j]);
					if (states[j].equals(objectstate))
						valid = true;
				}
			}

		}
		return valid;
	}

	@Override
	public boolean isMaybeAuthorized() {
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
		for (int i = 0; i < userauthorities.length; i++) {
			String thisauthority = userauthorities[i].getNr();
			if (thisauthority.equals(authority))
				return true;
		}
		return false;
	}

	private String authority;
	private String[] states;

	public ActionObjectStateSecurityManager(String authority, String[] states) {
		super();
		this.authority = authority;
		this.states = states;

	}

	@Override
	public Function<TableAlias, QueryFilter> getOutputFilterCondition() {
		logger.fine(" ----****---- Temporary debug - requested output filter condition for action object state");
		Function<TableAlias, QueryFilter> returnfunction = new Function<TableAlias, QueryFilter>() {

			@Override
			public QueryFilter apply(TableAlias alias) {

				boolean securitymanagerappliestouser = false;
				Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
				for (int i = 0; i < userauthorities.length; i++) {
					String thisauthority = userauthorities[i].getNr();
					if (thisauthority.equals(authority)) {
						securitymanagerappliestouser = true;
					}
				}
				if (securitymanagerappliestouser) {
					StoredFieldSchema<String> state = new StringStoredField("STATE", null, 64);

					if (states == null)
						return QueryFilter.get(new QueryConditionNever());
					if (states.length == 0)
						return QueryFilter.get(new QueryConditionNever());
					if (states.length == 1)
						return QueryFilter.get(new SimpleQueryCondition<String>(alias, state,
								new QueryOperatorEqual<String>(), states[0]));
					if (states.length > 1) {
						OrQueryCondition oroperator = new OrQueryCondition();
						for (int i = 0; i < states.length; i++)
							oroperator.addCondition(new SimpleQueryCondition<String>(alias, state,
									new QueryOperatorEqual<String>(), states[i]));
						return QueryFilter.get(oroperator);
					}
					return null;
				} else {
					return null;
				}

			}
		};
		return returnfunction;

	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer) {
		for (int h = 0; h < dataarray.length; h++) {
			try {
				@SuppressWarnings("unchecked")
				E object = (E) dataarray[h];

				Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
				if (userauthorities != null)
					for (int i = 0; i < userauthorities.length; i++) {
						String thisauthority = userauthorities[i].getNr();
						if (thisauthority.equals(authority)) {
							String objectstate = object.getState();
							for (int j = 0; j < states.length; j++) {
								logger.info("  --- comparing " + objectstate + "-" + states[j]);
								if (states[j].equals(objectstate))
									object.setUnfrozen();
							}
						}
					}
			} catch (ClassCastException e) {
				logger.warning("Exception in security manager " + e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++) {
					StackTraceElement stacktrace = e.getStackTrace()[i];
					logger.warning("   + " + stacktrace.toString());
				}
			}

		}

	}

	@Override
	public boolean isAuthorizedForCurrentUser(String context, E object) {
		return this.isObjectAuthorized(object);
	}

}
