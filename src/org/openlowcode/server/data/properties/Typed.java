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

	@SuppressWarnings("unchecked")
	public Typed(TypedDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.typeddefinition = definition;
		type = (StoredField<String>) this.field.lookupOnName("TYPE");
	}

	public void settypebeforecreation(E object, ChoiceValue<F> typechoice) {
		if (typechoice == null)
			this.type.setPayload("");
		if (typechoice != null)
			this.type.setPayload(typechoice.getStorageCode());
		boolean significant = false;
		if (object.getId() != null)
			if (object.getId().getId() != null)
				if (object.getId().getId().length() > 0)
					significant = true;
		if (significant)
			throw new RuntimeException("Type cannot be set after object was created, ID is set as "
					+ (object.getId() != null ? object.getId().getId() : "NULL"));

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
		CompanionInterface<?, E, F> blankcompanion = typeddefinition.getHelper().generateBlankCompanion(value);
		blankcompanion.insertcompanion(object);
	}

	/**
	 * creates by batch the companion objects. This is not yet performance optimized
	 * 
	 * @param object            a batch of object
	 * @param preproctypesbatch the corresponding typed properties
	 */
	public static <
			E extends DataObject<E> & TypedInterface<E, F>,
			F extends FieldChoiceDefinition<F>> void postprocStoredobjectInsert(
					E[] object,
					Typed<E, F>[] preproctypesbatch) {
		for (int i = 0; i < object.length; i++)
			preproctypesbatch[i].postprocStoredobjectInsert(object[i]);
	}
	public String getType() {
		return this.type.getPayload();
	}

}
