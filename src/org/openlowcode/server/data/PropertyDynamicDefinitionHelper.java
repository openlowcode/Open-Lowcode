/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay;

/**
 * An helper is defined at runtime during an action. It should be assigned to
 * all objects created.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object
 * @param <F> the property concerned
 */
public abstract class PropertyDynamicDefinitionHelper<E extends DataObject<E>, F extends DataObjectProperty<E>> {

	/**
	 * @return the list of fields to display for this specific use-case of the
	 *         property (typically, in the scope of an action
	 * 
	 */
	public abstract FieldSchemaForDisplay<E>[] getFieldsToDisplay();
}
