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
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * Definition of a field storing encrypted text in the database. Text can be
 * encrypted one way (passwords) or two ways (critical information that needs to
 * be decoded, not just checked for equality)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class EncryptedStringDataObjectFieldDefinition<E extends DataObject<E>>
		extends
		DataObjectFieldDefinition<E> {

	private int length;

	private int encryptiontype;

	/**
	 * no encryption
	 */
	public static int ENCRYPTION_NONE = 0;
	/**
	 * oneway encryption. Can never be decoded, but a string can be encoded again to
	 * be compared against the encoded value
	 */
	public static int ENCRYPTION_ONEWAY = 1;
	/**
	 * two ways encryption
	 */
	public static int ENCRYPTION_TWOWAYS = 2;

	/**
	 * @return the enctyption type for this field
	 */
	public int getEncryptiontype() {
		return encryptiontype;
	}

	/**
	 * @return the length of the field
	 */
	public int getLength() {
		return length;
	}

	/**
	 * create the definition of an encryption field
	 * 
	 * @param name           unique name of the field in the object
	 * @param displayname    label in main language
	 * @param tooltip        explanation for roll-over tip
	 * @param length         length of the field
	 * @param encryptiontype encryption type as one of the static integers in this
	 *                       class
	 * @param definition     definition of the data object on which the field is
	 *                       added
	 */
	public EncryptedStringDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int length,
			int encryptiontype,
			DataObjectDefinition<E> definition) {
		super(name, displayname, tooltip, false, definition);
		boolean encryptiontypesupported = false;
		if (encryptiontype == ENCRYPTION_NONE)
			encryptiontypesupported = true;
		if (encryptiontype == ENCRYPTION_ONEWAY)
			encryptiontypesupported = true;
		if (encryptiontype == ENCRYPTION_TWOWAYS)
			encryptiontypesupported = true;
		if (encryptiontypesupported == false)
			throw new RuntimeException("Encryption type not supported");
		this.length = length;
		this.encryptiontype = encryptiontype;

		this.addFieldSchema(new StringStoredField(this.getName(), null, length));
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		STextField textfield = new STextField(this.getDisplayname(), this.getName(), this.getTooltip(), this.length,
				null, true, null, this.isReadOnly(), this.isShowintitle(), this.isShowinbottonpage(), null);
		textfield.hideDisplay();
		textfield.setEncryptionMode(this.encryptiontype);
		return textfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new EncryptedStringDataObjectField<E>(this, parentpayload);
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return new EncryptedStringDataObjectFieldFlatFileLoaderColumn<E>(objectdefinition, columnattributes,
				this.getName(), length);
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "textinclear";
		returntable[3] = "no additional parameter for this type";
		return returntable;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {
		throw new RuntimeException("Not yet implemented");
	}
}
