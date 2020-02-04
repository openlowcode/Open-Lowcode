package org.openlowcode.server.security;

/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LinkobjectInterface;

import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.JoinQueryCondition;

import org.openlowcode.server.data.storage.QueryConditionNever;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A personal security mannager uses a link from the specific object to an
 * application user, and gives him a personal privilege.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> object being granted access rights
 * @param <F> links to an app user to grant a personal access
 */
public class ActionObjectPersonalSecurityManager<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, Appuser>>
		extends ActionObjectSecurityManager<E> {

	private static Logger logger = Logger.getLogger(ActionObjectPersonalSecurityManager.class.getName());

	private DataObjectDefinition<F> securitylinkdefinition;

	private Function<DataObjectId<Appuser>, F[]> getterfromappuserid;
	private BiFunction<DataObjectId<Appuser>, DataObjectId<E>, F[]> getterfromappuseridandobject;
	private StoredFieldSchema<DataObjectId<E>> maintableid;
	private StoredFieldSchema<DataObjectId<E>> securitylinkleftid;
	private StoredFieldSchema<DataObjectId<Appuser>> securitylinkrightid;
	private BiFunction<SActionData, SecurityBuffer, E[]> inputactiondataextractor;

	/**
	 * Creates a personal security manager
	 * 
	 * @param securitylinkdefinition       definition of the security link object
	 * @param getterfromappuserid          gets the list of security links from the
	 *                                     provided user
	 * @param getterfromappuseridandobject gets the list of security links for the
	 *                                     provided user and object
	 * @param inputactiondataextractor     the extractor to get the input objbects
	 *                                     from the action
	 * @param maintableid                  stored field of the main table id (used
	 *                                     for queries)
	 * @param securitylinkleftid           stored field of the link left id (used
	 *                                     for queries)
	 * @param securitylinkrightid          stored field of the link right id (used
	 *                                     for queries)
	 */
	public ActionObjectPersonalSecurityManager(DataObjectDefinition<F> securitylinkdefinition,
			Function<DataObjectId<Appuser>, F[]> getterfromappuserid,
			BiFunction<DataObjectId<Appuser>, DataObjectId<E>, F[]> getterfromappuseridandobject,
			BiFunction<SActionData, SecurityBuffer, E[]> inputactiondataextractor,
			StoredFieldSchema<DataObjectId<E>> maintableid, StoredFieldSchema<DataObjectId<E>> securitylinkleftid,
			StoredFieldSchema<DataObjectId<Appuser>> securitylinkrightid) {
		this.securitylinkdefinition = securitylinkdefinition;
		this.getterfromappuserid = getterfromappuserid;
		this.getterfromappuseridandobject = getterfromappuseridandobject;
		this.inputactiondataextractor = inputactiondataextractor;
		this.securitylinkleftid = securitylinkleftid;
		this.securitylinkrightid = securitylinkrightid;
		this.maintableid = maintableid;
	}

	@Override
	public boolean isObjectAuthorized(E object) {
		return this.isAuthorizedForCurrentUser(null, object);
	}

	@Override
	public E[] getInputObject(SActionData input, SecurityBuffer buffer) {

		if (inputactiondataextractor != null)
			return inputactiondataextractor.apply(input, buffer);
		return null;

	}

	@Override
	public boolean isAuthorizedForCurrentUser(String context, E object) {

		DataObjectId<Appuser> currentuserid = OLcServer.getServer().getCurrentUserId();
		F[] authorizations = this.getterfromappuseridandobject.apply(currentuserid, object.getId());
		if (authorizations != null)
			if (authorizations.length > 0)
				return true;
		return false;

	}

	@Override
	public boolean isMaybeAuthorized() {

		DataObjectId<Appuser> currentuserid = OLcServer.getServer().getCurrentUserId();
		F[] authorizations = getterfromappuserid.apply(currentuserid);
		if (authorizations != null)
			if (authorizations.length > 0)
				return true;
		return false;

	}

	@Override
	public boolean queryObjectData() {
		if (inputactiondataextractor == null)
			return false;
		return true;
	}

	@Override
	public boolean filterObjectData() {
		if (inputactiondataextractor != null)
			return false;
		return true;

	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer) {
		DataObjectId<Appuser> currentuserid = OLcServer.getServer().getCurrentUserId();
		F[] authorizations = getterfromappuserid.apply(currentuserid);
		HashMap<String, F> linksperobjectid = new HashMap<String, F>();
		if (authorizations != null)
			for (int i = 0; i < authorizations.length; i++)
				linksperobjectid.put(authorizations[i].getLfid().getId(), authorizations[i]);
		for (int h = 0; h < dataarray.length; h++) {
			try {
				@SuppressWarnings("unchecked")
				E object = (E) dataarray[h];
				if (linksperobjectid.get(object.getId().getId()) != null) {
					object.setUnfrozen();
				} else {
					object.setFrozen();
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
	public Function<TableAlias, QueryFilter> getOutputFilterCondition() {
		return new Function<TableAlias, QueryFilter>() {

			@Override
			public QueryFilter apply(TableAlias t) {
				TableAlias personallink = new TableAlias(securitylinkdefinition.getTableschema(),
						t.getName() + "_" + securitylinkdefinition.getName());
				JoinQueryCondition<DataObjectId<E>> joincondition = new JoinQueryCondition<DataObjectId<E>>(t, maintableid, personallink,
						securitylinkleftid, new QueryOperatorEqual<DataObjectId<E>>());
				try {
					DataObjectId<Appuser> currentuserid = OLcServer.getServer().getCurrentUserId();
					@SuppressWarnings({ "rawtypes", "unchecked" })
					SimpleQueryCondition<?> filterlinkbyuser = new SimpleQueryCondition(personallink,
							securitylinkrightid, new QueryOperatorEqual(), currentuserid.getId());
					return QueryFilter.get(new AndQueryCondition(joincondition, filterlinkbyuser), personallink);
				} catch (RuntimeException e) {
					logger.severe("Exception while generating Filter Condition " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.severe("    - " + e.getStackTrace()[i]);
					return QueryFilter.get(new QueryConditionNever());
				}
			}

		};

	}

}
