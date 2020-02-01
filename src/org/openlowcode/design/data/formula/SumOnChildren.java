/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.formula;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.StringFormatter;

/**
 * This formula element will sum a formula element on all children of the
 * current object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SumOnChildren
		implements
		FormulaDefinitionElement,
		SignificantTriggerPath {
	private FormulaDefinitionElement childelementtosum;
	private LinkedToParent<?> linkedtoparentforsum;

	/**
	 * creates a sum on children formula element
	 * 
	 * @param linkedtoparentforsum the linked to parent used for the sum
	 * @param childelementtosum    the formula element on the child element to sum
	 */
	public SumOnChildren(LinkedToParent<?> linkedtoparentforsum, FormulaDefinitionElement childelementtosum) {
		this.linkedtoparentforsum = linkedtoparentforsum;
		this.childelementtosum = childelementtosum;
		if (!childelementtosum.getOwnerObject().equals(this.linkedtoparentforsum.getParent()))
			throw new RuntimeException("Inconsistent objects used, FormulaElement linkedtoparent for object '"
					+ linkedtoparentforsum.getParent().toString()
					+ " is not consistent with element to sum for data object " + childelementtosum.getOwnerObject());
	}

	@Override
	public DataObjectDefinition getOwnerObject() {
		return this.linkedtoparentforsum.getParentObjectForLink();
	}

	@Override
	public String generateFormulaElement() {
		return "new SumOnChildren(this.getLinkedfromchildrenfor"
				+ this.linkedtoparentforsum.getLinkedFromChildrenName().toLowerCase() + "Navigator(),"
				+ childelementtosum.generateFormulaElement() + ")";
	}

	@Override
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath) {
		triggerpath.addPath(this);
		childelementtosum.setTriggersOnSourceFields(triggerpath);
	}

	@Override
	public CalculatedFieldTriggerPath[] getAllTriggerPaths() {
		return new CalculatedFieldTriggerPath[0];

	}

	@Override
	public String generatePath(String fullpath) {
		String childclass = StringFormatter.formatForJavaClass(linkedtoparentforsum.getParent().getName());
		return "new PathToCalculatedField(" + childclass + "Definition.get" + childclass + "Definition().getParentfor"
				+ linkedtoparentforsum.getInstancename().toLowerCase() + "Navigator()," + fullpath + ")";
	}

}
