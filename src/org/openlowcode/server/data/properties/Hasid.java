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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * The Hasid property stores a unique id of the data object. This id can be
 * filled with different business logic, especially
 * <ul>
 * <li>UniqueIdentified: an independent data object</li>
 * <li>Subobject Companion: a companion to a main data object</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class Hasid<E extends DataObject<E>>
		extends
		DataObjectProperty<E> {

	private StoredField<String> idfield;
	@SuppressWarnings("unused")
	private HasidDefinition<E> hasiddefinition;
	private StoredField<String> deletedfield;

	/**
	 * Creates the property Hasid
	 * 
	 * @param definition definition of the property
	 * @param parentpayload parent payload
	 */
	@SuppressWarnings("unchecked")
	public Hasid(HasidDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.hasiddefinition = definition;
		idfield = (StoredField<String>) this.field.lookupOnName("ID");
		deletedfield = (StoredField<String>) this.field.lookupOnName("DELETED");
	}

	/**
	 * @return the unique id of the object
	 */
	public DataObjectId<E> getId() {

		return new DataObjectId<E>(this.idfield.getPayload(), definition.getParentObject());
	}

	protected void SetId(String id) {
		this.idfield.setPayload(id);

	}
	
	protected void setDeleted(String deleted) {
		this.deletedfield.setPayload(deleted);
	}
	
	public String getDeleted() {
		return this.deletedfield.getPayload();
	}

	/**
	 * @return the Hasid property definition
	 */
	public HasidDefinition<E> getDefinition() {
		return hasiddefinition;
	}

}
