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
 * A trigger launch condition launching specific logic (trigger) when the object
 * is deleted
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TriggerLaunchConditionDelete
		extends
		TriggerLaunchCondition {
	/**
	 * creates a trigger launch condition on delete
	 * 
	 * @param parent parent data object
	 */
	public TriggerLaunchConditionDelete(DataObjectDefinition parent) {
		super(parent);

	}

	@Override
	public String[] generateImportConditions() {
		return new String[] { "import org.openlowcode.server.data.properties.trigger.TriggerConditionDelete;" };
	}

	@Override
	public String generateDeclaration() {
		return "new TriggerConditionDelete()";
	}

}
