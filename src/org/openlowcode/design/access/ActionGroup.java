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

import org.openlowcode.design.action.ActionDefinition;


/**
 * An Action group can be a single action or defined action groups. You may want
 * to consult the {@link org.openlowcode.design.data.DataObjectDefinition}
 * object predefined action groups
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface ActionGroup {

	/**
	 * @return the list of actions in this group
	 */
	public ActionDefinition[] getActionsInGroup();

	/**
	 * @return a unique name for the object (object action group) or module (custom
	 *         action) The name should start by letter, and be composed only of
	 *         letter and underscores. The name may be written in all caps or all
	 *         lower cases, so should be unique in all upper case also.
	 */
	public String getName();
}
