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

import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * An authority is given to user groups to provide them all the privileges
 * assigned to this authority
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Authority
		extends
		Named {

	private String label;
	private String scope;
	private Module module;

	/**
	 * @return get the label for the authority (plain language explanation in the
	 *         default language)
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return get the scope, the multi-line explanation on what the authority is
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Create the authority
	 * 
	 * @param name  unique id of the authority
	 * @param label name of the authority in plain nice short text
	 * @param scope up to 5 lines explanation of what the authority is doing
	 */
	public Authority(String number, String label, String scope) {
		super(number);
		this.label = label;
		this.scope = scope;
		if (!(Character.isLetter(number.charAt(0))))
			throw new RuntimeException("Name of module domain should start by a real letter");

	}

	/**
	 * @param module set the parent module
	 */
	public void setParentModule(Module module) {
		this.module = module;
	}

	/**
	 * @return get the parent module
	 */
	public Module getModule() {
		return this.module;
	}
}
