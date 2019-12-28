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
 * Sums on the left object calculated field the value of a formula element on
 * all right objects linked to the left object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left object
 * @param <F> link ojbect
 * @param <G> right object
 */
public class SumOnLinkRightObject<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		implements FormulaElement<E> {
	private LinkNavigator<E, F, G> linknavigator;
	private FormulaElement<G> rightobjectelement;
	private final static Logger logger = Logger.getLogger(SumOnLinkRightObject.class.getName());

	/**
	 * Creates a new SumOnLinkRightObject
	 * @param linknavigator a navigator for the link
	 * @param rightobjectelement the element on the right object to sum
	 */
	public SumOnLinkRightObject(LinkNavigator<E, F, G> linknavigator, FormulaElement<G> rightobjectelement) {
		this.linknavigator = linknavigator;
		this.rightobjectelement = rightobjectelement;
	}

	@Override
	public BigDecimal getValueForFormulaInput(E contextobject) {
		logger.severe(
				"executing sm on link right object for " + contextobject.getName() + " ID = " + contextobject.getId());
		TwoDataObjects<F, G>[] rightlinksandobjects = linknavigator.getLinksAndRightObjects(contextobject);
		logger.fine("found " + rightlinksandobjects.length + " links and right objects");
		BigDecimal sum = new BigDecimal(0);
		if (rightlinksandobjects != null)
			for (int i = 0; i < rightlinksandobjects.length; i++) {
				G rightobject = rightlinksandobjects[i].getObjectTwo();
				BigDecimal valueforrightobject = rightobjectelement.getValueForFormulaInput(rightobject);
				logger.fine("looking for field on right object = " + rightobject.getName() + " ID = "
						+ rightobject.getId() + " value found = " + valueforrightobject);

				if (valueforrightobject != null)
					sum = sum.add(valueforrightobject);
			}
		logger.fine("sum is " + sum.floatValue());
		return sum;
	}
}
