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

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A widget is a graphical component put on a data object page for a property.
 * Widgets are compared by priority
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Widget
		extends
		org.openlowcode.tools.misc.Named
		implements
		Comparable<Widget> {
	Widget(String name) {
		super(name);
	}

	/**
	 * @return the import statements for the widget code generation
	 */
	public abstract String[] getImportStatements();

	/**
	 * generates the widget code
	 * 
	 * @param sg           source generator
	 * @param module       parent module
	 * @param locationname a prefix related to the location of this widget
	 * @throws IOException if anything bad happens writing the file
	 */
	public abstract void generateWidgetCode(SourceGenerator sg, Module module, String locationname) throws IOException;

	/**
	 * @return the widget display priority if specified
	 */
	public abstract WidgetDisplayPriority getWidgetPriority();

	@Override
	public int compareTo(Widget otherwidget) {

		WidgetDisplayPriority thispriority = this.getWidgetPriority();
		WidgetDisplayPriority otherpriority = otherwidget.getWidgetPriority();
		int thisprioritynr = 0;
		int otherprioritynr = 0;
		if (thispriority != null)
			thisprioritynr = thispriority.getPriority();
		if (otherpriority != null)
			otherprioritynr = otherpriority.getPriority();
		if (thisprioritynr > otherprioritynr)
			return -1;
		if (thisprioritynr < otherprioritynr)
			return 1;
		return 0;
	}
}
