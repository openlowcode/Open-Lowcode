/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.typed;

import java.util.HashMap;
import java.util.function.Supplier;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.properties.CompanionInterface;
import org.openlowcode.server.data.properties.TypedInterface;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 *
 * @param <E>
 * @param <F>
 */
public class TypedHelper<E extends DataObject<E> & TypedInterface<E, F>, F extends FieldChoiceDefinition<F>> {
	private HashMap<ChoiceValue<F>, Supplier<CompanionInterface<?,E,F>>> suppliersbytype;

	public TypedHelper() {
		suppliersbytype = new HashMap<ChoiceValue<F>, Supplier<CompanionInterface<?,E,F>>>();
	}

	public CompanionInterface<?,E,F> generateBlankCompanion(ChoiceValue<F> type) {
		if (type==null) throw new RuntimeException("Provided choice is null");
		Supplier<CompanionInterface<?,E,F>> supplier = suppliersbytype.get(type);
		// typed should work if for some types, companion is not present
		if (supplier==null) return null;
		return supplier.get();
	}
	
	public void setCompanion(Supplier<CompanionInterface<?,E,F>> companionsupplier, ChoiceValue<F>[] typesforcompanion) {
		if (typesforcompanion == null)
			throw new RuntimeException("no types precised for companion (null array)");
		if (typesforcompanion.length == 0)
			throw new RuntimeException("No types precised for companion (zero element array)");
		for (int i = 0; i < typesforcompanion.length; i++)
			suppliersbytype.put(typesforcompanion[i], companionsupplier);
	}
}
