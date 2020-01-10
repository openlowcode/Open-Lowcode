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

import org.openlowcode.module.system.data.choice.ControllevelChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;

/**
 * A feedback is made of a type (ERROR, WARNING, VALID) and a message to be
 * interpreted by the user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataControlRuleFeedback {
	private String message;
	private ChoiceValue<ControllevelChoiceDefinition> feedbacktype;

	/**
	 * @return get the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return gets the feedback type
	 */
	public ChoiceValue<ControllevelChoiceDefinition> getFeedbacktype() {
		return feedbacktype;
	}

	/**
	 * creates a new feedback object
	 * 
	 * @param message      human readable message
	 * @param feedbacktype type of feedback (ERROR, WARNING, VALID)
	 */
	public DataControlRuleFeedback(String message, ChoiceValue<ControllevelChoiceDefinition> feedbacktype) {

		this.message = message;
		this.feedbacktype = feedbacktype;
	}

}
