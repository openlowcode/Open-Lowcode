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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class DecimalDataObjectFieldFlatFileLoaderColumn<E extends DataObject<E>>
		extends
		FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(DecimalDataObjectFieldFlatFileLoaderColumn.class.getName());
	private String name;
	private int scale;
	private int precision;
	@SuppressWarnings("unused")
	private ChoiceValue<ApplocaleChoiceDefinition> locale;
	public BigDecimal multiplyatexport = new BigDecimal(1);

	public static class DecimalParser {

		private int scale;
		private int precision;
		private ChoiceValue<ApplocaleChoiceDefinition> locale;
		private DecimalFormat decimalformat;
		private int specialtreatment;
		public static int SPECIAL_TREATMENT_NONE = 0;
		public static int SPECIAL_TREATMENT_MULTIPLY_BY_1000 = 1;
		public static int SPECIAL_TREATMENT_DIVIDE_BY_1000 = 2;
		public static int SPECIAL_TREATMENT_MULTIPLY_BY_100 = 3;

		public int getSpecialTreatment() {
			return this.specialtreatment;
		}

		/**
		 * creates a decimal parser
		 * 
		 * @param scale     total number of digits of the number (e.g. 533.33 is 5
		 *                  digits)
		 * @param precision (digits to the right of decimal point (e.g. 533.33 has
		 *                  precision on 2)
		 * @param locale    locale if CSV import
		 */
		public DecimalParser(
				int scale,
				int precision,
				ChoiceValue<ApplocaleChoiceDefinition> locale,
				int specialtreatment) {
			this.scale = scale;
			this.precision = precision;
			this.locale = locale;
			decimalformat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
			if (this.locale != null)
				if (this.locale.getStorageCode().equals(ApplocaleChoiceDefinition.get().FR))
					;
			decimalformat = (DecimalFormat) NumberFormat.getInstance(Locale.FRENCH);
			decimalformat.setParseBigDecimal(true);
			this.specialtreatment = specialtreatment;
		}

		public DecimalParser(int scale, int precision, ChoiceValue<ApplocaleChoiceDefinition> locale) {
			this(scale, precision, locale, SPECIAL_TREATMENT_NONE);
		}

		public BigDecimal parse(String value) {

			try {
				BigDecimal decimal = null;
				if (value != null)
					if (value.length() > 0) {
						Number number = decimalformat.parse(value);
						decimal = (BigDecimal) number;
						if (specialtreatment == SPECIAL_TREATMENT_MULTIPLY_BY_1000)
							decimal = decimal.multiply(new BigDecimal(1000));
						if (specialtreatment == SPECIAL_TREATMENT_DIVIDE_BY_1000)
							decimal = decimal.divide(new BigDecimal(1000));
						if (specialtreatment == SPECIAL_TREATMENT_MULTIPLY_BY_100)
							decimal = decimal.multiply(new BigDecimal(100));
					}
				if (decimal != null)
					if (decimal.scale() > scale) {
						decimal = decimal.setScale(scale, RoundingMode.HALF_DOWN);
					}

				if (decimal != null)
					if (decimal.precision() > precision)
						throw new RuntimeException("bad format for big decimal, precision is longer than limit "
								+ precision + " for value " + decimal);
				return decimal;
			} catch (ParseException e) {
				throw new RuntimeException(
						"data is supposed to be big decimal but received the following error when parsing '" + value
								+ "'. Exception " + e.getMessage());

			}

		}

	}

	private DecimalParser decimalparser;

	public static BigDecimal parsemultiplierForExport(String multiplier) {
		if (multiplier == null)
			return new BigDecimal(1);
		if (multiplier.length() == 0)
			return new BigDecimal(1);
		if (multiplier.trim().equals("M1000"))
			return new BigDecimal("0.001");
		if (multiplier.trim().equals("M100"))
			return new BigDecimal("0.01");
		if (multiplier.trim().equals("D1000"))
			return new BigDecimal("1000");
		throw new RuntimeException("Invalid multiplier " + multiplier);
	}

	public static int parseMultiplierForImport(String multiplier) {
		if (multiplier == null)
			return DecimalParser.SPECIAL_TREATMENT_NONE;
		if (multiplier.length() == 0)
			return DecimalParser.SPECIAL_TREATMENT_NONE;
		if (multiplier.trim().equals("M1000"))
			return DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_1000;
		if (multiplier.trim().equals("M100"))
			return DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_100;
		if (multiplier.trim().equals("D1000"))
			return DecimalParser.SPECIAL_TREATMENT_DIVIDE_BY_1000;
		throw new RuntimeException("Invalid multiplier " + multiplier);
	}

	public DecimalDataObjectFieldFlatFileLoaderColumn(
			DataObjectDefinition<E> definition,
			String[] arguments,
			String name,
			int scale,
			int precision,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		this.scale = scale;
		this.precision = precision;
		this.name = name;
		this.locale = locale;
		int modifier = DecimalParser.SPECIAL_TREATMENT_NONE;
		if (arguments != null)
			if (arguments.length == 1) {
				String multiplier = arguments[0];
				if (multiplier.trim().equals("M1000")) {
					logger.finer(" ---> For field " + name + ", special treatment of M1000");
					modifier = DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_1000;
					multiplyatexport = new BigDecimal("0.001");
				}
				if (multiplier.trim().equals("M100")) {
					logger.finer(" ---> For field " + name + ", special treatment of M100");
					modifier = DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_100;
					multiplyatexport = new BigDecimal("0.01");

				}
				if (multiplier.trim().equals("D1000")) {
					logger.finer(" ---> For field " + name + ", special treatment of D1000");
					modifier = DecimalParser.SPECIAL_TREATMENT_DIVIDE_BY_1000;
					multiplyatexport = new BigDecimal("1000");
				}
				if (modifier == DecimalParser.SPECIAL_TREATMENT_NONE)
					throw new RuntimeException("Decimal DataObjectFieldFlatFileLoader invalid modifier " + multiplier);
			}
		if (arguments != null)
			if (arguments.length > 1)
				throw new RuntimeException(
						"Decimal DataObjetFieldFlatFileLoader only supports 1 modifier ( M100, M1000 or D1000)");
		decimalparser = new DecimalParser(scale, precision, locale, modifier);
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		@SuppressWarnings("unchecked")
		DataObjectField<?, E> field = object.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + object.getName());
		if (!(field instanceof DecimalDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type DecimalDataObjectField but in reality, it is " + field.getClass().toString());
		DecimalDataObjectField<
				?> decimalfield = (DecimalDataObjectField<?>) object.payload.lookupSimpleFieldOnName(name);
		BigDecimal oldbigdecimal = decimalfield.getValue();
		BigDecimal newbigdecimal = FlatFileLoader.parseDecimal(value, precision, scale,
				"Flat file loader for column " + decimalfield.getName(), decimalparser);

		if (FlatFileLoader.isTheSame(oldbigdecimal, newbigdecimal)) {
			return false;
		} else {
			decimalfield.setValue(newbigdecimal);
			return true;
		}

	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		@SuppressWarnings("unchecked")
		DataObjectField<?, E> field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field == null)
			throw new RuntimeException("field " + name + " could not be looked-up on " + currentobject.getName());
		if (!(field instanceof DecimalDataObjectField))
			throw new RuntimeException("Expected field " + name
					+ " would be of type DecimalDataObjectField but in reality, it is " + field.getClass().toString());
		DecimalDataObjectField<
				?> decimalfield = (DecimalDataObjectField<?>) currentobject.payload.lookupSimpleFieldOnName(name);
		BigDecimal decimal = decimalfield.getValue();
		if (decimal != null) {
			decimal = decimal.multiply(this.multiplyatexport);
			cell.setCellValue(decimal.doubleValue());
		}
		if (this.decimalparser.getSpecialTreatment() == DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_100) {
			logger.finest("special treatment for cell percentage");
			CellStyle percentagecellstyle = FlatFileExtractor.createBorderedStyle(cell.getSheet().getWorkbook());
			percentagecellstyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("0.0%"));
			cell.setCellStyle(percentagecellstyle);
			return true;
		}
		return false;

	}

	/**
	 * puts a big decimal in cell, formatting as percentage if needed
	 * 
	 * @param cell       cell
	 * @param value      value
	 * @param multiplier multiplier in the sense of DecimalFlatFileLoader
	 * @return true if formatting was done, false if formatting was not done
	 */
	public static <F extends FieldChoiceDefinition<F>> boolean putContentInCell(
			Cell cell,
			BigDecimal value,
			String multiplier) {
		BigDecimal multiplierforexport = parsemultiplierForExport(multiplier);
		if (value!=null) cell.setCellValue(value.multiply(multiplierforexport).doubleValue());
		if (multiplierforexport.equals(new BigDecimal("0.01"))) {
			CellStyle percentagecellstyle = FlatFileExtractor.createBorderedStyle(cell.getSheet().getWorkbook());
			percentagecellstyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("0.0%"));
			cell.setCellStyle(percentagecellstyle);
			return true;
		}
		return false;
	}

	/**
	 * gets a BigDecimal from cell
	 * 
	 * @param value           value to parse
	 * @param scale           scale
	 * @param precision       precision
	 * @param locale          locale for import from text (comma or dot as decimal)
	 * @param modifier        String modifier in the sense of
	 *                        DecimalDataObjectFieldFlatFileLoader
	 * @param contextforerror context used in exceptions
	 * @return
	 */
	public static <F extends FieldChoiceDefinition<F>> BigDecimal getContentFromCell(
			Object value,
			int scale,
			int precision,
			ChoiceValue<ApplocaleChoiceDefinition> locale,
			int modifier,
			String contextforerror) {
		return FlatFileLoader.parseDecimal(value, precision, scale, contextforerror,
				new DecimalParser(scale, precision, locale, modifier));
	}
}
