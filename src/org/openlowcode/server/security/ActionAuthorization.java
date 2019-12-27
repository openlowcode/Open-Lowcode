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

import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * a class summarizing the result of a security check. It stores:
 * <ul>
 * <li>the security status (authorized, potentially authorized, not
 * authorized)</li>
 * <li>a function to generate a filter on queries when required</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ActionAuthorization {
	private int authorization;
	public static final int AUTHORIZED = 2;
	public static final int POTENTIALLY_AUTHORIZED = 1;
	public static final int NOT_AUTHORIZED = 0;
	private Function<TableAlias, QueryFilter> additionalconditiongenerator;

	/**
	 * Creates a new authorized that is either authorized or not authorized.
	 * Potentially authorized is not valid here.
	 * 
	 * @param authorization an integer corresponding to the constants declared in
	 *                      this class
	 */
	public ActionAuthorization(int authorization) {
		boolean valid = false;
		if (authorization == AUTHORIZED)
			valid = true;
		if (authorization == NOT_AUTHORIZED)
			valid = true;
		if (!valid)
			throw new RuntimeException(
					"simple authorization should be either AUTHORIZED or NOT_AUTHORIZED (as defined in the class)");
		this.authorization = authorization;
	}

	/**
	 * Creates an action authorization with value 'POTENTIALLY_AUTHORIZED) and the
	 * function to generate query filters
	 * 
	 * @param additionalconditiongenerator
	 */
	public ActionAuthorization(Function<TableAlias, QueryFilter> additionalconditiongenerator) {
		this.authorization = POTENTIALLY_AUTHORIZED;
		this.additionalconditiongenerator = additionalconditiongenerator;

	}

	/**
	 * gets authorization type
	 * 
	 * @return the authorization type
	 */
	public int getAuthorization() {
		return authorization;
	}

	/**
	 * gets the function to generate query filters
	 * 
	 * @return the function to generate query filters
	 */
	public Function<TableAlias, QueryFilter> getAdditionalconditiongenerator() {
		return additionalconditiongenerator;
	}

}
