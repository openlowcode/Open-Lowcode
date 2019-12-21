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

import java.util.logging.Logger;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.formula.DataUpdateTrigger;

import org.openlowcode.server.data.storage.Field;
import org.openlowcode.server.data.storage.Row;

import org.openlowcode.server.data.storage.TableAlias;

/**
 * A data Object Element is the base component of a data object. It is made of
 * some stored information, and possibly some business logic.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> class of the data object element
 * @param <F> data object this element is part of
 */
public abstract class DataObjectElement<E extends DataObjectElementDefinition<?, F>, F extends DataObject<F>>
		extends Named {
	private static Logger logger = Logger.getLogger(DataObjectElement.class.getName());
	@SuppressWarnings("rawtypes")
	protected NamedList<Field> field;
	protected E definition;
	protected DataObjectPayload parentpayload;

	/**
	 * This class needs to be implemented by every field or property that can launch
	 * an action (such as a calculated field)
	 * 
	 * @return the list of triggers to launch after an update action.
	 */
	public NamedList<DataUpdateTrigger<F>> getTriggersForThisUpdate() {
		return new NamedList<DataUpdateTrigger<F>>();
	}

	/**
	 * This class needs to be implemented by every field or property that can launch
	 * an action (such as a calculated field)
	 * 
	 * @return the list of triggers to launch after a refresh action.
	 */
	public NamedList<DataUpdateTrigger<F>> getAllTriggersForRefresh() {
		return new NamedList<DataUpdateTrigger<F>>();
	}

	/**
	 * @param name name of the field
	 * @return the field if it exists
	 */
	@SuppressWarnings("rawtypes")
	public Field getFieldFromName(String name) {
		return this.field.lookupOnName(name);
	}

	/**
	 * @param definition    definition of the element
	 * @param parentpayload payload of the object
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectElement(E definition, DataObjectPayload parentpayload) {
		super(definition.getName());
		this.definition = definition;

		field = new NamedList<Field>();
		for (int i = 0; i < definition.getFieldSchemaNumber(); i++) {
			field.add(definition.getFieldSchema(i).initBlankField());
		}
		this.parentpayload = parentpayload;
	}

	/**
	 * provides the number of fields in this element
	 * 
	 * @return an integer representing the number of fields
	 */
	public int getFieldNumber() {
		return this.field.getSize();
	}

	/**
	 * @param index an index between 0 (included) and getFieldNumber (excluded)
	 * @return the field if it exist, an exception is out of of range
	 */
	@SuppressWarnings("rawtypes")
	public Field getStoredField(int index) {
		return this.field.get(index);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initFromDB(Row row, TableAlias alias) {

		for (int i = 0; i < field.getSize(); i++) {
			Field thisfield = field.get(i);
			logger.finest("Init Element from DB from row " + row.hashCode() + ", element " + this.getName()
					+ ", sequence = " + i + " class = " + thisfield.getClass() + " field = " + field.hashCode());
			Object rowvalue = row.getValue(thisfield.getFieldSchema(), alias);
			thisfield.setReferencePayload(rowvalue);

		}
		this.postTreatmentAfterInitFromDB();
	}

	/**
	 * A placeholder to allow data object elements to perform some postprocessing
	 * after initialization from database
	 */
	public void postTreatmentAfterInitFromDB() {

	}
}
