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
 * A page add-on is an additional 'frame' around the main page content. By
 * default, Open Lowcode adds a top menu with the main actions of different
 * modules
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class AddonPageDefinition
		extends
		PageDefinition {

	/**
	 * creates an add-on page definition with the given name
	 * 
	 * @param name name, should be unique for the module and a valid java field name
	 */
	public AddonPageDefinition(String name) {
		super(name);
		this.setNoAddOn();
	}

	@Override
	public String getClassAddon() {
		return "Addon";
	}

	@Override
	public String getAttributeMethodAddon() {
		return "Addon";
	}
}
