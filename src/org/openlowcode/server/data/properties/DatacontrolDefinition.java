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
import org.openlowcode.server.data.properties.constraints.DataControlHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * definition of a data control property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class DatacontrolDefinition<E extends DataObject<E> & LifecycleInterface<E, ?>>
		extends DataObjectPropertyDefinition<E> {
	private DataControlHelper<E> datacontrolhelper;
	@SuppressWarnings("unused")
	private LifecycleDefinition<E, ?> lifecycledefinition;
	private StringStoredField summary;

	/**
	 * defintion of the data control property. The helper holds the business logic
	 * and actual controls
	 * 
	 * @param parentobject      parent data object
	 * @param datacontrolhelper helper for the data control (holds the logic)
	 */
	public DatacontrolDefinition(DataObjectDefinition<E> parentobject, DataControlHelper<E> datacontrolhelper) {
		super(parentobject, "DATACONTROL");
		this.datacontrolhelper = datacontrolhelper;
		this.summary = new StringStoredField("SUMMARY", null, 200, "");
		this.addFieldSchema(this.summary);
	}

	/**
	 * gets the data control helepr
	 * 
	 * @return the helper
	 */
	public DataControlHelper<E> getDataControlHelper() {
		return datacontrolhelper;
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
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[1];

		returnvalue[0] = new FieldSchemaForDisplay<E>("Control  summary", "summary of latest data control performed",
				this.summary, false, false, true, -50, 20, this.parentobject);
		return returnvalue;
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
		return new Datacontrol<E>(this, parentpayload);
	}

	/**
	 * sets the dependent property lifecycle definition
	 * 
	 * @param lifecycledefinition dependent property lifecycle definition
	 */
	public void setDependentDefinitionLifecycle(LifecycleDefinition<E, ?> lifecycledefinition) {
		this.lifecycledefinition = lifecycledefinition;
	}
}
