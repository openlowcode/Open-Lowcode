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

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.formula.DataUpdateTrigger;

import org.openlowcode.server.data.storage.FieldSchema;
import org.openlowcode.server.data.storage.StoredTableIndex;

/**
 * definition of a data object element. It stores all information about the type
 * of data element, such as list of fields...
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of field schemas that is authorized. This is mostly to
 *        separate fields that allow only Storedfields, and properties that also
 *        allow external fields.
 * @param <F> the data object this element definition is a part of
 */
@SuppressWarnings("rawtypes")
public abstract class DataObjectElementDefinition<E extends FieldSchema, F extends DataObject<F>> extends Named {
	private NamedList<DataUpdateTrigger<F>> triggerlist;
	private NamedList<E> definition;
	private NamedList<StoredTableIndex> indexlist;

	/**
	 * @return the list of triggers for this element
	 */
	public NamedList<DataUpdateTrigger<F>> getTriggerlist() {
		return triggerlist;
	}

	/**
	 * @return true if this element can be used as a formula element
	 */
	public boolean isFormulaElement() {
		return false;
	}

	/**
	 * @param trigger adds a trigger on data update
	 */
	public void setTriggerOnUpdate(DataUpdateTrigger<F> trigger) {

		triggerlist.addIfNew(trigger);
	}

	/**
	 * adds a field schema to the element
	 * 
	 * @param field the field schema to add
	 */
	protected void addFieldSchema(E field) {
		definition.add(field);
	}

	/**
	 * @param fieldname name of the field
	 * @return the field schema correponding to the name, null if nothing exists
	 */
	public E getFieldSchemaByName(String fieldname) {
		return definition.lookupOnName(fieldname);
	}

	/**
	 * adds a database index to this element
	 * 
	 * @param index the index to add
	 */
	public void addIndex(StoredTableIndex index) {
		indexlist.add(index);
	}

	/**
	 * creates a blank data object element definition
	 * 
	 * @param name name of the element
	 */
	public DataObjectElementDefinition(String name) {
		super(name);
		this.definition = new NamedList<E>();
		this.indexlist = new NamedList<StoredTableIndex>();
		this.triggerlist = new NamedList<DataUpdateTrigger<F>>();
	}

	/**
	 * @return the number of indexes
	 */
	public int getIndexNumber() {
		return this.indexlist.getSize();
	}

	/**
	 * @param i an integer between 0 (included) and getIndexNumber (excluded)
	 * @return
	 */
	public StoredTableIndex getIndexAt(int i) {
		return this.indexlist.get(i);
	}

	/**
	 * @return the number of fields
	 */
	public int getFieldSchemaNumber() {
		return definition.getSize();
	}

	/**
	 * @param index an integer between 0 (included) and get FieldSchemanumber
	 *              (excluded)
	 * @return the field (or an exception if out of range)
	 */
	public E getFieldSchema(int index) {
		return definition.get(index);
	}

	/**
	 * @return the list of fields as a string
	 */
	public String dropfieldnamelist() {
		return definition.dropNameList();
	}

	/**
	 * @return a namedlist of all fields of this element
	 */
	public NamedList<E> getDefinition() {
		return definition;
	}

	/**
	 * @param parentpayload the object payload
	 * @return an element corresponding to this element initiated with the
	 *         information in the object payload
	 */
	public abstract DataObjectElement<DataObjectElementDefinition<?, F>, F> initiateFieldInstance(
			DataObjectPayload parentpayload);

}
