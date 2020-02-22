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

/**
 * A data object definition implements this library if it manages several
 * typical list of aliases
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E>
 */
public interface SpecificAliasList<E extends FieldChoiceDefinition<E>> {
	/**
	 * @param choicevalue chosen value for list of alias
	 * @return all aliases that are either not specific or have restrictions that
	 *         include this choicevalue
	 */
	public String[] getSpecificAliasList(ChoiceValue<E> choicevalue);
}
