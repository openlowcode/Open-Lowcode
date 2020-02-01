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
 * A trigger launch condition that will trigger on any update of the data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TriggerLaunchConditionUpdate
		extends
		TriggerLaunchCondition {

	/**
	 * create a trigger launch condition that triggers on the update of the data
	 * object
	 * 
	 * @param parent data object the trigger launch condition is on
	 */
	public TriggerLaunchConditionUpdate(DataObjectDefinition parent) {
		super(parent);

	}

	@Override
	public String generateDeclaration() {
		return "new TriggerConditionCreateUpdate()";
	}

	@Override
	public String[] generateImportConditions() {
		return new String[] { "import gallium.server.data.properties.trigger.TriggerConditionCreateUpdate;" };
	}

}
