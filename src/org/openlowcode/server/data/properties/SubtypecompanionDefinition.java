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
import org.openlowcode.server.data.DataObjectElementDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.SimpleFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;


/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 * @param <F>
 * @param <G>
 */
public class SubtypecompanionDefinition
<E extends DataObject<E> & StoredobjectInterface<E>,
F extends DataObject<F> & SubtypeInterface<F, G>,
G extends SimpleFieldChoiceDefinition<G>> extends DataObjectPropertyDefinition<E> {

	private StoredobjectDefinition<E> storedobject;
	private UniqueidentifiedDefinition<F> uniqueidentifiedformainboject;
	private DataObjectDefinition<F> mainobjetforsubtype;
	private SimpleFieldChoiceDefinition<G> subtypeschoicedefinition;

	public SubtypecompanionDefinition(DataObjectDefinition<E> parentobject, String name) {
		super(parentobject, name);

	}

	public SubtypecompanionDefinition(
			DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> mainobjectforsubtype,
			SimpleFieldChoiceDefinition<G> subtypeschoicedefinition) {
		super(parentobject,"SUBTYPECOMPANION");
		this.mainobjetforsubtype = mainobjectforsubtype;
		this.subtypeschoicedefinition = subtypeschoicedefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldSchemaForDisplay[] setFieldSchemaToDisplay() {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataObjectElement<DataObjectElementDefinition<?, E>, E> initiateFieldInstance(
			DataObjectPayload parentpayload) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDependentDefinitionStoredobject(StoredobjectDefinition<E> storedobject) {
		this.storedobject = storedobject;
		
	}

	public void setGenericsMainobjectProperty(UniqueidentifiedDefinition<F> uniqueidentifiedformainboject) {
		this.uniqueidentifiedformainboject = uniqueidentifiedformainboject;
		
	}

}
