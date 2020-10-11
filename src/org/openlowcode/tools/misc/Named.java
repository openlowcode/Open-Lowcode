/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

/**
 * A named object is an object that can be identified, in the relevant
 * namespace, by a unique String
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public abstract class Named implements NamedInterface {
	private String name;

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @param name a name, that will be cleaned to become the unique identifier of
	 *             the object
	 */
	public Named(String name) {
		this.name = cleanName(name);
	}

	/**
	 * changes the name of the object. Note that changing the name after the object
	 * has been inserted in a NamedList
	 * 
	 * @param name
	 */
	public void changeName(String name) {
		this.name = cleanName(name);
	}

	/**
	 * This method aims at cleaning a String used for name identification. In
	 * current inplementation, it will keep only characters, numbers and underscore
	 * 
	 * @param name the input string
	 * @return
	 */
	public static String cleanName(String name) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char currentchar = name.charAt(i);
			if (Character.isLetterOrDigit(currentchar))
				result.append(currentchar);
			if (currentchar == '_')
				result.append(currentchar);
		}
		return result.toString().toUpperCase();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		Named namedobject = (Named) obj;
		return (namedobject.name.equals(this.name));
	}

	@Override
	public int hashCode() {
		return (this.getClass().getName()+"-"+this.name).hashCode();
	}

}
