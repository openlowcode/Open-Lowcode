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
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.generation.StringFormatter;

/**
 * this will sum on the left object the given formula element on the right
 * object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SumOnLinkRightObject
		implements
		FormulaDefinitionElement,
		SignificantTriggerPath {
	private FormulaDefinitionElement childelementtosum;
	private LinkObject<?, ?> linkedobjectproperty;

	/**
	 * creates a formula element summing an element on all the right objects into
	 * the left object for the link
	 * 
	 * @param linkedobjectproperty link object property on the link object
	 * @param childelementtosum    a child element on the right object to sum
	 */
	public SumOnLinkRightObject(LinkObject<?, ?> linkedobjectproperty, FormulaDefinitionElement childelementtosum) {
		this.linkedobjectproperty = linkedobjectproperty;
		this.childelementtosum = childelementtosum;
		if (!(linkedobjectproperty.getRightobjectforlink().equals(childelementtosum.getOwnerObject())))
			throw new RuntimeException("object not consistent, link object right = "
					+ linkedobjectproperty.getRightobjectforlink().getName() + ", element to sum on object = "
					+ childelementtosum.getOwnerObject().getName());
	}

	@Override
	public String generatePath(String fullpath) {
		String linkobjectclass = StringFormatter.formatForJavaClass(linkedobjectproperty.getParent().getName());
		return "new PathToCalculatedField(" + linkobjectclass + "Definition.get" + linkobjectclass
				+ "Definition().getLinkReverseNavigator()," + fullpath + ")";

	}

	@Override
	public DataObjectDefinition getOwnerObject() {
		return linkedobjectproperty.getLeftobjectforlink();
	}

	@Override
	public String generateFormulaElement() {

		String linkobjectclass = StringFormatter.formatForJavaClass(linkedobjectproperty.getParent().getName());
		return "new SumOnLinkRightObject(" + linkobjectclass + "Definition.get" + linkobjectclass
				+ "Definition().getLinkNavigator()," + childelementtosum.generateFormulaElement() + ")";
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
}
