/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
 * A trigger launch condition that will trigger before the persistence of data
 * on the database. It is an opportunity to peform checks and complex logic
 * before persistence
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 * @since 1.7
 *
 */
public class TriggerLaunchConditionBeforeUpdate
		extends
		TriggerLaunchCondition {

	/**
	 * create a trigger launch condition that will execute the trigger just before
	 * the update of data in the database
	 * 
	 * @param parent data object the trigger launch condition is on
	 */
	public TriggerLaunchConditionBeforeUpdate(DataObjectDefinition parent) {
		super(parent);

	}

	@Override
	public String generateDeclaration() {
		return "new TriggerConditionBeforeUpdate()";
	}

	@Override
	public String[] generateImportConditions() {
		return new String[] { "import org.openlowcode.server.data.properties.trigger.TriggerConditionBeforeUpdate;" };
	}

}
