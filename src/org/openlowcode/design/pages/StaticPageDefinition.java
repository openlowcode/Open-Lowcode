/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.pages;

/**
 * A static page has no input attribute. It can be navigated to from any point
 * in the application
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StaticPageDefinition
		extends
		PageDefinition {

	/**
	 * Creates a static page
	 * 
	 * @param name a unique name inside a module that is a valid java attribute and
	 *             class name
	 */
	public StaticPageDefinition(String name) {
		super(name);

	}

}
