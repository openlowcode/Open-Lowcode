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

/**
 * This class allows to declare a domain. By declaring several domains in the
 * application, several groups can be assigned to roles for objects of the
 * domain. This allows to have the same application used by different groups,
 * with segregation of data so that people only see data for the application for
 * their domain.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Domain
		extends
		Named {

	/**
	 * create the domain with the given name
	 * 
	 * @param name name of the domain (should be unique for the module)
	 */
	public Domain(String name) {
		super(name);
	}

}
