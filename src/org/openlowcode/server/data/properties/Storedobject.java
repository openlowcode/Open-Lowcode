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

import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;

import org.openlowcode.server.data.formula.DataUpdateTrigger;
import org.openlowcode.server.data.formula.GalliumTriggerLauncher;

/**
 * Storedobject property specifies that the object can be persisted in the
 * database. Typically, all objects in the application except reports are
 * persisted. This property allows to
 * <ul>
 * <li>Stores the object in the database</li>
 * <li>Retrieves all objects in the database</il>
 * </ul>
 * Please note that this property does not provide a unique id. This is provided
 * by the separate property Uniqueidentified
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object this property applies to
 */
public class Storedobject<E extends DataObject<E>> extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Storedobject.class.getName());

	public Storedobject(StoredobjectDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);

	}

	/**
	 * insert an object in the database (persistent store)
	 * 
	 * @param object the object to insert
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insert(E object) {

		NamedList<DataUpdateTrigger> triggers = object.getDataUpdateTriggers();

		GalliumTriggerLauncher triggerlauncher = new GalliumTriggerLauncher(triggers);
		triggerlauncher.executeTriggerList(object);

		this.parentpayload.insert();
		logger.finest("Object just inserted " + object.dropToString());
	}

	/**
	 * Peforms massive insert of several objects
	 * 
	 * @param objectbatch       the list of objects to insert
	 * @param storedobjectbatch the corresponding stored object properties for each
	 *                          object
	 */
	public static <E extends DataObject<E>> void insert(E[] objectbatch, Storedobject<E>[] storedobjectbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (storedobjectbatch == null)
			throw new RuntimeException("storedobject batch is null");
		if (objectbatch.length != storedobjectbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with storedobject batch length " + storedobjectbatch.length);
		DataObjectPayload[] payloads = new DataObjectPayload[objectbatch.length];
		for (int i = 0; i < storedobjectbatch.length; i++) {
			payloads[i] = storedobjectbatch[i].parentpayload;
		}
		DataObjectPayload.massiveinsert(payloads);
		logger.finest("Massive insert just performed " + payloads.length);
	}

}
