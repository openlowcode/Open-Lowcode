/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.control;

import java.io.IOException;

import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A data control element checking that a text or choice field actually has some
 * content
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataControlElementFieldFilled
		extends
		DataControlElement {
	private Field field;

	/**
	 * creates a data control element on field actually filled
	 * 
	 * @param mainobject main data object
	 * @param field      field to check
	 */
	public DataControlElementFieldFilled(DataObjectDefinition mainobject, Field field) {
		super(mainobject);
		if (field.getParentObject() != mainobject)
			throw new RuntimeException("Field " + field.getName() + " is not in object " + mainobject.getName());
		this.field = field;
	}

	@Override
	public void writeRule(SourceGenerator sg, Module module, String objectclass) throws IOException {
		boolean treated = false;
		if (field instanceof ChoiceField) {
			sg.wl("		this.addOneRule(new DataControlRuleFieldFilled<" + objectclass + ",ChoiceValue>((a ->a.get"
					+ StringFormatter.formatForJavaClass(field.getName()) + "()),\"" + field.getDisplayname()
					+ "\", null));");
			treated = true;
		}
		if (field instanceof StringField) {
			sg.wl("		this.addOneRule(new DataControlRuleFieldFilled<" + objectclass + ",String>((a ->a.get"
					+ StringFormatter.formatForJavaClass(field.getName()) + "()),\"" + field.getDisplayname()
					+ "\", (s -> (s.length()>0?true:false))));");
			treated = true;
		}
		if (!treated)
			throw new RuntimeException(
					"Field " + field.getName() + " of type " + field.getClass().getName() + " not managed");
	}

}
