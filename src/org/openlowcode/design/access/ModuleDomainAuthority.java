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
 * A module domain authority is an authority given for a domain of the module
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ModuleDomainAuthority
		extends
		Authority {

	/**
	 * creates the module domain authority
	 * 
	 * @param name  unique id of the authority
	 * @param label name of the authority in plain nice short text
	 * @param scope up to 5 lines explanation of what the authority is doing
	 */
	public ModuleDomainAuthority(String name, String label, String scope) {
		super(name, label, scope);

		if (name.length() > 29)
			throw new RuntimeException("Name of domain module should be less than 29 characters");

	}

}
