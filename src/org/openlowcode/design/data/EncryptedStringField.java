/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * Creates a string field that is encrypted either one way or two ways
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class EncryptedStringField
		extends
		Field {
	private int length;
	private int encryptiontype;
	/**
	 * no encryption
	 */
	public static int ENCRYPTION_NONE = 0;
	/**
	 * encryption one way. The string cannot be decoded but can be tested for
	 * equality
	 */
	public static int ENCRYPTION_ONEWAY = 1;
	/**
	 * encryption two ways. The string can be decoded
	 */
	public static int ENCRYPTION_TWOWAYS = 2;

	/**
	 * creates an encrypted string field
	 * 
	 * @param name           unique name on the data object (should be a valid java
	 *                       and sql field name
	 * @param displayname    name in the default language for display in the GUI
	 * @param tooltip        mouse roll-over tooltip. Can be longer
	 * @param length         length of the string
	 * @param encryptiontype type of encryption (declared as a static int in this
	 *                       class)
	 */
	public EncryptedStringField(String name, String displayname, String tooltip, int length, int encryptiontype) {
		super(name, displayname, tooltip);
		this.length = length;
		this.encryptiontype = encryptiontype;
		boolean encryptiontypesupported = false;
		if (encryptiontype == ENCRYPTION_NONE)
			encryptiontypesupported = true;
		if (encryptiontype == ENCRYPTION_ONEWAY)
			encryptiontypesupported = true;
		if (encryptiontype == ENCRYPTION_TWOWAYS)
			encryptiontypesupported = true;
		if (encryptiontypesupported == false)
			throw new RuntimeException("Encryption type not supported");
		StoredElement plainfield = new StringStoredElement("", length);
		this.addElement(plainfield);

	}

	@Override
	public String getDataObjectFieldName() {
		return "EncryptedStringDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {

		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.length + "," + this.encryptiontype;
	}

	@Override
	public String getJavaType() {

		return "String";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		throw new RuntimeException("Composite index not supported for encrypted field.");
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		throw new RuntimeException("Not yet implemented");
	}

}
