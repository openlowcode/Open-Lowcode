/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

/**
 * A choice inside a task step
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TaskChoiceTemplate {

	private String code;
	private String name;

	/**
	 * creates a choice template
	 * 
	 * @param code code of the choice (unique)
	 * @param name name of the choice (nice readable)
	 */
	public TaskChoiceTemplate(String code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * @return gets the unique code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return get the name
	 */
	public String getName() {
		return name;
	}

}
