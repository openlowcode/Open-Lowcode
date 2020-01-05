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
import org.openlowcode.server.data.workflowhelper.SimpletaskWorkflowHelper;

/**
 * Definition of the property creating a simple task workflow on the data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> the transition choice for the lifecycle
 */
public class SimpletaskworkflowDefinition<E extends DataObject<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectPropertyDefinition<E> {
	@SuppressWarnings("unused")
	private LifecycleDefinition<E, F> lifecycledefinition;
	private F transitionfieldchoiceedefinition;
	private SimpletaskWorkflowHelper<E> workflowhelper;

	/**
	 * creates a new simple task workflow definition
	 * 
	 * @param parentobject                    parent data object
	 * @param transitionfieldchoicedefinition transition choice for the lifecycle
	 * @param workflowhelper                  workflow helper
	 */
	public SimpletaskworkflowDefinition(DataObjectDefinition<E> parentobject, F transitionfieldchoicedefinition,
			SimpletaskWorkflowHelper<E> workflowhelper) {
		super(parentobject, "SIMPLETASKWORKFLOW");
		this.transitionfieldchoiceedefinition = transitionfieldchoicedefinition;
		this.workflowhelper = workflowhelper;
	}

	/**
	 * gets the workflow helper
	 * 
	 * @return workflow helper
	 */
	public SimpletaskWorkflowHelper<E> getWorkflowHelper() {
		return this.workflowhelper;
	}

	/**
	 * sets the definition of the dependent property lifecycle
	 * 
	 * @param lifecycledefinition definition of the lifecycle property
	 */
	public void setDependentDefinitionLifecycle(LifecycleDefinition<E, F> lifecycledefinition) {
		this.lifecycledefinition = lifecycledefinition;
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
	public FieldSchemaForDisplay<?>[] setFieldSchemaToDisplay() {
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
		return new Simpletaskworkflow<E, F>(this, parentpayload, transitionfieldchoiceedefinition);
	}

}
