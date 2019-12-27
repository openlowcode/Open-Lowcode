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
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Domain;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;
import org.openlowcode.server.data.properties.LocatedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A security manager providing grants based on location and state of the boejct
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object
 */
public abstract class ActionObjectStateDomainSecurityManager<E extends DataObject<E> & LocatedInterface<E> & LifecycleInterface<E, ?> & UniqueidentifiedInterface<E>>
		extends ActionObjectSecurityManager<E> {
	private static final Logger logger = Logger.getLogger(ActionObjectStateDomainSecurityManager.class.getName());
	private String authoritysuffix;
	private String[] states;

	/**
	 * @param authoritysuffix domain suffix for the authority (full authority name
	 *                        will include domain and authority)
	 * @param states          authorized states
	 */
	public ActionObjectStateDomainSecurityManager(String authoritysuffix, String[] states) {
		super();
		this.authoritysuffix = authoritysuffix;
		this.states = states;

	}

	@Override
	public abstract E[] getInputObject(SActionData input, SecurityBuffer buffer);

	@Override
	public String toString() {
		String returnstring = "GalliumActionObjectStateDomainSecurityManager:" + authoritysuffix + "/";
		for (int i = 0; i < states.length; i++)
			returnstring += states[i];
		return returnstring;
	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer) {
		for (int h = 0; h < dataarray.length; h++) {
			try {
				@SuppressWarnings("unchecked")
				E object = (E) dataarray[h];
				DataObjectId<Domain> domainid = object.getLocationdomainid();
				Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerId(domainid);
				String authority = domain.getNr() + "_" + authoritysuffix;
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
	public boolean isObjectAuthorized(E object) {
		DataObjectId<Domain> domainid = null;
		if (object == null)
			throw new RuntimeException(
					"Trying to use SecurityManager but object is not initialized, security manager" + this);

		domainid = object.getLocationdomainid();

		Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerId(domainid);
		boolean hasdomain = (domain != null ? true : false);
		boolean hasstate = false;
		if (object.getState() != null)
			if (object.getState().trim().length() > 0)
				hasstate = true;

		boolean valid = false;
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();

		if (userauthorities == null)
			return false;
		for (int i = 0; i < userauthorities.length; i++) {
			String thisauthority = userauthorities[i].getNr();
			boolean authorityvalid = false;
			if (hasdomain)
				if (thisauthority.equals(domain.getNr() + "_" + authoritysuffix))
					authorityvalid = true;
			if (!hasdomain)
				if (thisauthority.endsWith(authoritysuffix))
					authorityvalid = true;

			if (authorityvalid) {
				String objectstate = object.getState();
				if (!hasstate) {
					logger.info(" --- one match OK for " + authoritysuffix + " with state unspecified.");
					valid = true;
				} else {
					for (int j = 0; j < states.length; j++) {
						logger.info("  --- comparing " + objectstate + "-" + states[j]);
						if (states[j].equals(objectstate)) {
							valid = true;
							logger.info(" --- one match OK for " + authoritysuffix);
						}
					}
				}

			}

		}
		return valid;
	}

	public boolean isMaybeAuthorized() {
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
		for (int i = 0; i < userauthorities.length; i++) {
			String thisauthority = userauthorities[i].getNr();
			if (thisauthority.endsWith(authoritysuffix))
				return true;
		}
		return false;
	}

	@Override
	public Function<TableAlias, QueryFilter> getOutputFilterCondition() {
		logger.fine(" ----****---- Temporary debug - requested output filter condition for action object domain");
		Function<TableAlias, QueryFilter> returnfunction = new Function<TableAlias, QueryFilter>() {

			@Override
			public QueryFilter apply(TableAlias alias) {
				try {

					/// ---------------------------------------------------------------------
					/// Domain Query Condition
					/// ---------------------------------------------------------------------

					StoredFieldSchema<String> domainid = new StringStoredField("LOCATIONDOMAINID", null, 64);
					SecurityManager securitymanager = OLcServer.getServer().getSecuritymanager();
					Authority[] authorities = securitymanager.getAuthoritiesForCurrentUser();
					ArrayList<DataObjectId<Domain>> domains = new ArrayList<DataObjectId<Domain>>();
					for (int i = 0; i < authorities.length; i++) {
						Authority thisauthority = authorities[i];
						String authorityname = thisauthority.getNr();
						if (authorityname.endsWith(authoritysuffix))
							if (!authorityname.equals(authoritysuffix)) {
								String domainnumber = authorityname.substring(0,
										authorityname.length() - authoritysuffix.length() - 1);

								Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerNr(domainnumber);
								domains.add(domain.getId());
							}
					}
					QueryCondition domainquerycondition = null;
					if (domains.size() == 1)

						domainquerycondition = new SimpleQueryCondition<String>(alias, domainid,
								new QueryOperatorEqual<String>(), domains.get(0).getId());

					if (domains.size() > 1) {
						OrQueryCondition oroperator = new OrQueryCondition();
						for (int i = 0; i < domains.size(); i++)
							oroperator.addCondition(new SimpleQueryCondition<String>(alias, domainid,
									new QueryOperatorEqual<String>(), domains.get(i).getId()));
						domainquerycondition = oroperator;
					}
					/// ---------------------------------------------------------------------
					/// State Query Condition
					/// ---------------------------------------------------------------------

					boolean securitymanagerappliestouser = false;
					Authority[] userauthorities = OLcServer.getServer().getSecuritymanager()
							.getAuthoritiesForCurrentUser();
					for (int i = 0; i < userauthorities.length; i++) {
						String thisauthority = userauthorities[i].getNr();
						if (thisauthority.endsWith(authoritysuffix)) {
							securitymanagerappliestouser = true;
						}
					}
					QueryCondition statequerycondition = null;
					if (securitymanagerappliestouser) {
						StoredFieldSchema<String> state = new StringStoredField("STATE", null, 64);

						if (states != null)
							if (states.length == 1)
								statequerycondition = new SimpleQueryCondition<String>(alias, state,
										new QueryOperatorEqual<String>(), states[0]);
						if (states != null)
							if (states.length > 1) {
								OrQueryCondition oroperator = new OrQueryCondition();
								for (int i = 0; i < states.length; i++)
									oroperator.addCondition(new SimpleQueryCondition<String>(alias, state,
											new QueryOperatorEqual<String>(), states[i]));
								statequerycondition = oroperator;
							}
					}
					if ((domainquerycondition == null) || (statequerycondition == null))
						return null;
					return QueryFilter.get(new AndQueryCondition(domainquerycondition, statequerycondition));

				} catch (RuntimeException e) {
					logger.info("Exception during filter. This may be a security breach " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.info(" - " + e.getStackTrace()[i].toString());
					return null;
				}

			}
		};
		return returnfunction;
	}

	@Override
	public boolean isAuthorizedForCurrentUser(String context, E object) {
		return this.isObjectAuthorized(object);
	}
}
