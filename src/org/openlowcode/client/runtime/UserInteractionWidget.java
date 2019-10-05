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

/**
 * Widgets of the client that need to be disabled during a connection
 * should implement this interface.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public interface UserInteractionWidget {
	/**
	 * Called before a request to server
	 */
	public void disableDuringServerRequest();
	
	/**
	 * Called after server response has
	 * been processed
	 */
	public void enableAfterServerResponse();
}
