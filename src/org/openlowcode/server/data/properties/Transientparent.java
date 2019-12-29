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
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;

import org.openlowcode.server.data.storage.StoredField;

/**
 * A property to have on a transient object a parent. This is not persisted
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object
 * @param <F> the transient parent
 */
public class Transientparent<E extends DataObject<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>>
		extends DataObjectProperty<E> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Linkedtoparent.class.getName());
	private DataObjectDefinition<F> parentdefinition;
	private StoredField<String> pridfield;
	@SuppressWarnings("unused")
	private TransientparentDefinition<E, F> transientparentdefinition;

	@SuppressWarnings("unchecked")
	public Transientparent(TransientparentDefinition<E, F> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> parentdefinition) {
		super(definition, parentpayload);
		this.transientparentdefinition = definition;
		this.parentdefinition = parentdefinition;
		pridfield = (StoredField<String>) this.field.lookupOnName(this.getName() + "ID");

	}

	/**
	 * sets the id of the parent object
	 * 
	 * @param prid parent id
	 */
	protected void setPrid(DataObjectId<F> prid) {
		this.pridfield.setPayload(prid.getId());

	}

	/**
	 * gets the id of the parent object
	 * 
	 * @return the parent id
	 */
	public DataObjectId<F> getId() {
		return new DataObjectId<F>(this.pridfield.getPayload(), parentdefinition);
	}

	/**
	 * set parent id
	 * 
	 * @param object   object to add the parent to
	 * @param parentid id of the parent
	 */
	public void setparent(E object, DataObjectId<F> parentid) {

		this.pridfield.setPayload(parentid.getId());

	}

}
