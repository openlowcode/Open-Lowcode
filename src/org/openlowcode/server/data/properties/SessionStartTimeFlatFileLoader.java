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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;


/**
 * loader for the start time of the session object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object of the session
 * @param <F> data object of the 'parent' Timeslot
 */
public class SessionStartTimeFlatFileLoader<E extends DataObject<E> & SessionInterface<E,F>  & UniqueidentifiedInterface<E>,F extends DataObject<F> & TimeslotInterface<F>> extends FlatFileLoaderColumn<E> {
	private DateTimeFormatter format;
	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private SessionDefinition<E,F> sessiondefinition;
	private PropertyExtractor<E> propertyextractor;
	private String dateformat;
	private CellStyle cellStyle;
	public SessionStartTimeFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,SessionDefinition<E,F> sessiondefinition,String dateformat,PropertyExtractor<E> propertyextractor)  {
		this.dataobjectdefinition = dataobjectdefinition;
		this.sessiondefinition = sessiondefinition;
		this.propertyextractor=propertyextractor;
		format = FlatFileLoader.generateFormat(dateformat,"Session Starttime for object "+dataobjectdefinition.getName());
		this.dateformat = dateformat;
	}
	@Override
	public boolean load(E object, Object value,PostUpdateProcessingStore<E> postupdateprocessingstore)  {
		Date date=FlatFileLoader.parseDate(object,"Session Starttime for object "+dataobjectdefinition.getName()+" for format "+dateformat,true, format);
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Session)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings("unchecked")
		Session<E,F> session = (Session<E,F>) property;
		Date olddate = session.getStarttime();
		if (FlatFileLoader.isTheSame(olddate,date)) {
			return false;
		} else {
			session.SetStarttime(date);
			return true;		
		}
		
	}
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context)  {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property==null) throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Session)) throw new RuntimeException("Technical error in inserting start date: property not of correct class: "+property.getClass().getName());
		@SuppressWarnings("unchecked")
		Session<E,F> session = (Session<E,F>) property;
		cell.setCellValue(session.getStarttime());
		if (cellStyle==null) 
			cellStyle = FlatFileExtractor.createDateStyle(cell.getSheet().getWorkbook(),this.dateformat);
		cell.setCellStyle(cellStyle);
		return true;
	}
}
