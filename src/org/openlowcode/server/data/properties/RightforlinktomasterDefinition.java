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
 * Definition of a property that an object being linked as right object
 * implements. There will be one property per link going to the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the right object (current object the property is on)
 * @param <F>the link object
 * @param <G> the left object for the link
 */
public class RightforlinktomasterDefinition<
		E extends DataObject<E> & VersionedInterface<E>,
		F extends DataObject<F> & LinkobjecttomasterInterface<F, G, E>,
		G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends
		DataObjectPropertyDefinition<E> {

	@SuppressWarnings("unused")
	private DataObjectDefinition<F> linkobjectdefinition;
	private LinkobjecttomasterDefinition<F, G, E> linkobjectpropertydefinition;
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> parentobject;
	@SuppressWarnings("unused")
	private DataObjectDefinition<G> leftobjectforlinkdefinition;
	@SuppressWarnings("unused")
	private VersionedDefinition<E> versioneddefinition;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<G> uniqueidentifieddefinitionforleftobject;

	/**
	 * creates a right for link property definition for the specified link
	 * 
	 * @param parentobject                current object (being the right of the
	 *                                    link)
	 * @param name                        name of the property
	 * @param linkobjectdefinition        definition of the link object
	 * @param leftobjectforlinkdefinition definition of the right object
	 */
	public RightforlinktomasterDefinition(
			DataObjectDefinition<E> parentobject,
			String name,
			DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<G> leftobjectforlinkdefinition) {
		super(parentobject, name);
		this.linkobjectdefinition = linkobjectdefinition;
		this.parentobject = parentobject;
		this.leftobjectforlinkdefinition = leftobjectforlinkdefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {

		return new Rightforlinktomaster<E, F, G>(this, parentpayload);
	}

	/**
	 * sets the link object property definition from the link object on the property
	 * 
	 * @param linkobjectpropertydefinition link object property from the link object
	 */
	public void setGenericsLinkobjecttomasterProperty(LinkobjecttomasterDefinition<F, G, E> linkobjectpropertydefinition) {
		this.linkobjectpropertydefinition = linkobjectpropertydefinition;

	}

	/**
	 * gets the property of the link object from the link object on the property
	 * 
	 * @return the definition of the link object property on the link object
	 */
	public LinkobjecttomasterDefinition<F, G, E> getLinkObjectToMasterPropertyDefinition() {

		return this.linkobjectpropertydefinition;
	}

	/**
	 * sets the dependent definition unique identified on the object
	 * 
	 * @param uniqueidentifieddefinition unique identified definition on the object
	 */
	public void setDependentDefinitionVersioned(VersionedDefinition<E> versioneddefinition) {
		this.versioneddefinition = versioneddefinition;

	}

	/**
	 * @param uniqueidentifieddefinitionforleftobject
	 */
	public void setGenericsLeftobjectforlinktomasterProperty(
			UniqueidentifiedDefinition<G> uniqueidentifieddefinitionforleftobject) {
		this.uniqueidentifieddefinitionforleftobject = uniqueidentifieddefinitionforleftobject;

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
}
