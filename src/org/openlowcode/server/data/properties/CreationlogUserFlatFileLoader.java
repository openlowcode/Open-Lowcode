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

import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * A loader allowing to force the creation user of an object when doing data
 * loading. users are loading by their 'number', typically the enterprise ID in
 * the enterprise LDAP directory
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object processed
 */
public class CreationlogUserFlatFileLoader<E extends DataObject<E> & CreationlogInterface<E>>
		extends FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(CreationlogUserFlatFileLoader.class.getName());
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private CreationlogDefinition<E> creationdefinition;
	private boolean createifnotexists;
	private PropertyExtractor<E> propertyextractor;
	private HashMap<String, Appuser> usersbynumber;

	/**
	 * @param dataobjectdefinition
	 * @param creationdefinition
	 * @param createifnotexists
	 * @param propertyextractor
	 */
	public CreationlogUserFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,
			CreationlogDefinition<E> creationdefinition, boolean createifnotexists,
			PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.creationdefinition = creationdefinition;
		this.createifnotexists = createifnotexists;
		this.propertyextractor = propertyextractor;
		usersbynumber = new HashMap<String, Appuser>();

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting creation date: property not found");
		if (!(property instanceof Creationlog))
			throw new RuntimeException("Technical error in inserting creation date: property not of correct class: "
					+ property.getClass().getName());
		Creationlog<E> creationlog = (Creationlog<E>) property;
		Appuser user = null;
		String stringvalue = FlatFileLoader.parseObject(value, "CreationLogUser property");
		// user already known, use it
		if (usersbynumber.get(stringvalue) != null) {
			user = usersbynumber.get(stringvalue);
		} else {
			

			
				if (createifnotexists) {
					user = new Appuser();
					user.setobjectnumber(stringvalue);
					user.insert();
					usersbynumber.put(stringvalue, user);
				} else {
					logger.warning("Creation log, user '" + stringvalue + "' does not exist, will put admin");
				}
			
		}

		DataObjectId<Appuser> olduserid = creationlog.getCreateuserid();
		if (user != null) {
			if (FlatFileLoader.isTheSame(olduserid, user.getId())) {
				return false;
			} else {
				creationlog.SetCreateuserid(user.getId().getId());
				return true;
			}
		}
		return false;

	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property == null)
			throw new RuntimeException("Technical error in inserting creation date: property not found");
		if (!(property instanceof Creationlog))
			throw new RuntimeException("Technical error in inserting creation date: property not of correct class: "
					+ property.getClass().getName());
		Creationlog<E> creationlog = (Creationlog<E>) property;
		if (creationlog.getCreateuserid() != null)
			cell.setCellValue(creationlog.getCreateuserid().getId());
		return false;
	}
}
