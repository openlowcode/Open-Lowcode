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
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.typed.TypedHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public class TypedDefinition<E extends DataObject<E> & TypedInterface<E, F>, F extends FieldChoiceDefinition<F>>
		extends
		DataObjectPropertyDefinition<E> {
	private StringStoredField type;
	private F typechoice;
	private UniqueidentifiedDefinition<E> uniqueidentified;
	private TypedHelper<E, F> helper;

	public TypedHelper<E,F> getHelper() {
		return this.helper;
	}
	
	public TypedDefinition(DataObjectDefinition<E> parentobject, F typechoice) {
		super(parentobject, "TYPED");
		this.typechoice = typechoice;
		this.type = new StringStoredField("TYPE", null, typechoice.getStorageSize());
		this.addFieldSchema(type);
		StoredTableIndex typeindex = new StoredTableIndex("TYPE");
		typeindex.addStoredFieldSchema(type);
		this.addIndex(typeindex);
		helper = new TypedHelper<E,F>();
	}

	public UniqueidentifiedDefinition<E> getDependentUniqueIdentified() {
		return this.uniqueidentified;
	}

	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	public F getTypeChoice() {
		return this.typechoice;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		// TODO Auto-generated method stub
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Typed<E, F>(this, parentpayload);
	}

}
