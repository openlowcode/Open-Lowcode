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

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * The action security manager gives authorization to an action to be performed
 * or not,depending on the action type, the user, and the attributes (typically
 * the location of an object)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
public abstract class ActionSecurityManager {
	/**
	 * this method tells if the security manager has to query an object (typically
	 * to get the object localization and state). If so, the method business logic
	 * will get as attribute the object instead of the object id, to prevent double
	 * query and improve performance on database
	 * 
	 * @return true if the security manager needs to query object data
	 */
	public abstract boolean queryObjectData();

	/**
	 * this method tells if the security manager has to filter the output of the
	 * action Note: both queryObjectData and filterObjectData cannot be true at the
	 * same time
	 * 
	 * @return true if the security manager filters object data
	 */
	public abstract boolean filterObjectData();

	/**
	 * this method will look at an array of objects, and unfreeze all authorized
	 * objects. Note: for the method to work, the objects have to be frozen first
	 * 
	 * @param dataarray the array of objects;
	 */
	public abstract void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer);

	/**
	 * @return true if the user is authorized for the action, false if the user is
	 *         not authorized
	 */
	public abstract boolean isAuthorizedForCurrentUser(String context, SActionData input, SecurityBuffer buffer);

	/**
	 * @return null if unconditional access, or the function else
	 */
	public abstract Function<TableAlias, QueryFilter> getOutputFilterCondition();

	/**
	 * @param list an array of strings
	 * @return a single string for logging purposes
	 */
	public static String buildListSummary(String[] list) {
		if (list == null)
			return null;
		StringBuffer arraycontent = new StringBuffer("[");
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				arraycontent.append(',');
			arraycontent.append(list[i]);
		}
		arraycontent.append("]");
		return arraycontent.toString();
	}

	/**
	 * @param list a list of string
	 * @return one string summarizing all the strings
	 */
	public static String buildListSummary(ArrayList<String> list) {
		if (list == null)
			return null;
		StringBuffer arraycontent = new StringBuffer("[");
		for (int i = 0; i < list.size(); i++) {
			if (i > 0)
				arraycontent.append(',');
			arraycontent.append(list.get(i));
		}
		arraycontent.append("]");
		return arraycontent.toString();
	}

	/**
	 * @param list a list of authorities
	 * @return a string summarizing the number of authorities
	 */
	public static String buildListSummary(Authority[] list) {
		if (list == null)
			return null;
		StringBuffer arraycontent = new StringBuffer("[");
		for (int i = 0; i < list.length; i++) {
			if (i > 0)
				arraycontent.append(',');
			arraycontent.append(list[i].getNr());
		}
		arraycontent.append("]");
		return arraycontent.toString();
	}
}
