/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.math.BigDecimal;

import org.openlowcode.server.data.DataObject;


/**
 * the interface all objects having the flexible decimal fields property implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface FlexibledecimalfieldsInterface<E extends DataObject<E>> {
	
	/**
	 * add the value to the flexible decimal value with the given name
	 * 
	 * @param name name of the flexible decimal value (not the label)
	 * @param value BigDecimal value
	 */
	public void addflexibledecimalvalue(/*discarded - OBJECT*/String name,BigDecimal value);	
	/**
	 * gets the flexible decimal value with the given name
	 * 
	 * @param name name of the flexible decimal value (not the label)
	 * @return BigDecimal value
	 */
	public BigDecimal getflexibledecimalvalue(/*discarded - OBJECT*/String name) ;
}
