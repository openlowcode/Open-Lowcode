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


import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * loader for the sequence of the session object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object of the session
 * @param <F> data object of the 'parent' Timeslot
 */
public class SessionSequenceFlatFileLoader<E extends DataObject<E> & SessionInterface<E, F> & UniqueidentifiedInterface<E>, F extends DataObject<F> & TimeslotInterface<F>>
		extends FlatFileLoaderColumn<E> {

	private DataObjectDefinition<E> dataobjectdefinition;
	@SuppressWarnings("unused")
	private SessionDefinition<E, F> sessiondefinition;
	private PropertyExtractor<E> propertyextractor;

	/**
	 * creates a loader for the sequence of a session property
	 * 
	 * @param dataobjectdefinition parent object definition
	 * @param sessiondefinition    definition of the session object
	 * @param propertyextractor    a property extractor
	 */
	public SessionSequenceFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition,
			SessionDefinition<E, F> sessiondefinition, PropertyExtractor<E> propertyextractor) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.sessiondefinition = sessiondefinition;
		this.propertyextractor = propertyextractor;

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		Integer integervalue = FlatFileLoader.parseInteger(value,
				"Session Sequence for object " + dataobjectdefinition.getName());

		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Session))
			throw new RuntimeException("Technical error in inserting start date: property not of correct class: "
					+ property.getClass().getName());
		@SuppressWarnings("unchecked")
		Session<E, F> session = (Session<E, F>) property;
		Integer oldsequence = session.getSequence();
		if (FlatFileLoader.isTheSame(oldsequence, integervalue)) {
			return false;
		} else {
			session.setSequence(integervalue);
			return true;
		}

	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property == null)
			throw new RuntimeException("Technical error in inserting start date: property not found");
		if (!(property instanceof Session))
			throw new RuntimeException("Technical error in inserting start date: property not of correct class: "
					+ property.getClass().getName());
		@SuppressWarnings("unchecked")
		Session<E, F> session = (Session<E, F>) property;
		Integer sequence = session.getSequence();
		if (sequence != null)
			cell.setCellValue(session.getSequence().doubleValue());
		return false;
	}
}
