/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;


/**
 * A query condition that is always valid
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class QueryConditionAlways extends QueryCondition {

	@Override
	public void accept(Visitor visitor)  {
		visitor.visit(this);

	}

	@Override
	public boolean isSignificant(int circuitbreaker)  {
		return true;
	}

}
