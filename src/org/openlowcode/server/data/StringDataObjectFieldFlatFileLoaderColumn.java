/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * the loader for a String data object field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object field
 */
public class StringDataObjectFieldFlatFileLoaderColumn<E extends DataObject<E>>
		extends
		FlatFileLoaderColumn<E> {
	private int maxlength;
	private boolean richtext;
	private String name;
	private boolean truncate;

	/**
	 * Creates a loader for string data object field
	 * 
	 * @param definition object definition
	 * @param arguments  arguments from the input loading field
	 * @param name       name of the field
	 * @param maxlength  maximum length of the field
	 * @param richtext   if true, text is loaded as rich text
	 * @param truncate   if true, text will be truncated if too long with 3 dots at
	 *                   the end, if false, text too long will generate an error and
	 *                   the line will not be loaded
	 */
	public StringDataObjectFieldFlatFileLoaderColumn(
			DataObjectDefinition<E> definition,
			String[] arguments,
			String name,
			int maxlength,
			boolean richtext,
			boolean truncate) {
		this.maxlength = maxlength;
		this.richtext = richtext;
		this.name = name;
		this.truncate = truncate;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		@SuppressWarnings("rawtypes")
		DataObjectField field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof StringDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type StringDataObjectField but in reality, it is " + field.getClass().toString());
		@SuppressWarnings("rawtypes")
		StringDataObjectField stringfield = (StringDataObjectField) object.payload.lookupSimpleFieldOnName(name);
		String oldvalue = stringfield.getValue();
		String toinsert = FlatFileLoader.parseObject(value, "field '" + this.name + "'");

		if (!truncate)
			if (toinsert.length() > maxlength)
				throw new RuntimeException(
						"data is too long for field " + name + " on object " + object.getName() + ": " + toinsert);
		if (truncate)
			if (toinsert.length() > maxlength) {
				toinsert = toinsert.substring(0, maxlength - 3) + "...";
			}
		if (richtext) {
			// TODO - add here check on richtextvalidity
		}
		if (FlatFileLoader.isTheSame(oldvalue, toinsert)) {
			return false;
		} else {
			stringfield.setValue(toinsert);
			return true;
		}
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		@SuppressWarnings("rawtypes")
		DataObjectField field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof StringDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type StringDataObjectField but in reality, it is " + field.getClass().toString());
		@SuppressWarnings("rawtypes")
		StringDataObjectField stringfield = (StringDataObjectField) currentobject.payload.lookupSimpleFieldOnName(name);
		cell.setCellValue(stringfield.getValue());
		return false;
	}

}
