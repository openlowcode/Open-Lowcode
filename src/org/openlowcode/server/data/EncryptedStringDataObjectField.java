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

import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.tools.enc.OLcEncrypter;
import org.openlowcode.tools.structure.EncryptedTextDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * A field storing encrypted text in the database. Text can be encrypted one way
 * (passwords) or two ways (critical information that needs to be decoded, not
 * just checked for equality)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the field is on
 */
public class EncryptedStringDataObjectField<E extends DataObject<E>>
		extends
		DataObjectField<EncryptedStringDataObjectFieldDefinition<E>, E> {
	/**
	 * @return true if field data is encrypted one way
	 */
	public boolean isEncryptedOneWay() {
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_ONEWAY)
			return true;
		return false;
	}

	protected StoredField<String> content;
	private int encryptiontype;
	private int length;

	/**
	 * creates an encrypted data object field
	 * 
	 * @param definition    definition of the field
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public EncryptedStringDataObjectField(
			EncryptedStringDataObjectFieldDefinition<E> definition,
			DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		content = (StoredField<String>) this.field.get(0);
		this.encryptiontype = definition.getEncryptiontype();
		this.length = definition.getLength();

	}

	@Override
	public SimpleDataElt getDataElement() {
		// one way encrypted fields do not make any sense to be exported, so they are
		// shown empty
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_ONEWAY)
			return new EncryptedTextDataElt(this.getName(), null);
		return new EncryptedTextDataElt(this.getName(), this.getValue());
	}

	/**
	 * sets the value of the field after appropriate encryption
	 * 
	 * @param value the clear value (not encrypted)
	 */
	public void setValue(String value) {
		if (value.length() > this.length)
			throw new RuntimeException(String.format(
					"field %s : incorrect length of string provided, expected %d char, got '%s' (%d char)",
					this.definition.getName(), this.length, value, value.length()));
		OLcEncrypter encrypter = OLcEncrypter.getEncrypter();
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_NONE)
			content.setPayload(value);
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_ONEWAY)
			content.setPayload(encrypter.encryptStringOneWay(value));


	}

	/**
	 * gets the value of the field
	 * 
	 * @return gets the value of the field (decrypted if two ways, encrypted if one
	 *         way)
	 */
	public String getValue() {
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_NONE)
			return content.getPayload();
		// returns empty string as returning one way encrypted data does not make any
		// sense
		if (this.encryptiontype == EncryptedStringDataObjectFieldDefinition.ENCRYPTION_ONEWAY)
			return content.getPayload();


		return "#ERROR#";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[EncryptedStringDataObjectField:" + this.getName() + "," + this.getFieldNumber() + ";ENC"
				+ this.encryptiontype + "]";
	}

}
