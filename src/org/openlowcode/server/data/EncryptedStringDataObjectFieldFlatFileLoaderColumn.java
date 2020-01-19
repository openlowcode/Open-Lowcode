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

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * a loader to load an encrypted field information. The information should be in
 * clear in the loading file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object for loading
 */
public class EncryptedStringDataObjectFieldFlatFileLoaderColumn<E extends DataObject<E>>
		extends
		FlatFileLoaderColumn<E> {
	private int maxlength;
	private String name;

	/**
	 * Creates a flat-file loader for the encrypted field
	 * 
	 * @param definition definition of the data object
	 * @param arguments argments for loading
	 * @param name name of the field
	 * @param maxlength maximum length of the field
	 */
	public EncryptedStringDataObjectFieldFlatFileLoaderColumn(
			DataObjectDefinition<E> definition,
			String[] arguments,
			String name,
			int maxlength) {
		this.maxlength = maxlength;
		this.name = name;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		DataObjectField<?,?> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException( "field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof EncryptedStringDataObjectField))
			throw new RuntimeException(
					"Expected field " + name + " would be of type EncryptedStringDataObjectField but in reality, it is "
							+ field.getClass().toString());
		if (value instanceof String) {
			String stringvalue = (String) value;
			@SuppressWarnings("unchecked")
			EncryptedStringDataObjectField<E> encryptedstringfield = (EncryptedStringDataObjectField<E>) field;
			if (stringvalue.length() > maxlength)
				throw new RuntimeException(
						"data is too long for field " + name + " on object " + object.getName());
			encryptedstringfield.setValue(stringvalue);
			return true;
		}
		if (value == null)
			return false;
		throw new RuntimeException( "For field '" + this.name + "', received an object of unsupported type = "
				+ value.getClass() + " value = " + value);
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context)  {
		cell.setCellValue("Encrypted Content");
		return false;
	}

}
