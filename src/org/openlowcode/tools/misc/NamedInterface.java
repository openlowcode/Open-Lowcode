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
 * A named object is an object that can be identified, in the relevant namespace, by a
 * unique String
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public interface NamedInterface {
	/**
	 * @return the name, as potentially cleaned by 
	 * the class
	 */
	public String getName();
}
