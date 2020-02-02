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
 * Definition of the property Personal allowing a privilege to be granted to
 * users directly linked to the data obejct
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the property applies to
 */
public class PersonalDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		DataObjectPropertyDefinition<E> {

	public PersonalDefinition(DataObjectDefinition<E> parentobject, String name) {
		super(parentobject, name);
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		
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
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale)  {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload)  {
		return new Personal(this, parentpayload);
	}

}
