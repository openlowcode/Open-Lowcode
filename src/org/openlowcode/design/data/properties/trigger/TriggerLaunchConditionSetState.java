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

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.TransitionChoiceCategory;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A trigger launch condition that will launch when the data object reaches
 * certain states
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TriggerLaunchConditionSetState
		extends
		TriggerLaunchCondition {
	private ChoiceValue[] selectedstates;
	private Lifecycle lifecycle;
	private TransitionChoiceCategory lifecyclechoicecategory;

	/**
	 * creates a trigger launch condition that will trigger when the object reaches
	 * certain states
	 * 
	 * @param parentobject   parent data object
	 * @param selectedstates the selected states on which the trigger launch
	 *                       condition will pop-up
	 */
	public TriggerLaunchConditionSetState(DataObjectDefinition parentobject, ChoiceValue[] selectedstates) {
		super(parentobject);
		this.selectedstates = selectedstates;
		this.lifecycle = (Lifecycle) parentobject.getPropertyByName("LIFECYCLE");
		if (this.lifecycle == null)
			throw new RuntimeException(
					"TriggerLaunchConditionSetState can only be used with an object with a lifecycle. The object "
							+ parentobject.getName() + " does not have a lifecycle.");
		if (selectedstates == null)
			throw new RuntimeException(" selected states should not be null for object " + parentobject.getName()
					+ " for TriggerLaunchConiditionSetState");
		if (selectedstates.length == 0)
			throw new RuntimeException("at least one selected state should be filled for object "
					+ parentobject.getName() + " for TriggerLaunchConiditionSetState");
		lifecyclechoicecategory = this.lifecycle.getTransitionChoiceCategory();
		for (int i = 0; i < selectedstates.length; i++) {
			ChoiceValue thisvalue = selectedstates[i];
			if (!lifecyclechoicecategory.hasChoiceValue(thisvalue))
				throw new RuntimeException("Choice value " + thisvalue.getName()
						+ " is not valid for lifecycle of object " + parentobject.getName());
		}
	}

	@Override
	public String[] generateImportConditions() {
		return new String[] { "import gallium.server.data.properties.trigger.TriggerConditionStateChange;" };
	}

	@Override
	public String generateDeclaration() {
		StringBuffer values = new StringBuffer("new ChoiceValue[]{");
		for (int i = 0; i < selectedstates.length; i++) {
			if (i > 0)
				values.append(",");
			values.append(StringFormatter.formatForJavaClass(lifecyclechoicecategory.getName()) + "ChoiceDefinition");
			values.append(".getChoice");
			values.append(StringFormatter.formatForJavaClass(selectedstates[i].getName()));
			values.append("()");
		}
		values.append("}");
		return "new TriggerConditionStateChange(" + values.toString() + ")";
	}

}
