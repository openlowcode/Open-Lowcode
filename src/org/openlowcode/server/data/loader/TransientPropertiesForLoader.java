/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import java.util.HashMap;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;

/**
 * This object will store transient objects that will live for the time of a
 * loader exercise. THis allows to store data in the column loader algorithms,
 * for example, to process several columns at the same time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TransientPropertiesForLoader<E extends DataObject<E>> {
	private HashMap<String, CustomloaderHelper<E>> transientproperties;

	/**
	 * creates a store of Custom Loader helpers
	 */
	public TransientPropertiesForLoader() {
		transientproperties = new HashMap<String, CustomloaderHelper<E>>();
	}

	/**
	 * adds a custom loader helper
	 * 
	 * @param propertyname             name of the property
	 * @param transientcolumngenerator transient column generator
	 */
	public void addTransientColumnGenerator(String propertyname, CustomloaderHelper<E> transientcolumngenerator) {
		transientproperties.put(propertyname, transientcolumngenerator);
	}

	/**
	 * gets the column generator for the given property name
	 * 
	 * @param propertyname name of the property
	 * @return column generator
	 */
	public CustomloaderHelper<E> getTransientColumnGenerator(String propertyname) {
		return transientproperties.get(propertyname);
	}

}
