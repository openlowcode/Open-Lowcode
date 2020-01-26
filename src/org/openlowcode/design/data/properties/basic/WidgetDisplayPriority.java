/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.ObjectTab;

/**
 * This modifier allows to specify where on an object page to put a specific
 * widget. It is possible to create tabs, and to reorder standard property
 * widgets on those tabs
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class WidgetDisplayPriority {
	private int priority;
	private ObjectTab tab;

	/**
	 * creates a widget display priority
	 * 
	 * @param priority priority (the higher the number, the more priority).
	 *                 Recommended usage is between -1000 and 1000
	 * @param tab      the tab on the object where the widget will be shown
	 */
	public WidgetDisplayPriority(int priority, ObjectTab tab) {
		this.priority = priority;
		this.tab = tab;

	}

	/**
	 * @return the priority (a number between -1000 and 1000)
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the tab on the object page to put the widget in
	 */
	public ObjectTab getTab() {
		return tab;
	}

	/**
	 * will check if the widget display priority is valid for the given object.
	 * Checks that the tab belongs to the object
	 * 
	 * @param object data object
	 */
	public void checkIfValidForObject(DataObjectDefinition object) {
		if (tab != null)
			if (!tab.getOwner().equals(object))
				throw new RuntimeException("Adding a widget display priority for " + object.getName()
						+ " but uses a tab for " + tab.getOwner().getName());
	}
}
