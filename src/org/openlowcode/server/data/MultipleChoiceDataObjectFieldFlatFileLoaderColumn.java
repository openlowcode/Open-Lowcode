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

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.tools.misc.MultiStringEncoding;

/**
 * flat file loader for the multiple choice data object field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of the data object the field is on
 */
public class MultipleChoiceDataObjectFieldFlatFileLoaderColumn<E extends DataObject<E>>
		extends
		FlatFileLoaderColumn<E> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ChoiceDataObjectFieldFlatFileLoaderColumn.class.getName());
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> objectdefinition;
	private String name;
	@SuppressWarnings("unused")
	private FieldChoiceDefinition<?> fieldchoicedefinition;
	@SuppressWarnings("unused")
	private boolean lenient;

	/**
	 * creates the flat file loader for multiple choice field
	 * 
	 * @param objectdefinition      definition of the parent data object
	 * @param columnattributes      attributes of the loader
	 * @param name                  unique name of the field
	 * @param fieldchoicedefinition definition of the choice
	 * @param lenient               if true, does not blow up if there is an error
	 */
	public MultipleChoiceDataObjectFieldFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			String name,
			FieldChoiceDefinition<?> fieldchoicedefinition,
			boolean lenient) {
		this.objectdefinition = objectdefinition;
		this.name = name;
		this.fieldchoicedefinition = fieldchoicedefinition;
		this.lenient = lenient;

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		@SuppressWarnings("unchecked")
		DataObjectField<?, E> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof MultipleChoiceDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type ChoiceDataObjectField but in reality, it is " + field.getClass().toString());
		// first step : try code
		@SuppressWarnings("rawtypes")
		MultipleChoiceDataObjectField choicefield = (MultipleChoiceDataObjectField) field;
		String oldstoragestring = choicefield.getStorageString();
		// ---------------------- process null ---------------------
		if (value == null) {
			if (FlatFileLoader.isTheSame(oldstoragestring, null)) {
				return false;
			} else {
				choicefield.reset();
				return true;
			}
		}
		// ----------------------------- process string ----------------------------
		if (value instanceof String) {
			String stringvalue = (String) value;
			String[] storagecodearray = stringvalue.split("\\|");
			List<String> storagecodearraylist = Arrays.asList(storagecodearray);
			Collections.sort(storagecodearraylist);
			String orderedstringvalue = MultiStringEncoding.encode(storagecodearraylist);
			if (FlatFileLoader.isTheSame(oldstoragestring, orderedstringvalue)) {
				return false;
			} else {
				choicefield.loadNewStorageString(orderedstringvalue);
				return true;
			}
		}
		throw new RuntimeException("For field '" + this.name + "', received an object of unsupported type = "
				+ value.getClass() + " value = " + value);
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		@SuppressWarnings("unchecked")
		DataObjectField<?, E> field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof MultipleChoiceDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type ChoiceDataObjectField but in reality, it is " + field.getClass().toString());
		@SuppressWarnings("unchecked")
		MultipleChoiceDataObjectField<?, E> choicefield = (MultipleChoiceDataObjectField<?, E>) field;
		cell.setCellValue(choicefield.getStorageString());
		return false;
	}
}
