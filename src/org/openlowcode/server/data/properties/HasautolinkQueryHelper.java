/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * the query helper of the has autolink property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class HasautolinkQueryHelper {
	private static HashMap<String, HasautolinkQueryHelper> helperlist = new HashMap<String, HasautolinkQueryHelper>();
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(HasautolinkQueryHelper.class.getCanonicalName());
	@SuppressWarnings("unused")
	private String name;

	/**
	 * gets an has autolink query helper for the given link name
	 * 
	 * @param name
	 * @return
	 */
	public static HasautolinkQueryHelper get(String name) {
		// TODO check if threadsafe
		HasautolinkQueryHelper answer = helperlist.get(name);
		if (answer != null)
			return answer;
		answer = new HasautolinkQueryHelper(name);
		helperlist.put(name, answer);
		return answer;
	}

	/**
	 * creates a query helper for autolink property for the given name
	 * 
	 * @param name name of the auto-link
	 */
	private HasautolinkQueryHelper(String name) {
		this.name = name;
	}

	/**
	 * get autolink and children of the given origin object id (origin object is the
	 * left object for the link)
	 * 
	 * <br>
	 * Not yet implemented yet
	 * 
	 * @param originobjectid origin object id
	 * @param additionalcondition additional condition
	 * @param objectdefinition definition of the object being link
	 * @param autolinkdefinition definition of the auto-link object
	 * @param hasautolinkdefinition definition of the has autolink property
	 * @return the links and right object
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & AutolinkobjectInterface<F, E>> TwoDataObjects<F, E>[] getautolinksandchildren(
			DataObjectId<E> originobjectid, QueryFilter additionalcondition, DataObjectDefinition<E> objectdefinition,
			DataObjectDefinition<F> autolinkdefinition, HasautolinkDefinition<E, F> hasautolinkdefinition) {
		throw new RuntimeException("Not Yet Implemented");
	}

}