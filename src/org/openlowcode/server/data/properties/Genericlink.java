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

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.tools.trace.ExceptionLogger;

/**
 * A generic link allows to put a link to any data object. This is for example
 * used in the workflow object to link to the subject of the workflow that can
 * be linked to any object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class Genericlink<E extends DataObject<E>> extends DataObjectProperty<E> {
	private StoredField<String> idfield;
	private StoredField<String> objecttypefield;
	private static Logger logger = Logger.getLogger(Genericlink.class.getName());

	/**
	 * creates a generic link property
	 * 
	 * @param definition    definition of the property
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public Genericlink(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		idfield = (StoredField<String>) this.field.lookupOnName(this.getName() + "ID");
		objecttypefield = (StoredField<String>) this.field.lookupOnName(this.getName() + "OBJECTTYPE");

	}

	/**
	 * get the data object id of the object in the generic link)
	 * 
	 * @param object parent data object (having the generic link property)
	 * @return the data object id of the object in the generic link property
	 */
	public DataObjectId<?> getlinkedobjectid(E object) {
		return DataObjectId.generateDataObjectId(idfield.getPayload(), objecttypefield.getPayload());
	}

	/**
	 * sets the linked object id in the generic link
	 * 
	 * @param object object with the generic link
	 * @param parent id of the object that is referenced in the generic link
	 */
	public void setlinkedobjectid(E object, DataObjectId<?> parent) {
		idfield.setPayload(parent.getId());
		objecttypefield.setPayload(parent.getObjectId());

	}

	/**
	 * get the id of the data object referenced in the generic link
	 * 
	 * @return the data object id of the data object referenced in the generic link
	 */
	public DataObjectId<?> getId() {
		try {
			return DataObjectId.generateDataObjectId(idfield.getPayload(), objecttypefield.getPayload());
		} catch (Exception e) {
			ExceptionLogger.setInLogs(e, logger);
			return null;
		}
	}

}
