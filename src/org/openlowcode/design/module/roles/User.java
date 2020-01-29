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

/**
 * This class allows the creation of a user for a module. This is used by
 * default to create the administrative user of the module, but other users can
 * be created in the module code.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class User {
	private String password;
	private String email;
	private String name;
	private String firstname;
	private String lastname;

	/**
	 * @return first name of the user
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @return last name of the user
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * Creates a new user with a local password
	 * 
	 * @param name      unique name of the user (may be its company id)
	 * @param password  default password (best practice is to change it after
	 *                  application created)
	 * @param email     e-mail
	 * @param firstname first name
	 * @param lastname  last name
	 */
	public User(String name, String password, String email, String firstname, String lastname) {
		this.name = name;
		this.password = password;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
	}

	/**
	 * @return the unique name (typically the company id)
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return default password (best practice is to change it after application
	 *         created)
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the e-mail e-mail of the user
	 */
	public String getEmail() {
		return email;
	}

	private static User adminuser = new User("admin", "notapplicable", "notapplicable", "not applicable",
			"not applicable");

	/**
	 * @return a user representing the overall admin of the server
	 */
	public static User getAdminUser() {
		return adminuser;
	}
}
