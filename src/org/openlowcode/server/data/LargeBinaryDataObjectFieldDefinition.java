/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.graphic.SPageNode;

/**
 * Definition of a data object field of a data object where payload is a binary
 * content (typically a file). Files can be big. It is recommended to use data
 * objects with binary content only to store binary, as accessing data objects
 * with binary content to perform processing not involving those binary files is
 * likely to be sub-optimal
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class LargeBinaryDataObjectFieldDefinition<E extends DataObject<E>>
		extends
		DataObjectFieldDefinition<E> {
	private int maxlength;

	/**
	 * Defines a large binary field in a data object
	 * 
	 * @param name             unique name for the field in the data object
	 * @param displayname      label of the field in default language
	 * @param tooltip          tooltip shown with mouse roll-over
	 * @param objectdefinition parent data object definition
	 */
	public LargeBinaryDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, 0, objectdefinition);

	}

	/**
	 * Defines a large binary field in a data object with specified maximum size for
	 * the binary
	 * 
	 * @param name             unique name for the field in the data object
	 * @param displayname      label of the field in default language
	 * @param tooltip          tooltip shown with mouse roll-over
	 * @param maxlength        maximum length of the binary field
	 * @param objectdefinition parent data object definition
	 */
	public LargeBinaryDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int maxlength,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, false, objectdefinition);
		this.maxlength = maxlength;
		if (this.maxlength == 0)
			this.addFieldSchema(new LargeBinaryStoredField(this.getName(), null));
		if (this.maxlength > 0)
			this.addFieldSchema(new LargeBinaryStoredField(this.getName(), null, maxlength));
	}

	@Override
	public SPageNode getDataFieldDefinition() {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new LargeBinaryDataObjectField<E>(this, parentpayload);
	}

	@Override
	public String[] getLoaderFieldSample() {
		return null;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {
		throw new RuntimeException("Not yet implemented");
	}
}
