/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.trigger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * A common interface to all elements generating trigger to execute on an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
public interface CustomTriggerExecutionFactory<E extends DataObject<E> & UniqueidentifiedInterface<E>> {

	/**
	 * method to call to generate all the triggers on an object
	 * 
	 * @return a list of triggers to execute
	 */
	public CustomTriggerExecution<E> generate();

}
