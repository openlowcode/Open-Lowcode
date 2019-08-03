/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

/**
 * An exception signaling there has been an error processing the
 * request or building a message from the remote party
 * @author Open Lowcode SAS
 *
 */
public class OLcRemoteException extends Exception {
	


	private static final long serialVersionUID = 1L;
	private int remoteerrorcode;
	private String message;
	/**
	 * Creates a remote exception
	 * @param remoteerrorcode 0 if error code not used
	 * @param message excception message
	 */
	public OLcRemoteException(int remoteerrorcode, String message) {
		this.message = message;
		this.remoteerrorcode = remoteerrorcode;
	}
	/**
	 * @return the error code (0 if not used)
	 */
	public int getRemoteErrorCode() {
		return this.remoteerrorcode;
	}
	@Override
	public String getMessage() {
	
		return ""+(this.remoteerrorcode!=0?this.remoteerrorcode+"-":"")+this.message;
	}
}
