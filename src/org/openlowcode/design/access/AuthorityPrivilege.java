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
 * An authority privilege gives rights to users with an authority to perform
 * some actions on a data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class AuthorityPrivilege
		extends
		Privilege {

	private Authority authority;

	/**
	 * create an authority privilege for the given action group and authority
	 * 
	 * @param actiongroup the group of actions the users in the authority will be
	 *                    granted access to
	 * @param authority   authority that will be assigned the privilege
	 */
	public AuthorityPrivilege(ActionGroup actiongroup, Authority authority) {
		super(actiongroup);
		if (actiongroup == null)
			throw new RuntimeException("ActionGroup cannot be null");

		this.authority = authority;
	}

	/**
	 * @return gets the authorit the privilege is assigned to
	 */
	public Authority getAuthority() {
		return this.authority;
	}

	@Override
	public String toString() {
		return "[" + this.getClass().getName() + "-" + this.getActiongroup().toString() + ";"
				+ (authority != null ? authority.getName() : "-") + "]";
	}

}
