/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.trigger;

import org.openlowcode.design.data.DataObjectDefinition;

/**
 * A trigger condition to launch some logic based on an event on a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class TriggerLaunchCondition {
	private DataObjectDefinition parent;

	/**
	 * creates a trigger launch condition for the parent data object
	 * 
	 * @param parent data object the trigger is on
	 */
	public TriggerLaunchCondition(DataObjectDefinition parent) {
		this.parent = parent;
	}

	/**
	 * import statements when this trigger launch condition is used. This is used in
	 * code generation
	 * 
	 * @return all the import statements
	 */
	public abstract String[] generateImportConditions();

	/**
	 * generates the declaration of this trigger launch condition
	 * 
	 * @return declaration (in java)
	 */
	public abstract String generateDeclaration();

	/**
	 * @return the data object this trigger launch condition is working on
	 */
	public DataObjectDefinition getParent() {
		return parent;
	}

}
