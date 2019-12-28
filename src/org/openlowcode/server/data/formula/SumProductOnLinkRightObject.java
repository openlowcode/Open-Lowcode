/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.formula;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.LinkobjectInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * This formula element will multiply an element on the link object and an
 * element on the right object and sum this for each link. An example would be
 * in a Bill of Material, with a quantity on the link and weight on each right
 * object, the sum product would give the weight of the left object as assembly
 * with given quantity of all the specified right objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left object
 * @param <F> link object
 * @param <G> right object
 */
public class SumProductOnLinkRightObject<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		implements FormulaElement<E> {
	private LinkNavigator<E, F, G> linknavigator;
	private FormulaElement<F> linkobjectelement;
	private FormulaElement<G> rightobjectelement;
	private final static Logger logger = Logger.getLogger(SumProductOnLinkRightObject.class.getName());

	/**
	 * creates a sum-product on link right object
	 * 
	 * @param linknavigator      navigator for the link
	 * @param linkobjectelement  formula element on the link to sum/multiply
	 * @param rightobjectelement formula element on the right object to sum/multiply
	 */
	public SumProductOnLinkRightObject(LinkNavigator<E, F, G> linknavigator, FormulaElement<F> linkobjectelement,
			FormulaElement<G> rightobjectelement) {
		this.linknavigator = linknavigator;
		this.linkobjectelement = linkobjectelement;
		this.rightobjectelement = rightobjectelement;
	}

	@Override
	public BigDecimal getValueForFormulaInput(E contextobject) {
		TwoDataObjects<F, G>[] rightlinksandobjects = linknavigator.getLinksAndRightObjects(contextobject);
		logger.severe("executing sum product on link right object for " + contextobject.getName() + " ID = "
				+ contextobject.getId());

		BigDecimal sum = new BigDecimal(0);
		if (rightlinksandobjects != null)
			for (int i = 0; i < rightlinksandobjects.length; i++) {
				F linkobject = rightlinksandobjects[i].getObjectOne();
				G rightobject = rightlinksandobjects[i].getObjectTwo();
				BigDecimal valueforlinkobject = linkobjectelement.getValueForFormulaInput(linkobject);
				BigDecimal valueforrightobject = rightobjectelement.getValueForFormulaInput(rightobject);
				logger.severe("looking for field on right object = " + rightobject.getName() + " ID = "
						+ rightobject.getId() + " value found = " + valueforrightobject);
				logger.severe("looking for field on link object = " + rightobject.getName() + " ID = "
						+ rightobject.getId() + " value found = " + valueforrightobject);
				BigDecimal product = null;
				if (valueforlinkobject != null)
					if (valueforrightobject != null)
						product = valueforlinkobject.multiply(valueforrightobject);
				if (product != null)
					sum = sum.add(product);
			}
		logger.severe("sum is " + sum.floatValue());
		return sum;
	}
}
