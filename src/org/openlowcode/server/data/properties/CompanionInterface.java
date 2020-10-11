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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.FieldChoiceDefinition;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public interface CompanionInterface<
		E extends DataObject<E> & HasidInterface<E>,
		F extends DataObject<F> & TypedInterface<F, G>,
		G extends FieldChoiceDefinition<G>> {
	public void createtyped(F mainobject, ChoiceValue<G> type);

	public void insertcompanion(F mainobject);

	public void updatetyped(F mainobject);
}
