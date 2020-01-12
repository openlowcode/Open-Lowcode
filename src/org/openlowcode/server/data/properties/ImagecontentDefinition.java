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
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * definition of a data object with an image content will hold a main image.
 * Both a thumbnail and a full image are stored for the object. By default,the
 * thumbnail is shown, and when clicking on it, the full image is shown
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object of the image content property
 */
public class ImagecontentDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends DataObjectPropertyDefinition<E> {
	private StringStoredField imgid;
	private StringStoredField thbid;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentified;

	/**
	 * defines an image content for a data object
	 * 
	 * @param parentobject parent data object definition
	 * @param name         name of the image (there can be several images with
	 *                     different names on the same data object definition
	 */
	public ImagecontentDefinition(DataObjectDefinition<E> parentobject, String name) {
		super(parentobject, name);
		imgid = new StringStoredField(this.getName() + "IMGID", null, 200);
		this.addFieldSchema(imgid);
		thbid = new StringStoredField(this.getName() + "THBID", null, 200);
		this.addFieldSchema(thbid);

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
		return new Imagecontent<E>(this, parentpayload);
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

}
