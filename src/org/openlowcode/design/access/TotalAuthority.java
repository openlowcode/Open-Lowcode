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
 * This class allows the creation of an authority for the whole module (not
 * linked to a specific domain). It can be used in the design script of a
 * module.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TotalAuthority
		extends
		Authority {

	/**
	 * Creates a total authority for the module (not linked to a domain)
	 * 
	 * @param name  unique id of the authority
	 * @param label name of the authority in plain nice short text
	 * @param scope up to 5 lines explanation of what the authority is doing
	 * 
	 */
	public TotalAuthority(String name, String label, String scope) {
		super(name, label, scope);
	}

}
