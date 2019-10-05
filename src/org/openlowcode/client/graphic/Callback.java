/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic;

/**
 * Callback is a mechanism to have the same action for different objects, with a
 * callback on the precise widget pressed. The most common usage is for the
 * ObjectBand callback where a list of objects are shown behind one another, and
 * one button will appear per object for an action assigned to the object band.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public interface Callback {
	public void callback();
}
