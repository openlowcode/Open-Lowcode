/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.pages;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.tools.misc.NamedList;

/**
 * A dynamic Page has input attributes. It cannot be displayed in a generic way
 * by the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class DynamicPageDefinition
		extends
		PageDefinition {
	private NamedList<ArgumentContent> pageinputparameters;

	/**
	 * creates a dynamic page definition with an empty list of input arguments
	 * 
	 * @param name name of the page. Should be unique for the module and a valid
	 *             java name
	 */
	public DynamicPageDefinition(String name) {
		super(name);
		pageinputparameters = new NamedList<ArgumentContent>();

	}

	/**
	 * @param parameter adds an input parameter to the page definition
	 */
	public void addInputParameter(ArgumentContent parameter) {
		this.pageinputparameters.add(parameter);
	}

	@Override
	public NamedList<ArgumentContent> getPageAttributes() {
		return pageinputparameters;
	}

	/**
	 * @param actiondefinition automatically adds all the action output attributes
	 *                         to the page. This allows to declare the attributes
	 *                         passed from the action to the page only on the action
	 */
	public void linkPageToAction(ActionDefinition actiondefinition) {
		for (int i = 0; i < actiondefinition.getOutputArgumentNumber(); i++) {
			this.pageinputparameters.add(actiondefinition.getOutputArgument(i));
		}
	}

}
