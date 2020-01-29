/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.module.roles;

import java.util.ArrayList;

import org.openlowcode.design.access.ModuleDomainAuthority;
import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * This class allows the creation of a group for the module. It can be used in
 * the design script of a module.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Group
		extends
		Named {
	private boolean moduleadminismember;
	private ArrayList<User> members;
	private NamedList<TotalAuthority> authorities;
	private NamedList<ModuleDomainAuthority> domainauthorities;
	private String description;

	/**
	 * @return true if the module admin is a module of the group
	 */
	public boolean isModuleadminismember() {
		return moduleadminismember;
	}

	/**
	 * @return a plain language description of the group
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * creates a group with the given name and description
	 * 
	 * @param name        a unique name of the group, should be a valid java field
	 *                    name
	 * @param description description of the group in plain language
	 */
	public Group(String name, String description) {
		super(name);
		authorities = new NamedList<TotalAuthority>();
		domainauthorities = new NamedList<ModuleDomainAuthority>();
		this.members = new ArrayList<User>();
		this.moduleadminismember = false;
		this.description = description;
	}

	/**
	 * creates a group with the given name (no description)
	 * 
	 * @param name a unique name of the group, should be a valid java field name
	 */
	public Group(String name) {
		this(name, name);
	}

	/**
	 * adds the module administrator as a member of the group
	 */
	public void setModuleAdminAsMember() {
		this.moduleadminismember = true;
	}

	/**
	 * creates a group, and assigns it to the total authority
	 * 
	 * @param name      a unique name of the group, should be a valid java field
	 *                  name
	 * @param authority authority that should be granted to the group
	 */
	public Group(String name, TotalAuthority authority) {
		this(name);
		authorities.add(authority);

	}

	/**
	 * adds the following total authority to the group
	 * 
	 * @param authority authority to be granted to the group
	 */
	public void setGroupInAuthority(TotalAuthority authority) {
		this.authorities.add(authority);
	}

	/**
	 * adds a domain authority to the group. This means the group will have the
	 * authority to all the domains of the module
	 * 
	 * @param domainauthority the domain authority to add to the group
	 */
	public void setGroupInDomainAuthority(ModuleDomainAuthority domainauthority) {
		this.domainauthorities.add(domainauthority);
	}

	/**
	 * @return the number of domain authorities granted for this group
	 */
	public int getDomainAuthoritiesIndex() {
		return this.domainauthorities.getSize();
	}

	/**
	 * gets the domain authority at the given index
	 * 
	 * @param index index of the authority between 0 (included) and
	 *              getDomainAuthoritiesIndex (excluded)
	 * @return the module domain authority at the given index
	 */
	public ModuleDomainAuthority getDomainAuthorityAt(int index) {
		return this.domainauthorities.get(index);
	}

	/**
	 * @return the number of total authorities granted to this group
	 */
	public int getAuthoritiesindex() {
		return authorities.getSize();
	}

	/**
	 * returns an authority assigned to this group
	 * 
	 * @param index a number between 0 (included) and getAuthoritiesindex (excluded)
	 * @return the total authority at the given index
	 */
	public TotalAuthority getAuthority(int index) {
		return authorities.get(index);
	}

	/**
	 * gets the number of users assigned to this group
	 * 
	 * @return the number of users
	 */
	public int getMemberindex() {
		return this.members.size();
	}

	/**
	 * gets the user member at the given index
	 * 
	 * @param index a number between 0 (included) and getMemberindex (excluded)
	 * @return the user at the given index
	 */
	public User getMember(int index) {
		return this.members.get(index);
	}
}
