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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of parent object
 */
public class MultidimensionchildDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>,F extends DataObject<F>>
		extends
		DataObjectPropertyDefinition<E> {

	@SuppressWarnings("unused")
	private DataObjectDefinition<F> parentobjectforlinkdefinition;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<F> uniqueidentifiedforparentobjectforlink;
	@SuppressWarnings("unused")
	private LinkedtoparentDefinition<E, F> dependentpropertylinkedtoparentdefinition;

	public MultidimensionchildDefinition(DataObjectDefinition<E> parentobject,DataObjectDefinition<F> parentobjectforlinkdefinition) {
		super(parentobject,"MULTIDIMENSIONCHILD");
		this.parentobjectforlinkdefinition = parentobjectforlinkdefinition;
	}
	
	public void setDependentDefinitionLinkedtoparent(LinkedtoparentDefinition<E,F> dependentpropertylinkedtoparentdefinition) {
		this.dependentpropertylinkedtoparentdefinition = dependentpropertylinkedtoparentdefinition;
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

	@SuppressWarnings("rawtypes")
	@Override
	public FieldSchemaForDisplay[] setFieldSchemaToDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
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
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
			return new  Multidimensionchild<E,F>(this,parentpayload);
	}
	public void setGenericsParentobjectforlinkProperty(UniqueidentifiedDefinition<F> uniqueidentifiedforparentobjectforlink) {
		this.uniqueidentifiedforparentobjectforlink = uniqueidentifiedforparentobjectforlink;
	}
}
