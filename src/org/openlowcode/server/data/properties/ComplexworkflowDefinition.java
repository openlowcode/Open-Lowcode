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
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.workflowhelper.ComplexWorkflowHelper;

/**
 * Definition for a complex workflow that allows several tasks with routing
 * options
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition field choice definition used by the lifecycle
 */
public class ComplexworkflowDefinition<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectPropertyDefinition<E> {
	/**
	 * defines the complex workflow for this object (one complex workflow allowed
	 * per object on current version)
	 * 
	 * @param parentobject              parent data object definition
	 * @param lifecyclechoicedefinition transition choice definition used by the
	 *                                  lifecycle
	 * @param workflowhelper            complex workflow helper
	 */
	public ComplexworkflowDefinition(DataObjectDefinition<E> parentobject, F lifecyclechoicedefinition,
			ComplexWorkflowHelper<E, F> workflowhelper) {
		super(parentobject, "COMPLEXWORKFLOW");
		this.workflowhelper = workflowhelper;
	}

	/**
	 * gets the complex workflow helper (defining the workflow logic)
	 * 
	 * @return complex workflow helper
	 */
	public ComplexWorkflowHelper<E, F> getWorkflowHelper() {
		return this.workflowhelper;
	}

	private ComplexWorkflowHelper<E, F> workflowhelper;

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
		throw new RuntimeException("Not yet implemented");
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
		return new Complexworkflow<E, F>(this, parentpayload);
	}
}
