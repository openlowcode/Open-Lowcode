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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;

/**
 * the interface all objects with simple tasks workflow comply to. It does not
 * have currently any methods in addition to the general WorkflowInterface of
 * the Open Lowcode framework
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> lifecycle on which the simple task workflow is running
 */
public interface SimpletaskworkflowInterface<E extends DataObject<E>, 
F extends TransitionFieldChoiceDefinition<F>>
		extends WorkflowInterface {

}
