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
import org.openlowcode.server.data.storage.StringStoredField;


/**
 * Definition of a property to use the data object as a schedule dependency. It
 * should be an auto-link to an object implementing the timeslot and schedule
 * properties
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object used as schedule dependency
 * @param <F> the data object used as timeslot and schedule
 */
public class ScheduledependencyDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F> & ScheduledependencyInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F> & TimeslotInterface<F> & ScheduleInterface<F, E>>
		extends DataObjectPropertyDefinition<E> {

	private DataObjectDefinition<F> scheduleobjectdefinition;
	private AutolinkobjectDefinition<E, F> autolinkobjectdefinition;
	@SuppressWarnings("unused")
	private ScheduleDefinition<F, E> genericsscheduledefinition;
	private StringStoredField split;

	public ScheduledependencyDefinition(DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> scheduleobjectdefinition) {
		super(parentobject, "SCHEDULEDEPENDENCY");
		this.scheduleobjectdefinition = scheduleobjectdefinition;
		split = new StringStoredField("SPLIT", null, 8);
		this.addFieldSchema(split);
	}

	public AutolinkobjectDefinition<E, F> getAutoLinkObjectDefinition() {
		return this.autolinkobjectdefinition;
	}

	public void setDependentDefinitionAutolinkobject(AutolinkobjectDefinition<E, F> autolinkobjectdefinition) {
		this.autolinkobjectdefinition = autolinkobjectdefinition;
	}

	public void setGenericsScheduleProperty(ScheduleDefinition<F, E> genericsscheduledefinition)
			 {
		this.genericsscheduledefinition = genericsscheduledefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema()  {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias)  {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale)  {

		return null;
	}

	@Override
	public String[] getLoaderFieldList() {

		return null;
	}

	@Override
	public String[] getLoaderFieldSample(String name) {

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload)  {
		return new Scheduledependency<E, F>(this, parentpayload, scheduleobjectdefinition);
	}

}
