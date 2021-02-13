/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.multichild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.properties.HasmultidimensionalchildFlatFileLoaderHelper;
import org.openlowcode.server.data.properties.MultidimensionchildInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.tools.misc.StandardUtil;
import org.openlowcode.tools.misc.TriConsumer;
import org.openlowcode.tools.misc.TriFunction;

/**
 * A helper managing one dimension of the multi-dimension child property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> payload of the field
 * @param <G> type of the parent object (or any other object to be used)
 * @since 1.11
 */
public abstract class MultichildValueHelper<
		E extends DataObject<E> & UniqueidentifiedInterface<E> & MultidimensionchildInterface<E, G>,
		F extends Object,
		G extends DataObject<G> & UniqueidentifiedInterface<G>> {

	private static Logger logger = Logger.getLogger(MultichildValueHelper.class.getName());

	private BiConsumer<E, F> setter;
	private Function<E, F> getter;

	private TriConsumer<Cell, String[], F> cellfiller;
	private String[] restrictions;
	private TriFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, String[], F> payloadparser;
	private String fieldname;
	private BiFunction<F, F, F> valueconsolidator;

	private Function<F, String> printer;

	/**
	 * Creates a value helper for multi-child
	 * 
	 * @param fieldname     name of the field
	 * @param setter        function to set the value on the object
	 * @param getter        function to get the value on the object
	 * @param cellfiller    a function to fill a spreadsheet cell
	 * @param payloadparser a function to parse payload
	 * @param printer       a function to print the payload for object keys
	 */
	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			TriConsumer<Cell, String[], F> cellfiller,
			TriFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, String[], F> payloadparser,
			Function<F, String> printer) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = null;
		this.valueconsolidator = null;
	}

	/**
	 * Creates a value helper for multi-child
	 * 
	 * @param fieldname         name of the field
	 * @param setter            function to set the value on the object
	 * @param getter            function to get the value on the object
	 * @param cellfiller        a function to fill a spreadsheet cell
	 * @param payloadparser     a function to parse payload
	 * @param printer           a function to print the payload for object keys
	 * @param valueconsolidator a function to consolidate values (typically performs
	 *                          a sum if object is numeric)
	 */
	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			TriConsumer<Cell, String[], F> cellfiller,
			TriFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, String[], F> payloadparser,
			Function<F, String> printer,
			BiFunction<F, F, F> valueconsolidator) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = null;
		this.valueconsolidator = valueconsolidator;
	}

	/**
	 * Creates a value helper for multi-child
	 * 
	 * @param fieldname     name of the field
	 * @param setter        function to set the value on the object
	 * @param getter        function to get the value on the object
	 * @param cellfiller    a function to fill a spreadsheet cell
	 * @param payloadparser a function to parse payload
	 * @param printer       a function to print the payload for object keys
	 * @param restrictions
	 */
	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			TriConsumer<Cell, String[], F> cellfiller,
			TriFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, String[], F> payloadparser,
			Function<F, String> printer,
			String[] restrictions) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = restrictions;
		this.valueconsolidator = null;
	}

	/**
	 * Creates a value helper for multi-child
	 * 
	 * @param fieldname         name of the field
	 * @param setter            function to set the value on the object
	 * @param getter            function to get the value on the object
	 * @param cellfiller        a function to fill a spreadsheet cell
	 * @param payloadparser     a function to parse payload
	 * @param printer           a function to print the payload for object keys
	 * @param restrictions
	 * @param valueconsolidator a function to consolidate values (typically performs
	 *                          a sum if object is numeric)
	 */
	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			TriConsumer<Cell, String[], F> cellfiller,
			TriFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, String[], F> payloadparser,
			Function<F, String> printer,
			String[] restrictions,
			BiFunction<F, F, F> valueconsolidator) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = restrictions;
		this.valueconsolidator = valueconsolidator;
	}

	/**
	 * this method is only relevant to be implemented for a main value helper. It
	 * should return true when the column should be consolidated for totals
	 * 
	 * @param value value of the object
	 * @return true if it should be consolidated into the total, false if it should
	 *         be discarded
	 */

	public abstract boolean filterForPayloadTotal(F value);

	/**
	 * This method will determine if the object is a candidate for total column
	 * 
	 * @param object the object with the MultiDimensionChild property
	 * @return true if the payload should be consolidated, or false else
	 */
	public boolean filterForPayloadTotalFromObject(E object) {
		return filterForPayloadTotal(this.get(object));
	}

	public String getFieldName() {
		return this.fieldname;
	}

	/**
	 * sets the context for the helper with the parent
	 * 
	 * @param parent parent object
	 */
	public abstract void setContext(G parent);

	/**
	 * sets the payload of the field on the child object
	 * 
	 * @param object  a child object
	 * @param payload the payload to set on the object
	 */
	public void set(E object, F payload) {
		setter.accept(object, payload);
	}

	/**
	 * 
	 * 
	 * @param cell
	 * @param object
	 */
	public void fillCell(Cell cell, E object, String[] extraattributes) {
		this.cellfiller.apply(cell, extraattributes, this.getter.apply(object));
	}

	/**
	 * @param cell
	 * @param parsedvalue
	 * @param locale
	 * @param extraattributes
	 * @since 1.12
	 */
	public void fillWithValue(
			E object,
			String parsedvalue,
			ChoiceValue<ApplocaleChoiceDefinition> locale,
			String[] extraattributes) {
		F payload = this.payloadparser.apply(parsedvalue, locale, extraattributes);
		this.setter.accept(object, payload);
	}

	/**
	 * @param cell
	 * @param value
	 */
	public void fillCellWithValue(Cell cell, F value, String[] extraattributes) {
		this.cellfiller.apply(cell, extraattributes, value);
	}

	/**
	 * gets the payload of the field on the child object
	 * 
	 * @param object the child object
	 * @return the extracted payload for the field
	 */
	public F get(E object) {
		return getter.apply(object);
	}

	public String print(F payload) {
		return this.printer.apply(payload);
	}

	public String getAndPrint(E object) {
		return printer.apply(getter.apply(object));
	}

	/**
	 * gets the mandatory values that have to exist for this field in the list of
	 * children
	 * 
	 * @return the mandatory values required for this field
	 */
	public abstract F[] getMandatoryValues();
	
	/**
	 * Returns true if the value is part of mandatory values
	 * 
	 * @param mainvalue the value
	 * @return
	 */
	public boolean isIsMandatory(Object mainvalue) {
		F[] mandatoryvalues = getMandatoryValues();
		if (mandatoryvalues!=null) for (int i=0;i<mandatoryvalues.length;i++) {
			if (mandatoryvalues[i]!=null) if (mandatoryvalues[i].equals(mainvalue)) return true;
		}
		return false;
	}

	/**
	 * gets the optional values that have to exist for this field on the list of
	 * children. Users can choose which values would work
	 * 
	 * @return the optional number of values required for this field
	 */

	public abstract F[] getOptionalValues();

	/**
	 * @return true if free values are allowed
	 */
	public abstract boolean allowUserValue();

	/**
	 * @return true if other values present in legacy data (for a new version) are
	 *         to be kept. Else, they are consolidated if
	 */
	public abstract boolean allowothervalues();

	/**
	 * @param current the current value
	 * @return the value to consolidate other values in. Example, if you want to
	 *         have always time data from a 5 years time window, you may have a
	 *         value 'Before' that will consolidate values from previous year when
	 *         you get previous year data. Return null in order not to consolidate
	 *         the data
	 * @since 1.11.4
	 */

	public abstract F getDefaultValueForOtherData(F current);

	/**
	 * This method checks if the current value can be kept or an alternative should
	 * be used or the value should be discarded. Updates object passed as argument
	 * with the alternative value if required
	 * 
	 * @return true if value should be kept / consolidated, false if value should be
	 *         discarded
	 */
	public boolean replaceWithDefaultValue(E object) {
		logger.finer(" ------------------ Replace With Default Value audit " + fieldname + " --------------------");
		if (allowothervalues()) {
			logger.finer("Allow other values");
			return true;
		}
		if (allowUserValue()) {
			logger.finer("Allow user values");
			return true;
		}

		F[] minimumvalues = this.getMandatoryValues();
		if (minimumvalues != null)
			for (int i = 0; i < minimumvalues.length; i++) {
				logger.finest("      --> compare " + minimumvalues[i] + " and " + get(object));
				if (StandardUtil.compareIncludesNull(minimumvalues[i], get(object))) {
					logger.finest("					Match");
					return true;
				}

			}

		F[] maximumvalues = getOptionalValues();
		if (maximumvalues != null)
			for (int i = 0; i < maximumvalues.length; i++) {
				logger.finest("      --> compare " + maximumvalues[i] + " and " + get(object));
				if (StandardUtil.compareIncludesNull(maximumvalues[i], get(object))) {
					logger.finest("					Match");
					return true;
				}

			}
		F alternative = getDefaultValueForOtherData(get(object));
		if (alternative != null) {
			logger.finer("       ---> Found alternative " + alternative);
			set(object, alternative);
			return true;
		}
		logger.finer("No match found");
		return false;
	}

	public boolean LoadIfDifferent(
			E object,
			Object value,
			ChoiceValue<ApplocaleChoiceDefinition> applocale,
			String[] extraattributes) {
		F newvalue = payloadparser.apply(value, applocale, extraattributes);
		F oldvalue = getter.apply(object);
		if (StandardUtil.compareIncludesNull(newvalue, oldvalue)) {
			return false;
		} else {
			setter.accept(object, newvalue);
			object.update();
			return true;
		}
	}

	@Override
	public String toString() {
		return this.fieldname;
	}

	public class SecondValueFlatFileLoader
			extends
			FlatFileLoaderColumn<G> {

		private boolean mainsecondary = false;
		private HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper;
		private int index;
		private ChoiceValue<ApplocaleChoiceDefinition> applocale;
		private String[] extraattributes;

		private F parsedvalue;
		
		public F getParsedValue() {
			return parsedvalue;
		}
		
		public MultichildValueHelper<E,F,G> getParent() {
			return MultichildValueHelper.this;
		}
		
		public SecondValueFlatFileLoader(
				HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper,
				ChoiceValue<ApplocaleChoiceDefinition> applocale,
				int index,
				boolean mainsecondary) {
			this.mainsecondary = mainsecondary;
			this.helper = helper;
			this.index = index;
			this.applocale = applocale;
		}

		public SecondValueFlatFileLoader(
				HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper,
				ChoiceValue<ApplocaleChoiceDefinition> applocale,
				int index,
				String[] extraattributes) {
			this.mainsecondary = false;
			this.helper = helper;
			this.index = index;
			this.applocale = applocale;
			this.extraattributes = extraattributes;
		}

		public HasmultidimensionalchildFlatFileLoaderHelper<G, E> getHelper() {
			return this.helper;
		}
		
		@Override
		public String[] getValueRestriction() {
			return restrictions;
		}

		@Override
		public boolean load(G object, Object value, PostUpdateProcessingStore<G> postupdateprocessingstore) {
			logger.warning("Adding value in index " + index + " value = " + value.toString());
			parsedvalue = MultichildValueHelper.this.payloadparser.apply(value, applocale, extraattributes);
			helper.setSecondaryValueForLoading(index, MultichildValueHelper.this
					.print(parsedvalue));

			// returns false as no change of value is done
			return false;
		}

		@Override
		protected boolean putContentInCell(G currentobject, Cell cell, String context) {
			E child = helper.getFirstChildForLineKey(context);
			if (child == null) {
				logger.warning("Did not find object for key " + context + ". This is not normal behaviour");
				return false;
			}
			F value = getter.apply(child);
			cell.setCellValue(MultichildValueHelper.this.printer.apply(value));
			return true;

		}

		@Override
		public String[] initComplexExtractorForObject(G currentobject) {
			if (!this.mainsecondary)
				return null;
			return helper.generateKeyAndLoadExistingData(currentobject);
		}

		@Override
		public boolean isComplexExtractor() {
			return this.mainsecondary;
		}
	}

	public class MainValueTotalFlatFileLoader
			extends
			FlatFileLoaderColumn<G> {

		private HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper;
		@SuppressWarnings("unused")
		private ChoiceValue<ApplocaleChoiceDefinition> applocale;
		private MultichildValueHelper<E, F, G> payloadhelper;
		private String[] extraattributes;
		private MultichildValueHelper<E, ?, G> mainvaluehelper;

		@SuppressWarnings("unchecked")
		public MainValueTotalFlatFileLoader(
				HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper,
				ChoiceValue<ApplocaleChoiceDefinition> applocale,
				MultichildValueHelper<E, ?, G> payloadhelper,
				String[] extraattributes) {

			this.helper = helper;
			this.applocale = applocale;
			this.payloadhelper = (MultichildValueHelper<E, F, G>) payloadhelper;
			this.extraattributes = extraattributes;
			mainvaluehelper = helper.getMainValueHelper();
		}

		@Override
		public boolean processAfterLineInsertion() {
			return true;
		}

		@Override
		public boolean load(G object, Object value, PostUpdateProcessingStore<G> postupdateprocessingstore) {
			return false;
		}

		@Override
		protected boolean putContentInCell(G currentobject, Cell cell, String context) {
			logger.finer("Processing total column for object " + currentobject.dropIdToString());
			ArrayList<E> children = helper.getChildrenForLine(context);
			F total = null;
			if (valueconsolidator == null)
				throw new RuntimeException("No value consolidator for " + this.getClass() + " and "
						+ MultichildValueHelper.this.getClass());
			for (int i = 0; i < children.size(); i++) {
				boolean valid = mainvaluehelper.filterForPayloadTotalFromObject(children.get(i));
				if (valid) {
					F value = getter.apply(children.get(i));
					logger.finer("        > " + value);
					total = valueconsolidator.apply(total, value);
				}
			}
			logger.finer("    ----> total " + total);
			payloadhelper.fillCellWithValue(cell, total, extraattributes);

			return true;
		}
	}

	public class MainValueFlatFileLoader
			extends
			FlatFileLoaderColumn<G> {

		private F mainvalue;
		private HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper;
		private ChoiceValue<ApplocaleChoiceDefinition> applocale;
		private MultichildValueHelper<E, ?, G> payloadhelper;
		private String[] extraattributes;

		public MainValueFlatFileLoader(
				HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper,
				ChoiceValue<ApplocaleChoiceDefinition> applocale,
				String unparsedpayload,
				MultichildValueHelper<E, ?, G> payloadhelper,
				String[] extraattributes) {
			this.mainvalue = MultichildValueHelper.this.payloadparser.apply(unparsedpayload, applocale, null);
			this.helper = helper;
			this.applocale = applocale;
			this.payloadhelper = payloadhelper;
			this.extraattributes = extraattributes;
		}

		public HasmultidimensionalchildFlatFileLoaderHelper<G, E> getHelper() {
			return this.helper;
		}
		
		@Override
		public boolean processAfterLineInsertion() {
			return true;
		}

		@Override
		public boolean load(G object, Object value, PostUpdateProcessingStore<G> postupdateprocessingstore) {
			helper.setContext(object);
			String helpercontextkey = helper.getContextKey();
			helper.getMainValueHelper().setContext(object);
			if (!helper.getMainValueHelper().isIsMandatory(mainvalue)) throw new RuntimeException("Column is not valid: "+mainvalue);
			E relevantchild = helper.getChildForLineAndColumnKey(helpercontextkey,
					MultichildValueHelper.this.print(mainvalue));
			if (relevantchild == null) {
				
				boolean valid = helper.generateNewRowForContext(object, applocale, extraattributes);
				if (valid) {
					relevantchild = helper.getChildForLineAndColumnKey(helpercontextkey,
							MultichildValueHelper.this.print(mainvalue));
				} else

					throw new RuntimeException(
							"Did not find existing child for key, debug = " + helper.getDebugForLineAndColumnKey(
									helpercontextkey, MultichildValueHelper.this.print(mainvalue)));
			}
			return this.payloadhelper.LoadIfDifferent(relevantchild, value, applocale, extraattributes);

		}

		@Override
		protected boolean putContentInCell(G currentobject, Cell cell, String context) {
			E child = helper.getChildForLineAndColumnKey(context, MultichildValueHelper.this.print(mainvalue));
			if (child == null) {
				logger.warning("Did not find child for context = " + context + " for object "
						+ currentobject.dropToString() + " for mainvalue = "
						+ MultichildValueHelper.this.printer.apply(this.mainvalue) + ", debug = "
						+ helper.getDebugForLineAndColumnKey(context, MultichildValueHelper.this.print(mainvalue)));

				return false;
			}
			MultichildValueHelper<E, ?, G> payloadhelper = helper.getPayloadHelper();
			payloadhelper.fillCell(cell, child, extraattributes);

			return true;
		}

	}

	/**
	 * checks if the element is valid
	 * 
	 * @param optionalorinvalid
	 * @return true if the element is valid for this criteria
	 * @since 1.12
	 */
	public boolean isValid(E optionalorinvalid) {
		if (this.allowothervalues())
			return true;
		F value = getter.apply(optionalorinvalid);
		if (value == null)
			return false;
		F[] mandatoryvalues = this.getMandatoryValues();
		F[] optionalvalues = this.getOptionalValues();
		if (mandatoryvalues != null)
			for (int i = 0; i < mandatoryvalues.length; i++)
				if (value.equals(mandatoryvalues[i]))
					return true;
		if (optionalvalues != null)
			for (int i = 0; i < optionalvalues.length; i++)
				if (value.equals(optionalvalues[i]))
					return true;
		return false;
	}

	public boolean isTextValid(
			String text,
			ChoiceValue<ApplocaleChoiceDefinition> applocale,
			String[] extraattributes) {
		F payload = payloadparser.apply(text, applocale, extraattributes);
		if (this.allowothervalues())
			return true;
		F[] mandatoryvalues = this.getMandatoryValues();
		F[] optionalvalues = this.getOptionalValues();
		if (mandatoryvalues != null)
			for (int i = 0; i < mandatoryvalues.length; i++)
				if (payload.equals(mandatoryvalues[i]))
					return true;
		if (optionalvalues != null)
			for (int i = 0; i < optionalvalues.length; i++)
				if (payload.equals(optionalvalues[i]))
					return true;
		return false;
	}

	/**
	 * @param thisoptional
	 * @return
	 * @since 1.12
	 */
	public ArrayList<E> generateElementsForAllMandatory(E thisoptional, G thisparent,MultidimensionchildHelper<E,G> helper,HashMap<String, HashMap<String, E>> childrenbykey) {
		ArrayList<E> returnelements = new ArrayList<E>();
		this.setContext(thisparent);
		F[] allmandatories = this.getMandatoryValues();
		for (int i = 0; i < allmandatories.length; i++) {
			E thischild = thisoptional.deepcopy();
			setter.accept(thischild, allmandatories[i]);
			
			String keyforchild = helper.generateKeyForObject(thischild, true);
			String mainvalue = helper.getMainValueHelper().getAndPrint(thischild);
			boolean alreadypresent=false;
			if (childrenbykey.containsKey(keyforchild)) if (childrenbykey.get(keyforchild).containsKey(mainvalue)) alreadypresent=true;
			if (!alreadypresent) returnelements.add(thischild);
		}
		return returnelements;
	}

	/**
	 * @param thisoptional
	 * @param childrenbykey
	 * @param multidimensionhelper
	 * @param payloadhelper
	 * @return
	 */
	public ArrayList<E> getMissingElementsForKey(
			E thisoptional,
			HashMap<String, E> childrenbykey,
			MultidimensionchildHelper<E, G> multidimensionhelper,
			MultichildValueHelper<E, ?, G> payloadhelper) {
		HashMap<String, F> valuesbykey = new HashMap<String, F>();
		Iterator<String> childrenkeyiterator = childrenbykey.keySet().iterator();
		while (childrenkeyiterator.hasNext()) {
			E element = childrenbykey.get(childrenkeyiterator.next());
			F value = getter.apply(element);
			valuesbykey.put(this.print(value), value);
		}
		Iterator<F> existingvalues = valuesbykey.values().iterator();
		ArrayList<E> missingvalues = new ArrayList<E>();
		while (existingvalues.hasNext()) {
			F value = existingvalues.next();
			E newoptional = thisoptional.deepcopy();
			payloadhelper.set(newoptional, null);
			this.setter.accept(newoptional, value);
			String keyforobject = multidimensionhelper.generateKeyForObject(newoptional);
			if (!childrenbykey.containsKey(keyforobject))
				missingvalues.add(newoptional);

		}
		return missingvalues;
	}
	
	public SecondaryValueSelection<E,F,G> getSecondaryValueSelectionForField() {
		return new SecondaryValueSelection<E,F,G>(this);
	}

}
