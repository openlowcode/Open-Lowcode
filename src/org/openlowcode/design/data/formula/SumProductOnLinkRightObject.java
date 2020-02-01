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
 * This product will put on the left object the result of the sum of the product
 * of a field on the link and a field of the right object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SumProductOnLinkRightObject
		implements
		FormulaDefinitionElement,
		SignificantTriggerPath {
	private LinkObject<?, ?> linkedobjectproperty;
	private FormulaDefinitionElement childelementtosum;
	private FormulaDefinitionElement linkelementforproduct;

	private class PathToLink
			implements
			SignificantTriggerPath {
		private LinkObject<?, ?> linkobjectproperty;

		public PathToLink(LinkObject<?, ?> linkobjectproperty) {
			this.linkobjectproperty = linkobjectproperty;
		}

		@Override
		public String generatePath(String fullpath) {
			String linkobjectclass = StringFormatter.formatForJavaClass(linkobjectproperty.getParent().getName());
			return "new PathToCalculatedField(" + linkobjectclass + "Definition.get" + linkobjectclass
					+ "Definition().getLinkToLeftReverseNavigator()," + fullpath + ")";

		}

	}

	/**
	 * creates a sum product formula element
	 * 
	 * @param linkedobjectproperty  link object property used to consolidate data on
	 *                              the left object for link
	 * @param linkelementforproduct the element on the link object to sum / multiply
	 * @param childelementtosum     the element on the right object to sum /
	 *                              multiply
	 */
	public SumProductOnLinkRightObject(
			LinkObject<?, ?> linkedobjectproperty,
			FormulaDefinitionElement linkelementforproduct,
			FormulaDefinitionElement childelementtosum) {
		this.linkedobjectproperty = linkedobjectproperty;
		this.childelementtosum = childelementtosum;
		this.linkelementforproduct = linkelementforproduct;
		if (!(linkedobjectproperty.getRightobjectforlink().equals(childelementtosum.getOwnerObject())))
			throw new RuntimeException("Owner object not consistent, link object right = "
					+ linkedobjectproperty.getRightobjectforlink().getName() + ", element to sum on object = "
					+ childelementtosum.getOwnerObject().getName());
		if (!(linkedobjectproperty.getParent().equals(linkelementforproduct.getOwnerObject())))
			throw new RuntimeException(
					"Owner object not consistent, link object = " + linkedobjectproperty.getParent().getName()
							+ ", element to multiply on link = " + linkelementforproduct.getOwnerObject().getName());

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
		return "new SumProductOnLinkRightObject(" + linkobjectclass + "Definition.get" + linkobjectclass
				+ "Definition().getLinkNavigator()," + linkelementforproduct.generateFormulaElement() + ","
				+ childelementtosum.generateFormulaElement() + ")";

	}

	@Override
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath) {
		CalculatedFieldTriggerPath pathforlink = triggerpath.clone();
		triggerpath.addPath(this);
		childelementtosum.setTriggersOnSourceFields(triggerpath);
		pathforlink.addPath(new PathToLink(linkedobjectproperty));
		linkelementforproduct.setTriggersOnSourceFields(pathforlink);
	}

	@Override
	public CalculatedFieldTriggerPath[] getAllTriggerPaths() {
		return new CalculatedFieldTriggerPath[0];
	}
}
