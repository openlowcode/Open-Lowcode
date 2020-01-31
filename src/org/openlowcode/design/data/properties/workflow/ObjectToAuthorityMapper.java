/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * An object to authority mapper will determine for an object the authority to
 * use for a workflow task. The authority has to be on the domain of the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
public abstract class ObjectToAuthorityMapper {
	private DataObjectDefinition object;
	private String message = null;
	private int days = 0;
	private String emaildelaytype;

	/**
	 * creates an object to authority mapper
	 * 
	 * @param object data object the workflow is running on
	 */
	public ObjectToAuthorityMapper(DataObjectDefinition object) {
		this.object = object;
	}

	/**
	 * @param message sets the task message
	 */
	public void setTaskMessage(String message) {
		this.message = message;
	}

	/**
	 * @return get the Message of the task
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return get the type of delay for sending tasks
	 */
	public String getEmailDelayType() {
		return this.emaildelaytype;
	}

	/**
	 * @param emaildelaytype get the e-mail delay type for the task
	 */
	public void setEmailDelayType(String emaildelaytype) {
		this.emaildelaytype = emaildelaytype;
	}

	/**
	 * @param days set the default delay for tasks in days
	 */
	public void setDefaultDelayForTask(int days) {
		this.days = days;
	}

	/**
	 * @return get the default delay for tasks
	 */
	public int getDefaultDelayForTask() {
		return this.days;
	}

	/**
	 * generate the helper task
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @param parent parent object
	 * @throws IOException if anything bad happens writing the file
	 */
	public abstract void generateSingleTaskPropertyHelperToFile(
			SourceGenerator sg,
			Module module,
			DataObjectDefinition parent) throws IOException;

	/**
	 * @return get the data object
	 */
	protected DataObjectDefinition getObject() {
		return this.object;
	}
}
