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

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
		// ---------------------- process null ---------------------
		if (value == null) {
			if (FlatFileLoader.isTheSame(oldchoicevalue, null)) {
				return false;
			} else {
				choicefield.setValue((ChoiceValue<F>) null);
				return true;
			}
		}
		// ----------------------------- process string ----------------------------
		if (value instanceof String) {
			String stringvalue = (String) value;
			ChoiceValue<F> choicevalue = fieldchoicedefinition.parseChoiceValue(stringvalue);

			// second step: try value
			if (choicevalue == null) {
				choicevalue = fieldchoicedefinition.lookUpByDisplayValue(stringvalue);
			}
			// third step: try value after trim
			if (choicevalue == null) {
				choicevalue = fieldchoicedefinition.lookUpByDisplayValue(stringvalue.trim());

			}
			if (stringvalue.length() > 0)
				if (choicevalue == null) {
					if (!lenient)
						throw new RuntimeException("Invalid value " + value + " for field " + name + " valid values = "
								+ fieldchoicedefinition.toString());
					if (lenient)
						logger.warning("During loading, found an invalid value " + value + " for field " + name
								+ " valid values = " + fieldchoicedefinition.toString() + " for "
								+ object.dropIdToString());
				}

			if (FlatFileLoader.isTheSame(oldchoicevalue, choicevalue)) {
				return false;
			} else {
				choicefield.setValue(choicevalue);
				return true;
			}
		}
		throw new RuntimeException("For field '" + this.name + "', received an object of unsupported type = "
				+ value.getClass() + " value = " + value);

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
		cell.setCellValue((choicefield.getValue() == null ? "" : choicefield.getValue().getDisplayValue()));
		// ChoiceDataObjectFieldFlatFileLoaderColumn.setRestrictionsOnCell(cell,fieldchoicedefinition);
		return false;
	}

	/**
	 * sets restriction on cell so that only display values in the list of value, or
	 * empty string, can be entered. Warning: in current version, does not work in
	 * excel (works with Libre Office though)
	 * 
	 * @param cell             cell
	 * @param choicedefinition choice definition
	 */
	public static <E extends FieldChoiceDefinition<E>> void setRestrictionsOnCell(Cell cell, E choicedefinition) {
		ArrayList<String> restrictions = new ArrayList<String>();
		restrictions.add("");
		ChoiceValue<E>[] choices = choicedefinition.getChoiceValue();
		for (int i = 0; i < choices.length; i++)
			restrictions.add(choices[i].getDisplayValue());
		XSSFSheet sheet = (XSSFSheet) cell.getSheet();
		DataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
		DataValidationConstraint constraint = validationHelper
				.createExplicitListConstraint(restrictions.toArray(new String[0]));
		CellRangeAddressList addressList = new CellRangeAddressList(cell.getRowIndex(), cell.getRowIndex(),
				cell.getColumnIndex(), cell.getColumnIndex());
		DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
		dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
		dataValidation.setSuppressDropDownArrow(true);
		sheet.addValidationData(dataValidation);
	}

}
