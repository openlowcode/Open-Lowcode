/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.access;

/**
 * A module domain is defined for the an application module. Its name has to be
 * unique for the domain
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ModuleDomain
		extends
		Domain {

	private String originalname;

	/**
	 * the name as entered. It is not cleaned (basically spaces are suppressed)
	 * 
	 * @return the original name
	 */
	public String getOriginalName() {
		return this.originalname;
	}

	/**
	 * Create a module domain with the given name
	 * 
	 * @param name a 29 characters unique identifier. It should start by a letter
	 */
	public ModuleDomain(String name) {
		super(name);
		this.originalname = name;
		if (name.length() > 29)
			throw new RuntimeException("Name of domain module should be less than 29 characters");
		if (!(Character.isLetter(name.charAt(0))))
			throw new RuntimeException("Name of module domain should start by a real letter");

	}

}
