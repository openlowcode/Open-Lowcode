/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.design.utility;

import org.openlowcode.tools.misc.Named;

/**
 * This class allows to define a system attribute for the module. Name of the
 * object should be unique for the module
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SystemAttributeInit
		extends
		Named {

	private String comment;
	private String defaultvalue;

	/**
	 * Creates a system attribute to initiate at module creation
	 * 
	 * @param name         the final name of the system attribute will be the name
	 *                     entered here, with a prefix added with the module code
	 *                     and a '.'
	 * @param defaultvalue value at initialisation
	 * @param comment      explanation on the system attribute. It should include
	 *                     the list of potential values
	 * 
	 */

	public SystemAttributeInit(String name, String defaultvalue, String comment) {
		super(name);
		this.comment = comment;
		this.defaultvalue = defaultvalue;

	}

	/**
	 * @return get the comment of the system attribute init
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return get the default value of the system attribute
	 */
	public String getDefaultvalue() {
		return defaultvalue;
	}

}
