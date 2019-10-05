/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import org.openlowcode.client.graphic.CPageNode;

/**
 * An unsaved data warning is raised by a component where update has started. It tells the client
 * There should be a warning displayed when potentially leaving the page.<br>
 * Unsaved data warning is displayed for any action in the client except the action using the object as parameter. 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class UnsavedDataWarning {
	private String message;
	private String continuemessage;
	private String stopmessage;
	private CPageNode originnode;

	
	/**
	 * @param message the message to display in English
	 * @param continuemessage the message for the continue button
	 * @param stopmessage the message for the stop message
	 * @param originnode component for the action expected. If any other component triggers an action, the message will show
	 */
	public UnsavedDataWarning(String message, String continuemessage, String stopmessage, CPageNode originnode ) {
		super();
		this.message = message;
		this.continuemessage = continuemessage;
		this.stopmessage = stopmessage;
		this.originnode = originnode;

	}

	/**
	 * @return main message, the question you ask to users
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the label of the continue button
	 */
	public String getContinuemessage() {
		return continuemessage;
	}

	/**
	 * @return the label of the stop message
	 */
	public String getStopmessage() {
		return stopmessage;
	}

	/**
	 * @return the node having sent the warning
	 */
	public CPageNode getOriginode() {
		return originnode;
	}

	@Override
	public String toString() {
		return " -- "+originnode.getSignificantpath()+"/"+originnode.getClass();
	}

	
	
	
}
