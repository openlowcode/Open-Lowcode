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

import java.util.logging.Logger;

import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;

/**
 * a common parent class for all security managers that requires data from the
 * object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> class of the data object
 */
public abstract class ActionObjectSecurityManager<E extends DataObject<E>> extends ActionSecurityManager {
	private static Logger logger = Logger.getLogger(ActionObjectSecurityManager.class.getName());

	/**
	 * function to be implemented by the specific security manager, telling if an
	 * object is valid or not
	 * 
	 * @param object the object
	 * @return true if authorized, false if not
	 */
	public abstract boolean isObjectAuthorized(E object);

	/**
	 * @param input  input action data
	 * @param buffer security buffer (should be used as much as possibl)e
	 * @return the list of objects that have to be tested as input of the action
	 */
	public abstract E[] getInputObject(SActionData input, SecurityBuffer buffer);

	/**
	 * this method allows to check if an object is valid or not depending on the
	 * context
	 * 
	 * @param context any context useful for the specific logic
	 * @param object  the object
	 * @return
	 */
	public abstract boolean isAuthorizedForCurrentUser(String context, E object);

	/**
	 * this method should return true if there is a possibility that the user has
	 * the privilege for some objects. This is typically for queries where users
	 * will be authorized to see some (but not all) of the objects. Then, the query
	 * filter mechanism should be used
	 * 
	 * @return true if function is maybe authorized for the user (should use query
	 *         filter to filter data)
	 */
	public abstract boolean isMaybeAuthorized();

	@Override
	public boolean isAuthorizedForCurrentUser(String context, SActionData input, SecurityBuffer buffer) {

		E[] objects = this.getInputObject(input, buffer);
		// as a rule, if the function is called with no object, returns OK if the
		// function may be OK at some time
		if (objects == null)
			return isMaybeAuthorized();
		if (objects.length == 0)
			return isMaybeAuthorized();
		for (int h = 0; h < objects.length; h++) {
			E object = objects[h];
			logger.info(" --------------------- Start Evaluation " + context + " -------------------- ");
			boolean valid = this.isObjectAuthorized(object);

			if (!valid) {
				logger.info("object KO : " + object.dropToString());
				return false;
			}
		}
		return true;

	}

}
