/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.HashMap;

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.14
 *
 */
public class ConstraintOnLinkTypeRestrictionForLeft
		extends
		PropertyBusinessRule<LinkObject<? super DataObjectDefinition, ? super DataObjectDefinition>> {

	private ChoiceValue[] allowedtypes;
	private HashMap<ChoiceValue, String> alternativelabels;

	/**
	 * @param allowedtypes
	 */
	public ConstraintOnLinkTypeRestrictionForLeft(ChoiceValue[] allowedtypes) {
		super("TYPERESTRICTIONFORLEFT", false);
		if (allowedtypes == null)
			throw new RuntimeException("no allowed types entered");
		if (allowedtypes.length == 0)
			throw new RuntimeException("no allowed types entered");
		this.allowedtypes = allowedtypes;
		this.alternativelabels = new HashMap<ChoiceValue, String>();
	}

	/**
	 * @param type
	 * @param alternativelabel
	 */
	public void addSpecificLabelFromLeft(ChoiceValue type, String alternativelabel) {
		boolean istypepresent = false;
		for (int i = 0; i < allowedtypes.length; i++) {
			if (allowedtypes[i].equals(type)) {
				istypepresent = true;
				break;
			}
		}
		if (!istypepresent)
			throw new RuntimeException(
					"Type " + type.getName() + " is not valid for alternative label " + alternativelabel);
		alternativelabels.put(type, alternativelabel);
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getImportstatements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkBeforeGeneration(Property<?> parentproperty) {
		if (!(parentproperty instanceof LinkObject))
			throw new RuntimeException(
					"Business rule added to bad type of property " + parentproperty.getClass().getName());
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) parentproperty;
		DataObjectDefinition leftobject = linkobject.getLeftobjectforlink();
		Typed typed = (Typed) leftobject.getPropertyByName("TYPED");
		if (typed == null)
			throw new RuntimeException("Left object " + leftobject.getName() + " for link "
					+ linkobject.getParent().getName() + " is not typed");
		SimpleChoiceCategory type = typed.getTypes();
		for (int i = 0; i < allowedtypes.length; i++) {
			if (!type.isKeyPresent(allowedtypes[i].getName()))
				throw new RuntimeException("Choice Value " + allowedtypes[i].getName()
						+ " is not valid for type of object " + leftobject.getName());
		}
	}

}
