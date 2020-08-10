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

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * A loader for a ChoiceDataObjectField. It allows to load either the code or
 * the display value of the choice field.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 * @param <F> the FieldChoiceDefinition
 * 
 */
public class ChoiceDataObjectFieldFlatFileLoaderColumn<E extends DataObject<E>, F extends FieldChoiceDefinition<F>>
		extends
		FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(ChoiceDataObjectFieldFlatFileLoaderColumn.class.getName());
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> objectdefinition;
	private String name;
	private F fieldchoicedefinition;
	private boolean lenient;

	/**
	 * creates a ChoiceField flatfile loader
	 * 
	 * @param objectdefinition      definition of the object
	 * @param columnattributes      not used, there is no attribute
	 * @param name                  name of the field
	 * @param fieldchoicedefinition definition of the choice (list of values)
	 * @param lenient               if true, will not blow-up the line with an
	 *                              exception if the value is wrong
	 */
	public ChoiceDataObjectFieldFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			String name,
			F fieldchoicedefinition,
			boolean lenient) {
		this.objectdefinition = objectdefinition;
		this.name = name;
		this.fieldchoicedefinition = fieldchoicedefinition;
		this.lenient = lenient;

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		DataObjectField<?, E> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof ChoiceDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type ChoiceDataObjectField but in reality, it is " + field.getClass().toString());
		// first step : try code
		ChoiceDataObjectField<F, E> choicefield = (ChoiceDataObjectField<F, E>) field;
		ChoiceValue<F> oldchoicevalue = choicefield.getValue();
		
		ChoiceValue<F> newchoicevalue = ChoiceDataObjectFieldFlatFileLoaderColumn.getContentFromCell(value, fieldchoicedefinition, null, lenient, 
				" Field "+name+" for object "+object.dropIdToString());
		
		
			if (FlatFileLoader.isTheSame(oldchoicevalue, newchoicevalue)) {
				return false;
			} else {
				choicefield.setValue(newchoicevalue);
				return true;
			}
		
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {

		DataObjectField<?, E> field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof ChoiceDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type ChoiceDataObjectField but in reality, it is " + field.getClass().toString());
		ChoiceDataObjectField<F, E> choicefield = (ChoiceDataObjectField<F, E>) field;
		putContentInCell(cell, choicefield.getValue());
		return false;
	}

	@Override
	public String[] getValueRestriction() {
		ArrayList<String> allowedvalues = new ArrayList<String>();
		allowedvalues.add("");
		ChoiceValue<?>[] choices = fieldchoicedefinition.getChoiceValue();
		for (int i = 0; i < choices.length; i++)
			allowedvalues.add(choices[i].getDisplayValue());
		return allowedvalues.toArray(new String[0]);
	}

	/**
	 * A helper to parse value from a cell
	 * 
	 * @param value object in the cell
	 * @param choicedefinition definition of the choice value
	 * @param defaultvalue default value to put if cell does not have significant text
	 * @param lenient if true, does not blow an exception, if false, blows an exception if illegal content
	 * @param contextforerror the context to print in exceptions
	 * @return the value
	 */
	public static <F extends FieldChoiceDefinition<F>> ChoiceValue<F> getContentFromCell(
			Object value,
			FieldChoiceDefinition<F> choicedefinition,
			ChoiceValue<F> defaultvalue,
			boolean lenient,
			String contextforerror) {
		if (value == null)
			return defaultvalue;
		if (value instanceof String) {
			String stringvalue = (String) value;
			if (stringvalue.trim().length() == 0)
				return defaultvalue;
			ChoiceValue<F> choicevalue = choicedefinition.parseChoiceValue(stringvalue);

			// second step: try value
			if (choicevalue == null) {
				choicevalue = choicedefinition.lookUpByDisplayValue(stringvalue);
			}
			// third step: try value after trim
			if (choicevalue == null) {
				choicevalue = choicedefinition.lookUpByDisplayValue(stringvalue.trim());

			}

			if (choicevalue == null) {
				if (!lenient)
					throw new RuntimeException("Invalid value " + value + "  valid values = "
							+ choicedefinition.toString() + " for " + contextforerror);
				if (lenient)
					logger.warning("During loading, found an invalid value " + value + "  valid values = "
							+ choicedefinition.toString() + " for " + contextforerror);
			}

			return choicevalue;
		}
		throw new RuntimeException("For " + contextforerror + ", received an object of unsupported type = "
				+ value.getClass() + " value = " + value);

	}

	/**
	 * Fills a spreadsheet cell with a choice value
	 * 
	 * @param cell cell to fill
	 * @param value value
	 */

	public static <F extends FieldChoiceDefinition<F>> void putContentInCell(Cell cell, ChoiceValue<F> value) {
		cell.setCellValue((value == null ? "" : value.getDisplayValue()));
	}

}
