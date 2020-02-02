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
 * This trigger will be executed after the object has been inserted.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TriggerLaunchConditionInsert
		extends
		TriggerLaunchCondition {

	/**
	 * create a trigger launch condition that is launching when the object is
	 * inserted in the data store for the first time
	 * 
	 * @param parent parent data object
	 */
	public TriggerLaunchConditionInsert(DataObjectDefinition parent) {
		super(parent);

	}

	@Override
	public String generateDeclaration() {
		return "new TriggerConditionCreate()";

	}

	@Override
	public String[] generateImportConditions() {
		return new String[] { "import org.openlowcode.server.data.properties.trigger.TriggerConditionCreate;" };
	}

}
