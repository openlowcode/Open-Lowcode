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

import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * Subobject allows to define a simple subobject to a main object. A subobject
 * cannot be linked to any other object, it does not have its main screen but is
 * always shown in the context of the parent screen There is iteration
 * management linked to the parent if the parent is iterated (property
 * IteratedSubObject)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SubObject
		extends
		PropertyBusinessRule<LinkedToParent<?>> {
	private LinkedToParent<?> linkedtoparent;
	private boolean showastable;

	/**
	 * Subobject allows to define a simple subobject to a main object. A subobject
	 * cannot be linked to any other object, it does not have its main screen but is
	 * always shown in the context of the parent screen There is iteration
	 * management linked to the parent if the parent is iterated (property
	 * IteratedSubObject)
	 * 
	 * @param linkedtoparent linked to parent from the subobject to the main object
	 * @param showastable    true to show the children as table, false to show the
	 *                       children as an object band
	 */
	public SubObject(LinkedToParent<?> linkedtoparent, boolean showastable) {
		super("SUBOJECT", false);
		this.linkedtoparent = linkedtoparent;
		this.showastable = showastable;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String linkedtoparentinstancename = StringFormatter.formatForAttribute(linkedtoparent.getInstancename());
		sg.wl("		linkedtoparentfor" + linkedtoparentinstancename + ".setSubObject(" + showastable + ");");

	}

	@Override
	public String[] getImportstatements() {

		return new String[0];
	}

	public boolean isShowastable() {
		return showastable;
	}

}
