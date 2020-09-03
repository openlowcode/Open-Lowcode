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
 * a generator for one dynamic alias according to data on the parent of the
 * loading object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> main loading data object
 * @param <F> parent data object
 * @param <G> type of choice value for export (by convention, BooleanChoiceDefinitin is used when there is no choice value defined)
 * @since 1.11
 */
public interface FlatFileExtractorDynamicAliasParentFilter<E extends DataObject<E>, F extends DataObject<F>,G extends FieldChoiceDefinition<G>> extends FlatFileExtractorDynamicAliasFilter<E,G> {


	/**
	 * Checks if the variable part of the dynamic alias parent generator is valid
	 * 
	 * @param loadedobjectdefinition
	 * @param parent parent 
	 * @param variablepart variable part of the dynamic alias
	 * @param value                  export type value (or null if not used or no
	 *                               value selected)
	 * @return
	 */
	public boolean isVariablePartValid(DataObjectDefinition<E> loadedobjectdefinition, F parent, String variablepart,ChoiceValue<G> value);

	/**
	 * selects the columns to show when exporting several objects in the context of a parent object
	 * 
	 * @param loadedobjectdefinition definition of the object to load
	 * @param parent                 parent
	 * @param value                  export type value (or null if not used or no
	 *                               value selected)
	 * @return the list of dynamic aliases variable part
	 */
	public String[] generateForExport(DataObjectDefinition<E> loadedobjectdefinition, F parent,ChoiceValue<G> value);


	
}
