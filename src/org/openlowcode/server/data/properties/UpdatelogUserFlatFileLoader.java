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
 * a loader to force load the user that performed the last update
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object this loader applies to
 */
public class UpdatelogUserFlatFileLoader<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(CreationlogUserFlatFileLoader.class.getName());
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private UpdatelogDefinition<E> updatelogdefinition;
	private boolean createifnotexists;
	private PropertyExtractor<E> propertyextractor;
	private HashMap<String, Appuser> usersbynumber;

	public UpdatelogUserFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,
			UpdatelogDefinition<E> updatelogdefinition, boolean createifnotexists,
			PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.updatelogdefinition = updatelogdefinition;
		this.createifnotexists = createifnotexists;
		this.propertyextractor = propertyextractor;
		usersbynumber = new HashMap<String, Appuser>();

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting update date: property not found");
		if (!(property instanceof Updatelog))
			throw new RuntimeException("Technical error in inserting update date: property not of correct class: "
					+ property.getClass().getName());
		Updatelog<E> updatelog = (Updatelog<E>) property;
		Appuser user = null;
		String stringvalue = FlatFileLoader.parseObject(value, "CreationLogUser property");
		// user already known, use it
		if (usersbynumber.get(stringvalue) != null) {
			user = usersbynumber.get(stringvalue);
		} else {
			Appuser[] users = Appuser.getobjectbynumber(stringvalue);

			if (users.length == 1) {
				user = users[0];
				usersbynumber.put(stringvalue, user);
			} else {
				if (createifnotexists) {
					user = new Appuser();
					user.setobjectnumber(stringvalue);
					user.insert();
					usersbynumber.put(stringvalue, user);
				} else {
					logger.warning("Update log, user '" + stringvalue + "' does not exist, will put admin");
					
				}
			}
		}

		DataObjectId<Appuser> olduserid = updatelog.getUpdateuserid();
		if (user != null) {
			if (FlatFileLoader.isTheSame(olduserid, user.getId())) {
				return false;
			} else {
				updatelog.setUpdateuserid(user.getId().getId());
				updatelog.setuserByscript();
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
		if (!(property instanceof Updatelog))
			throw new RuntimeException("Technical error in inserting update date: property not of correct class: "
					+ property.getClass().getName());
		Updatelog<E> updatelog = (Updatelog<E>) property;
		if (updatelog.getUpdateuserid() != null)
			cell.setCellValue(updatelog.getUpdateuserid().getId());
		return false;
	}
}
