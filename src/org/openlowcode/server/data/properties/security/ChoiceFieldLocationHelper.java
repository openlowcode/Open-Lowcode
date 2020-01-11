/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.security;

import java.util.function.Function;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.properties.LocatedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object for which this location helper is used
 * @param <F> definition of the choice used by the cchoice field
 */
public class ChoiceFieldLocationHelper<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>, F extends FieldChoiceDefinition<F>>
		extends StringLocationHelper<E> {
	private String defaultdomainnr;
	private Function<E, ChoiceValue<F>> choicevalueextractor;
	private Function<String, String> choicecodetodomainnumber;

	/**
	 * Creates the location helper using a choice field on the object
	 * 
	 * @param defaultdomainnr          default domain number
	 * @param choicevalueextractor     extractor that takes an object in input and
	 *                                 provides the choice value as output
	 * @param choicecodetodomainnumber mapping from choice storage code to domain
	 *                                 number
	 */
	public ChoiceFieldLocationHelper(String defaultdomainnr, Function<E, ChoiceValue<F>> choicevalueextractor,
			Function<String, String> choicecodetodomainnumber) {
		this.defaultdomainnr = defaultdomainnr;
		this.choicevalueextractor = choicevalueextractor;
		this.choicecodetodomainnumber = choicecodetodomainnumber;
	}

	@Override
	public String getObjectLocationNumber(E object) {
		ChoiceValue<F> choicevalue = this.choicevalueextractor.apply(object);
		if (choicevalue == null)
			return defaultdomainnr;
		String domainnr = this.choicecodetodomainnumber.apply(choicevalue.getStorageCode());
		if (domainnr == null)
			return defaultdomainnr;
		return domainnr;
	}

}
