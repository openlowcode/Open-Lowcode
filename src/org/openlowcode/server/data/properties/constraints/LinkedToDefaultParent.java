/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import org.openlowcode.server.data.DataObject;

/**
 * An option to the linked to parent property to specify a default parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> definition of the child object
 * @param <F> definition of the parent object
 */
public abstract class LinkedToDefaultParent<E extends DataObject<E>, F extends DataObject<F>> {
	private String number;
	private boolean insertifnotexists;
	private F defaultparentstored;

	/**
	 * @return gets the default parent
	 */
	public F getDefaultparentstored() {
		return defaultparentstored;
	}

	/**
	 * sets the default parent
	 * 
	 * @param defaultparentstored default parent
	 */
	public void setDefaultparentstored(F defaultparentstored) {
		this.defaultparentstored = defaultparentstored;
	}

	/**
	 * creates a linked to default parent
	 * 
	 * @param number            number of the default parent
	 * @param insertifnotexists sets to true to create the default parent if it does
	 *                          not exists yet
	 */
	public LinkedToDefaultParent(String number, boolean insertifnotexists) {
		this.number = number;
		this.insertifnotexists = insertifnotexists;
	}

	/**
	 * performs processing before insertion
	 * 
	 * @param object
	 */
	public abstract void processBeforeInsert(E object);

	/**
	 * the object number
	 * 
	 * @return gets the object number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * true if program will insert default parent if it does not exist
	 * 
	 * @return true if the parent does not exist
	 */
	public boolean isInsertifnotexists() {
		return insertifnotexists;
	}

}
