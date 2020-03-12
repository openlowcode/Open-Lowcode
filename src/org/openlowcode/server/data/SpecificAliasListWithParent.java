/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A data object definition implements this interface if it manages several
 * typical list of aliases (managed by parameter) and conditions on alias based
 * on values on parent object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> the parameters of loading
 * @param <F> the parent object on which filter will happen
 */
public interface SpecificAliasListWithParent<E extends FieldChoiceDefinition<E>, F extends DataObject<F>> {
	/**
	 * @param choicevalue chosen value for list of alias
	 * @param parentid    : id of the parent to perform filter on
	 * @return all aliases that are either not specific or have restrictions that
	 *         include this choicevalue
	 */
	public String[] getSpecificAliasList(ChoiceValue<E> choicevalue, DataObjectId<F> parent);
}
