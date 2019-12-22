/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action;

/**
 * This enumeration allows to specify what kind of security checks should be
 * performed during the object data access method. Generally, security is
 * performed as part of {@link org.openlowcode.server.action.ActionExecution}.
 * However, in some cases it is necessary to perform an additional security
 * check with the object in the datamethod just before the action. The best case
 * is an insert where it may be necessary to perform a check (for example on
 * location) only after all preprocessing of the object has been finished.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public enum SecurityInDataMethod {
	/**
	 * Perform no security check. This is the default
	 */
	NONE,
	/**
	 * Performs security check on the main object (will check once more that the
	 * action is authorized for the object after all pre-processing. If security
	 * check does not work, there will be an exception thrown.
	 */
	FAIL_IF_NOT_AUTHORIZED,
	/**
	 * Performs security check on the main object (will check once more that the
	 * action is authorized for the object after all pre-processing. If security
	 * check does not work, the data access .
	 */
	DO_NOTHING_IF_NOT_AUTHORIZED;
}
