/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.storage.StoredField;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public class Typed<E extends DataObject<E> & TypedInterface<E, F>, F extends FieldChoiceDefinition<F>>
		extends
		DataObjectProperty<E> {
	private StoredField<String> type;
	private Uniqueidentified<E> uniqueidentified;
	private TypedDefinition<E, F> typeddefinition;

	public Typed(TypedDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.typeddefinition = definition;

	}

	public void settypebeforecreation(E object, ChoiceValue<F> typechoice) {
		if (typechoice == null)
			this.type.setPayload("");
		if (typechoice != null)
			this.type.setPayload(typechoice.getStorageCode());
		if (object.getId() != null)
			throw new RuntimeException("Type cannot be set after object was created");

	}

	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	public Uniqueidentified<E> getDependentPropertyUniqueidentified() {
		return this.uniqueidentified;
	}

	public void postprocStoredobjectInsert(E object) {
		// creates the companion object. 
		ChoiceValue<F> value = typeddefinition.getTypeChoice().parseChoiceValue(type.getPayload());
		HasidInterface<?> blankcompanion = typeddefinition.getHelper().generateBlankCompanion(value);
		blankcompanion.insert();
	}
}
