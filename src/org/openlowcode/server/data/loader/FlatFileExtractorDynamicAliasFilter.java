/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;

/**
 * a generator for one dynamic alias
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> main loading data object
 * @since 1.11
 */
public interface FlatFileExtractorDynamicAliasFilter<E extends DataObject<E>, F extends FieldChoiceDefinition<F>> {
	/**
	 * selects the columns to show when exporting several objects without the
	 * context of a parent object
	 * 
	 * @param loadedobjectdefinition definition of the object to load
	 * @param parent                 parent
	 * @param value                  export type value (or null if not used or no
	 *                               value selected)
	 * @return the list of dynamic aliases variable part
	 */
	public String[] generateForExportWithoutContext(
			DataObjectDefinition<E> loadedobjectdefinition,
			ChoiceValue<F> value);
}
