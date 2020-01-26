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

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A module domain group is a family of groups that are created on each domain
 * of the module. Module domain groups can be assigned to module domain
 * authorities to be given privileges linked to domains
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ModuleDomainGroup
		extends
		Named {
	private String description;
	private NamedList<ModuleDomainAuthority> parentauthorities;
	private NamedList<TotalAuthority> totalauthorities;

	/**
	 * create the given module domain group assigned to the module domain authority
	 * 
	 * @param name            name of the module domain group
	 * @param parentauthority authority the group is assigned to
	 */
	public ModuleDomainGroup(String name, ModuleDomainAuthority parentauthority) {
		this(name, name, parentauthority);
	}

	/**
	 * create the given module domain group assigned to the module domain authority
	 * with the description
	 * 
	 * @param name            name of the module domain group
	 * @param description     plain language description of the group
	 * @param parentauthority authority the group is assigned to
	 */
	public ModuleDomainGroup(String name, String description, ModuleDomainAuthority parentauthority) {
		super(name);

		this.parentauthorities = new NamedList<ModuleDomainAuthority>();
		this.totalauthorities = new NamedList<TotalAuthority>();
		this.parentauthorities.add(parentauthority);
		if (name.length() > 29)
			throw new RuntimeException("Name of  module domain group should be less than 29 characters for ");
		if (description.length() > 800)
			throw new RuntimeException("Description of  module domain groupshould be less than 800 characters");
		if (!(Character.isLetter(name.charAt(0))))
			throw new RuntimeException("Name of module domain should start by a real letter");
		this.description = description;
	}

	/**
	 * @return get the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * adds a another authority to the domain group
	 * 
	 * @param otherauthority authority to be added
	 */
	public void addModuleDomainAuthority(ModuleDomainAuthority otherauthority) {
		this.parentauthorities.add(otherauthority);
	}

	/**
	 * get the number of authorities for this domain group
	 * 
	 * @return the number of authorities for this domain group
	 */
	public int getAuthoritySize() {
		return parentauthorities.getSize();
	}

	/**
	 * get the domain authority at the given index
	 * 
	 * @param index a number between 0 (included) and getAuthoritySize() (excuded)
	 * @return the domain authority at the given index
	 */
	public ModuleDomainAuthority getModuleDomainAuthority(int index) {
		return parentauthorities.get(index);
	}

	/**
	 * adds a total authority (an authority valid for all domains)
	 * 
	 * @param totalauthority total authority
	 */
	public void addTotalAuthority(TotalAuthority totalauthority) {
		totalauthorities.add(totalauthority);
	}

	/**
	 * get the number of total authorities assigned to this domain group
	 * 
	 * @return the number of total authorities
	 */
	public int getTotalAuthoritySize() {
		return totalauthorities.getSize();
	}

	/**
	 * get the authority at the given index
	 * 
	 * @param index a number between 0 (included) and getTotalAuthoritySize
	 *              (excluded)
	 * @return the total authority at the given index
	 */
	public TotalAuthority getTotalAuthority(int index) {
		return totalauthorities.get(index);
	}

}
