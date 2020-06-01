/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import java.util.function.BiConsumer;

import org.openlowcode.client.graphic.widget.fields.FormatValidator;
import org.openlowcode.client.graphic.widget.table.EditableTreeTable;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of the object in the editable Tree Table
 * @param <F> payload used for the value edition in editable Tree Table
 * @since 1.7
 */
public interface ObjectDataElementValueUpdater<E extends Object, F extends Object>
		extends
		ObjectDataElementKeyExtractor<E, F> {
	/**
	 * @return a function setting the new payload of type F to the object
	 */
	public BiConsumer<E, F> payloadIntegration();

	/**
	 * @return the operator to consolidate two F values (typically, this is the sum)
	 */
	public EditableTreeTable.Operator<F> operator();

	/**
	 * @return a function to validate the typed format by the user
	 */
	public FormatValidator<F> formatValidator();

}
