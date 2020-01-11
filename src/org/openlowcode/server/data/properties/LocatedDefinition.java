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

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.security.LocationHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * The definition of the located property of an object. Location is uses a a
 * criteria for user and access rights
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object getting a location
 */
public class LocatedDefinition<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>>
		extends DataObjectPropertyDefinition<E> {
	private DataObjectDefinition<Domain> domaindefinition = Domain.getDefinition();
	private StringStoredField locationdomainid;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;
	@SuppressWarnings("unused")
	private StoredobjectDefinition<E> storedobjectdefinition;
	private LocationHelper<E> locationhelper;

	/**
	 * creates the definition of the located property for the object
	 * 
	 * @param parentobject   parent data object
	 * @param locationhelper helper that will define the location depending on the
	 *                       object content at creation and update
	 */
	public LocatedDefinition(DataObjectDefinition<E> parentobject, LocationHelper<E> locationhelper) {
		super(parentobject, "LOCATED");
		locationdomainid = new StringStoredField("LOCATIONDOMAINID", null, 200);
		this.addFieldSchema(locationdomainid);
		this.locationhelper = locationhelper;
	}

	/**
	 * gets the location helper allowing to determine
	 * 
	 * @return the location helper
	 */
	public LocationHelper<E> getLocationHelper() {
		return this.locationhelper;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();
		JoinQueryConditionDefinition<String> joincondition = domaindefinition.generateJoinQueryDefinition(
				domaindefinition.getTableschema(), locationdomainid, "UNIQUEIDENTIFIED", "ID", this.getName(),
				new QueryOperatorEqual<String>());

		ExternalFieldSchema<?> externalfield = domaindefinition.generateExternalField(this.getName() + "NUMBER",
				"Folder", "Indicates the field the notes has been put in", "NUMBERED", "NR", joincondition, -10, 25);
		externalfieldlist.add(externalfield);

		return externalfieldlist;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Located<E>(this, parentpayload);
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet Implemented");
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

	/**
	 * sets dependent definition unique identified
	 * 
	 * @param uniqueidentifieddefinition dependent unique identified property
	 *                                   definition
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
		this.uniqueidentifieddefinition = uniqueidentifieddefinition;
	}

	/**
	 * sets dependent definition of the stored object
	 * 
	 * @param storedobjectdefinition dependent stored object property definition
	 */
	public void setDependentDefinitionStoredobject(StoredobjectDefinition<E> storedobjectdefinition) {
		this.storedobjectdefinition = storedobjectdefinition;
	}
}
