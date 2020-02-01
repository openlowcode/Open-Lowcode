/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import org.openlowcode.design.access.ActionGroup;
import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.module.Module;

/**
 * An automatic action marker allows to set specific privileges for a specific
 * automatically generated action
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AutomaticActionMarker
		extends
		org.openlowcode.tools.misc.Named
		implements
		ActionGroup {

	private Module parentmodule;

	/**
	 * creates an automatic action marker
	 * 
	 * @param name         name of the action
	 * @param parentmodule module of the data object the action is part of
	 */
	public AutomaticActionMarker(String name, Module parentmodule) {
		super(name);
		this.parentmodule = parentmodule;
	}

	@Override
	public ActionDefinition[] getActionsInGroup() {
		ActionDefinition action = this.parentmodule.lookupActionDefinition(this.getName());
		if (action == null)
			throw new RuntimeException("AutomaticActionMarker error : action " + this.getName() + " does not exist.");
		return new ActionDefinition[] { action };
	}

}
