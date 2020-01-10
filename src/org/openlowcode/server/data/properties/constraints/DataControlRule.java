/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import org.openlowcode.server.data.DataObject;

/**
 * A rule to check on a data object. It should perform controls with the minimum
 * access to persistence layer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
public abstract class DataControlRule<E extends DataObject<E>> {
	public abstract DataControlRuleFeedback control(E object);
}
