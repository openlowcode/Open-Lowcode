/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.Date;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;

/**
 * The interface all objects having targetdate as a property implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> the type of lifecycle (transition choice) of the corresponding
 *        lifecycle property
 */
public interface TargetdateInterface<E extends DataObject<E>, F extends TransitionFieldChoiceDefinition<F>>
		extends LifecycleInterface<E, F> {
	/**
	 * @return
	 */
	public Date getTargetdate();
}
