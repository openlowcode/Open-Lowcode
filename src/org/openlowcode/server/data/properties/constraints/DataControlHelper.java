/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import java.util.ArrayList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.LifecycleInterface;

/**
 * An object grouping a number of rules on a data object. The helper will
 * execute all the controls and provide all the feedback
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public abstract class DataControlHelper<E extends DataObject<E> & LifecycleInterface<E, ?>> {
	private ArrayList<DataControlRule<E>> controlrules;

	/**
	 * performs controls and provides all feedback
	 * 
	 * @param object data object
	 * @return the list of feedbacks
	 */
	public DataControlRuleFeedback[] performcontrols(E object) {
		ArrayList<DataControlRuleFeedback> feedback = new ArrayList<DataControlRuleFeedback>();
		for (int i = 0; i < controlrules.size(); i++) {
			DataControlRuleFeedback unitfeedback = controlrules.get(i).control(object);
			if (unitfeedback != null)
				feedback.add(unitfeedback);
		}
		return feedback.toArray(new DataControlRuleFeedback[0]);
	}

	/**
	 * creates an empty data control helper
	 */
	public DataControlHelper() {
		controlrules = new ArrayList<DataControlRule<E>>();
		addrules();
	}

	/**
	 * adds one rule to the data control helper
	 * 
	 * @param thisrule rule to add
	 */
	protected void addOneRule(DataControlRule<E> thisrule) {
		controlrules.add(thisrule);
	}

	/**
	 * method to be implemented by the helper adding all rules
	 */
	public abstract void addrules();
}
