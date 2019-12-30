/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;



import java.util.logging.Logger;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * a property specifying a unique business identifier (number) for this data
 * object. This can be generated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 */
public class Numbered<E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E>>
		extends DataObjectProperty<E> {
	private StoredField<String> nr;
	// dependent properties
	private Uniqueidentified<E> uniqueidentified;
	private NumberedDefinition<E> numbereddefinition;
	private static Logger logger = Logger.getLogger(Numbered.class.getName());

	@SuppressWarnings("unchecked")
	public Numbered(NumberedDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.numbereddefinition = definition;
		nr = (StoredField<String>) this.field.lookupOnName("NR");
	}

	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	public String getNr() {
		return this.nr.getPayload();
	}

	protected void setNr(String nr, E object) {
		if (!nr.equals(object.getNr())) {
			if (numbereddefinition.DoesNumberExist(nr, object))
				throw new RuntimeException("Number " + nr + " is not unique.");
			setNrUnsafe(nr);
		}
	}

	protected void setNrUnsafe(String nr) {
		if (nr.length() >= 64)
			throw new RuntimeException("Number is too long " + nr);
		this.nr.setPayload(nr);
	}

	public void setobjectnumber(E object, String nr) {
		this.setNr(nr, object);
		if (uniqueidentified.getId() != null)
			if (uniqueidentified.getId().getId() != null)
				if (uniqueidentified.getId().getId().length() > 0)
					object.update();
	}

	public void preprocStoredobjectInsert(E object) {
		if (numbereddefinition.getAutonumberingRule() == null)
			throw new RuntimeException(" this function can be called only if autonumbering property is set");
		boolean numbertogenerate = true;
		// if number already exists (for revise), do NOT generate new number
		if (this.getNr() != null)
			if (this.getNr().length() > 0)
				numbertogenerate = false;

		if (numbertogenerate)
			this.setNr(numbereddefinition.getAutonumberingRule().generateNumber(object), object);
	}

	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E>> void preprocStoredobjectInsert(
			E[] objectbatch, Numbered<E>[] preprocnumberedbatch) {
		if (preprocnumberedbatch[0].numbereddefinition.getAutonumberingRule() == null)
			throw new RuntimeException(" this function can be called only if autonumbering property is set");
		logger.warning("---- PreprocStoredobjectInsert is not tuned  for massive treatment ----------");
		for (int i = 0; i < objectbatch.length; i++) {
			preprocnumberedbatch[i].preprocStoredobjectInsert(objectbatch[i]);
		}
	}

}
