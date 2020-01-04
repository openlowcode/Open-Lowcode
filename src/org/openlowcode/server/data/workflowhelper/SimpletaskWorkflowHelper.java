package org.openlowcode.server.data.workflowhelper;

/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/


import org.openlowcode.server.data.DataObject;

/**
 * A helper for objects that have simple task workflow properties. It includes
 * all the specifics for the workflow for this specific data object type
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public abstract class SimpletaskWorkflowHelper<E extends DataObject<E>> {

	public static final String EMAIL_NONE = null;
	public static final String EMAIL_NOW = "NOW";
	public static final String EMAIL_DELAY15MIN = "D15M";
	public static final String EMAIL_DELAY2H = "D2H";
	public static final String EMAIL_DAILY = "DAY";
	public static final String EMAIL_WEEKLY = "WEEKLY";
	private String emailtype;

	/**
	 * e-mail type specifies at which frequencies notifications are sent. If a delay
	 * is set, the program will group all notifications received in the given delay
	 * and sends a single consolidated name
	 * 
	 * @return the e-mail notification type in one of the statically defined strings
	 *         of this class
	 */
	public String getEmailtype() {
		return this.emailtype;
	}

	/**
	 * provides the relevant authority mapper to send tasks to for the given object
	 * 
	 * @return the authority mapper
	 */
	public abstract ObjectToAuthorityMapper<E> getSingleAuthorityMapper();

	/**
	 * provides the task message
	 * 
	 * @return the task message
	 */
	public abstract String getTaskMessage();

	/**
	 * provides the default delay (in calendar days) given for the tasks
	 * 
	 * @return the default delay (in days)
	 */
	public abstract int getDefaultDelay();

	/**
	 * creates a simple task workflow helper with the choosen e-mail sending delay
	 * 
	 * @param emailtype a valid static string declared in this class, or null if no
	 *                  delay
	 */
	public SimpletaskWorkflowHelper(String emailtype) {

		boolean emailtypevalue = false;
		if (emailtype == null)
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_NOW))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DELAY15MIN))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DELAY2H))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_DAILY))
			emailtypevalue = true;
		if (emailtype.equals(EMAIL_WEEKLY))
			emailtypevalue = true;

		if (!emailtypevalue)
			throw new RuntimeException(
					"email type is not valid, please refer to the class static string, given type =  '" + emailtype
							+ "'");

		this.emailtype = emailtype;
	}
}
