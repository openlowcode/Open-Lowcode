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

import java.util.ArrayList;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * definition of the timeslot with session property. This property is added on
 * the timeslot object to mean it has detailed sessions
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> The timeslot object (total calendar period for an activity
 * @param <F> the session object. A child of the timeslot, it stores any precise
 *        session
 */
public class TimeslotwithsessionsDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & TimeslotInterface<E> & ScheduleInterface<E, ?>, F extends DataObject<F> & SessionInterface<F, E> & UniqueidentifiedInterface<F>>
		extends DataObjectPropertyDefinition<E> {

	@SuppressWarnings("unused")
	private DataObjectDefinition<F> childsessiondefinition;
	private SessionDefinition<F, E> childrensessionpropertydefinition;
	@SuppressWarnings("unused")
	private ScheduleDefinition<E, ?> scheduledefinition;
	private UniqueidentifiedDefinition<E> uniqueidentified;

	/**
	 * @return the unique identified definition for this object
	 */
	public UniqueidentifiedDefinition<E> getUniqueIdentifiedDefinition() {
		return this.uniqueidentified;
	}

	/**
	 * @return the definition of the property session on the session child object
	 */
	public SessionDefinition<F, E> getChildrenSessionDefinition() {
		return this.childrensessionpropertydefinition;
	}

	/**
	 * sets the dependent property schedule definition
	 * 
	 * @param scheduledefinition schedule definition
	 */
	public void setDependentDefinitionSchedule(ScheduleDefinition<E, ?> scheduledefinition) {
		this.scheduledefinition = scheduledefinition;
	}

	/**
	 * creates the property definition
	 * 
	 * @param parentobject           parent object definition
	 * @param childsessiondefinition child session definition
	 */
	public TimeslotwithsessionsDefinition(DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> childsessiondefinition) {
		super(parentobject, "TIMESLOTWITHSESSIONS");
		this.childsessiondefinition = childsessiondefinition;
	}

	/**
	 * sets the session definition on the child object
	 * 
	 * @param childrensessionpropertydefinition session definition on the child
	 *                                          object
	 */
	public void setGenericsChildrensessionsProperty(SessionDefinition<F, E> childrensessionpropertydefinition) {
		this.childrensessionpropertydefinition = childrensessionpropertydefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Timeslotwithsessions<E, F>(this, parentpayload);
	}

	/**
	 * sets dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}
}
