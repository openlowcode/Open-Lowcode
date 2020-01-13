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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.FieldChoiceDefinition;

/**
 * A choice field extractor will extract a choice payload from a choice field of
 * the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object the workflow is running on
 * @param <F> the definition of the choice field to extract
 */
public interface ChoiceFieldExtractor<E extends DataObject<E>, F extends FieldChoiceDefinition<F>> {
	/**
	 * Extracts the payload from a choice field in the object
	 * 
	 * @param object data object
	 * @return the choice value that is payload of the field
	 */
	public ChoiceValue<F> extractChoiceValue(E object);
}
