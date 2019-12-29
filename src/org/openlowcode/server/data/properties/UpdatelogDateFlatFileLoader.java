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

import java.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class UpdatelogDateFlatFileLoader<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	private DateTimeFormatter format;
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private UpdatelogDefinition<E> updatelogdefinition;
	private PropertyExtractor<E> propertyextractor;
	private String dateformat;
	private static Logger logger = Logger.getLogger(UpdatelogDateFlatFileLoader.class.getName());

	public UpdatelogDateFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,
			UpdatelogDefinition<E> updatelogdefinition, String dateformat, PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.updatelogdefinition = updatelogdefinition;
		this.propertyextractor = propertyextractor;
		this.format = FlatFileLoader.generateFormat(dateformat,
				" creationlog for object " + dataobjectdefinition.getName());
		this.dateformat = dateformat;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		Date newdate = FlatFileLoader.parseDate(value, "Creationlog CreationDate for format " + dateformat, false,
				format);
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting creation date: property not found");
		if (!(property instanceof Updatelog))
			throw new RuntimeException("Technical error in inserting update date: property not of correct class: "
					+ property.getClass().getName());
		Updatelog<E> updatelog = (Updatelog<E>) property;
		Date olddate = updatelog.getUpdatetime();
		if (FlatFileLoader.isTheSame(olddate, newdate)) {
			return false;
		} else {
			logger.fine("  *** dates are different " + olddate + " " + newdate);
			updatelog.setUpdatetime(newdate);
			return true;
		}

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
		cell.setCellValue(updatelog.getUpdatetime());
		return false;
	}
}
