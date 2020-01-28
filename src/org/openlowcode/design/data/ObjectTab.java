/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import org.openlowcode.tools.misc.Named;

/**
 * A tab in the object page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectTab
		extends
		Named {
	private String label;
	private DataObjectDefinition owner;

	/**
	 * create a new object tab in a data object display page
	 * 
	 * @param name  unique name for the tab. Should be a valid java field name
	 * @param label label of the tab in the default language of the application
	 * @param owner parent data object for the object tab
	 */
	public ObjectTab(String name, String label, DataObjectDefinition owner) {
		super(name);
		this.label = label;
		this.owner = owner;
		owner.setObjectTab(this);
	}

	/**
	 * @return the owner data object for the tab
	 */
	public DataObjectDefinition getOwner() {
		return this.owner;
	}

	/**
	 * @return the label of the tab
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * @return get the widget name (name + 'tab' suffix)
	 */
	public String getWidgetName() {
		return this.getName().toLowerCase() + "tab";
	}
}
