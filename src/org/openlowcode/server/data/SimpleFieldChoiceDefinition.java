/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.tools.misc.NamedList;

/**
 * This class allows to define a definition of a FieldChoice without transitions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the FieldChoiceDefinition
 */
public class SimpleFieldChoiceDefinition<E extends FieldChoiceDefinition<E>> extends FieldChoiceDefinition<E> {

	/**
	 * Creates a blank SimpleFieldChoiceDefinition
	 * @param storagesize the storage size of the code for one value
	 */
	public SimpleFieldChoiceDefinition(int storagesize) {
		super(storagesize);

	}

	/**
	 * Creates a SimpleFieldChoiceDefinition with the provided values
	 * @param storagesize the storage size of the code for one value
	 * @param choices the list of possible choices
	 */
	public SimpleFieldChoiceDefinition(int storagesize, NamedList<ChoiceValue<E>> choices) {
		super(storagesize);
		if (choices != null)
			for (int i = 0; i < choices.getSize(); i++) {
				this.addChoiceValue(choices.get(i));
			}
	}

	/**
	 * 
	 * @param valueasstored the code of one value
	 * @return ChoiceValue as parsed
	 */
	public ChoiceValue<E> parseValueFromStorageCode(String valueasstored) {
		return this.parseChoiceValue(valueasstored);
	}
}
