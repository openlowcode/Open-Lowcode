/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.tools.misc.Named;

import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;

/**
 * An helper object to access to a persisted sequence
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Sequence
		extends
		Named {

	/**
	 * creates an helper to access a sequence
	 * 
	 * @param name name of the sequence
	 */
	public Sequence(String name) {
		super(name.toUpperCase());
	}

	/**
	 * gets the next value of the persisted sequence
	 * 
	 * @return the next sequence
	 */
	public int getNextValue() {
		PersistentStorage storage = PersistenceGateway.getStorage();
		int nextvalue = storage.getNextValue(this.getName());
		PersistenceGateway.checkinStorage(storage);
		return nextvalue;
	}

	/**
	 * updates the persistence layer, creating the sequence if required
	 */
	public void updatepersistence() {
		PersistentStorage storage = PersistenceGateway.getStorage();
		boolean issequence = storage.isSequenceExisting(this.getName());
		if (!issequence) {
			storage.createSequence(this.getName(), 1);
		}
		PersistenceGateway.checkinStorage(storage);
	}
}
