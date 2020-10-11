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
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;


/**
 * definition of the Uniqueidentified property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class UniqueidentifiedDefinition<E extends DataObject<E>>
		extends
		DataObjectPropertyDefinition<E> {

	private StoredobjectDefinition<E> dependentdefinitionstoredobject;
	private HasidDefinition<E> dependentdefinitionhasid;

	/**
	 * creates a new definition of a Uniqueidentified property for a class of data
	 * object
	 * 
	 * @param parentobject parent object definition for the property
	 */
	public UniqueidentifiedDefinition(DataObjectDefinition<E> parentobject) {
		super(parentobject, "UNIQUEIDENTIFIED");

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Uniqueidentified<E>(this, parentpayload);
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[0];
		return returnvalue;
	}

	/**
	 * @param dependentdefinitionstoredobject dependent storedobject property for
	 *                                        the object definition
	 */
	public void setDependentDefinitionStoredobject(StoredobjectDefinition<E> dependentdefinitionstoredobject) {
		this.dependentdefinitionstoredobject = dependentdefinitionstoredobject;

	}

	/**
	 * @return the dependent storedobject property for the object definition
	 */
	public StoredobjectDefinition<E> getDependentDefinitionStoredobject() {
		return this.dependentdefinitionstoredobject;
	}

	/**
	 * @param dependentdefinitionhasid dependent hasid property for the object
	 *                                 definition
	 * @since 2.0
	 */
	public void setDependentDefinitionHasid(HasidDefinition<E> dependentdefinitionhasid) {
		this.dependentdefinitionhasid = dependentdefinitionhasid;

	}

	/**
	 * @return the dependent hasid property for the object definition
	 * @since 2.0
	 */
	public HasidDefinition<E> getDependentDefinitionHasid() {
		return this.dependentdefinitionhasid;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("No loader for this property");
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

}
