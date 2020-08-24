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

/**
 * A helper managing one dimension of the multi-dimension child property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> payload of the field
 * @param <G> type of the parent object (or any other object to be used)
 */
public abstract class MultichildValueHelper<
		E extends DataObject<E> & UniqueidentifiedInterface<E> & MultidimensionchildInterface<E, G>,
		F extends Object,
		G extends DataObject<G> & UniqueidentifiedInterface<G>> {

	private static Logger logger = Logger.getLogger(MultichildValueHelper.class.getName());

	private BiConsumer<E, F> setter;
	private Function<E, F> getter;

	private BiConsumer<Cell, F> cellfiller;
	private String[] restrictions;
	private BiFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, F> payloadparser;
	private String fieldname;

	private Function<F, String> printer;

	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			BiConsumer<Cell, F> cellfiller,
			BiFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, F> payloadparser,
			Function<F, String> printer) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = null;
	}

	public MultichildValueHelper(
			String fieldname,
			BiConsumer<E, F> setter,
			Function<E, F> getter,
			BiConsumer<Cell, F> cellfiller,
			BiFunction<Object, ChoiceValue<ApplocaleChoiceDefinition>, F> payloadparser,
			Function<F, String> printer,
			String[] restrictions) {
		this.fieldname = fieldname;
		this.setter = setter;
		this.getter = getter;
		this.cellfiller = cellfiller;
		this.payloadparser = payloadparser;
		this.printer = printer;
		this.restrictions = restrictions;
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
	public void fillCell(Cell cell, E object) {
		this.cellfiller.accept(cell, this.getter.apply(object));
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
	 * gets the minimum values that have to exist for this field in the list of
	 * children
	 * 
	 * @return the minimum values required for this field
	 */
	public abstract F[] getMinimumvalues();

	/**
	 * gets the maximum values that have to exist for this field on the list of
	 * children. Users can choose which values would work
	 * 
	 * @return the maximum number of values required for this field
	 */

	public abstract F[] getMaximumvalues();

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
	 * @return the value to consolidate other values in. Example, if you want to
	 *         have always time data from a 5 years time window, you may have a
	 *         value 'Before' that will consolidate values from previous year when
	 *         you get previous year data
	 */
	public abstract F getDefaultValueForOtherData();

	/**
	 * This method checks if the current value can be kept or an alternative should
	 * be used or the value should be discarded. Updates object passed as argument
	 * with the alternative value if required
	 * 
	 * @return true if value should be kept / consolidated, false if value should be
	 *         discarded
	 */
	public boolean replaceWithDefaultValue(E object) {
		if (allowothervalues())
			return true;
		if (allowUserValue())
			return true;
		F[] maximumvalues = getMaximumvalues();
		if (maximumvalues != null)
			for (int i = 0; i < maximumvalues.length; i++) {
				if (!StandardUtil.compareIncludesNull(maximumvalues[i], get(object)))
					return true;
			}
		F alternative = getDefaultValueForOtherData();
		if (alternative != null) {
			set(object, alternative);
			return true;
		}
		return false;
	}

	public boolean LoadIfDifferent(E object, Object value, ChoiceValue<ApplocaleChoiceDefinition> applocale) {
		F newvalue = payloadparser.apply(value, applocale);
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
				int index) {
			this.mainsecondary = false;
			this.helper = helper;
			this.index = index;
			this.applocale = applocale;
		}

		@Override
		public String[] getValueRestriction() {
			return restrictions;
		}

		@Override
		public boolean load(G object, Object value, PostUpdateProcessingStore<G> postupdateprocessingstore) {
			logger.warning("Adding value in index " + index + " value = " + value.toString());
			helper.setSecondaryValueForLoading(index,
					MultichildValueHelper.this.print(MultichildValueHelper.this.payloadparser.apply(value, applocale)));

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

	public class MainValueFlatFileLoader
			extends
			FlatFileLoaderColumn<G> {

		private F mainvalue;
		private HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper;
		private ChoiceValue<ApplocaleChoiceDefinition> applocale;
		private MultichildValueHelper<E, ?, G> payloadhelper;

		public MainValueFlatFileLoader(
				HasmultidimensionalchildFlatFileLoaderHelper<G, E> helper,
				ChoiceValue<ApplocaleChoiceDefinition> applocale,
				String unparsedpayload,
				MultichildValueHelper<E, ?, G> payloadhelper) {
			this.mainvalue = MultichildValueHelper.this.payloadparser.apply(unparsedpayload, applocale);
			this.helper = helper;
			this.applocale = applocale;
			this.payloadhelper = payloadhelper;
		}

		@Override
		public boolean load(G object, Object value, PostUpdateProcessingStore<G> postupdateprocessingstore) {
			helper.setContext(object);
			String helpercontextkey = helper.getContextKey();
			E relevantchild = helper.getChildForLineAndColumnKey(helpercontextkey,
					MultichildValueHelper.this.print(mainvalue));
			if (relevantchild == null) {
				throw new RuntimeException("Did not find existing child for key, debug = " + helper
						.getDebugForLineAndColumnKey(helpercontextkey, MultichildValueHelper.this.print(mainvalue)));
			}
			return this.payloadhelper.LoadIfDifferent(relevantchild, value, applocale);

		}

		@Override
		protected boolean putContentInCell(G currentobject, Cell cell, String context) {
			E child = helper.getChildForLineAndColumnKey(context, MultichildValueHelper.this.print(mainvalue));
			if (child == null) {
				logger.warning(
						"Did not find child for context = " + context + " for object " + currentobject.dropToString()
								+ " for mainvalue = " + MultichildValueHelper.this.printer.apply(this.mainvalue)+", debug = "+helper.getDebugForLineAndColumnKey(context, MultichildValueHelper.this.print(mainvalue)));

				return false;
			}
			MultichildValueHelper<E, ?, G> payloadhelper = helper.getPayloadHelper();
			payloadhelper.fillCell(cell, child);

			return true;
		}

	}

}
