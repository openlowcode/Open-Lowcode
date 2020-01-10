/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import org.openlowcode.server.data.DataObject;

/**
 * an interface allowing to extract the required field from an object for
 * further processing. Typically used in workflows
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@FunctionalInterface
public interface DataObjectExtractor<E extends DataObject<E>, F extends Object> {
	/**
	 * extract from object an information of the given type
	 * 
	 * @return an element
	 */
	public F extract(E contextobject);

}
