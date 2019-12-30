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
import org.openlowcode.server.data.properties.trigger.CustomTriggerExecutionFactory;
import org.openlowcode.server.data.properties.trigger.TriggerCondition;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of an object that has a trigger. The trigger will be kicked at a
 * specific condition (typically insert, update, or set state)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 */
public class TriggerDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends DataObjectPropertyDefinition<E> {
	private TriggerCondition<E> triggercondition;
	private CustomTriggerExecutionFactory<E> triggerexecution;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;

	/**
	 * gets the condition for trigger kick
	 * 
	 * @return the condition
	 */
	public TriggerCondition<E> getTriggerCondition() {
		return this.triggercondition;
	}

	/**
	 * gets the trigger execution factory
	 * 
	 * @return the execution factory
	 */
	public CustomTriggerExecutionFactory<E> getTriggerExecution() {
		return this.triggerexecution;
	}

	/**
	 * creates the trigger definition
	 * 
	 * @param parentobject     object
	 * @param triggername      name (object can have several triggers of different
	 *                         names)
	 * @param triggercondition condition to kick the trigger
	 * @param triggerexecution content of the trigger
	 */
	public TriggerDefinition(DataObjectDefinition<E> parentobject, String triggername,
			TriggerCondition<E> triggercondition, CustomTriggerExecutionFactory<E> triggerexecution) {
		super(parentobject, triggername);
		this.triggercondition = triggercondition;
		this.triggerexecution = triggerexecution;
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
		return new Trigger<E>(this, parentpayload);
	}

	/**
	 * sets the dependent property definition unique-identified
	 * 
	 * @param uniqueidentifieddefinition the definition of unique-identified
	 *                                   property for the object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
		this.uniqueidentifieddefinition = uniqueidentifieddefinition;

	}
}
