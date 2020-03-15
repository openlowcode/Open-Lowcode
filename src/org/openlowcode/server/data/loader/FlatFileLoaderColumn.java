/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of a column for loading data from a flat table (CSV or XLS)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public abstract class FlatFileLoaderColumn<E extends DataObject<E>> {

	/**
	 * @return
	 */
	public boolean isDiscarded() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isStaticPreProcessing() {
		return false;
	}

	/**
	 * @param next
	 */
	public void staticpreprocessor(String next) {
		throw new RuntimeException("Static preprocessor not implemented");
	}

	/**
	 * 
	 * @return true if the loader has a preparator. Preparator will be executed
	 *         before line insertion
	 * 
	 */
	public boolean isLinePreparator() {
		return false;
	}

	/**
	 * @return true if
	 */
	public boolean secondpass() {
		return false;
	}

	/**
	 * returns the line preparation for the column.
	 * 
	 * @param maincolumnvalue              the object to process
	 * @param linepreparatorextracriterias extra criterias for the line prepataion
	 * @return
	 */
	public LinePreparation<E> LinePreparation(
			Object maincolumnvalue,
			ArrayList<LinePreparationExtra<E>> linepreparatorextracriterias) {
		throw new RuntimeException("no line  preparator with two attribute");
	}

	/**
	 * true if processing after line insertion
	 * 
	 * @return
	 */
	public boolean processAfterLineInsertion() {
		return false;
	}

	/**
	 * A line preparation
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E>
	 */
	public static class LinePreparation<E extends DataObject<E>> {
		private E payload;
		private boolean update;

		/**
		 * @return the payload
		 */
		public E getPayload() {
			return payload;
		}

		/**
		 * @return true if update was performed, false else
		 */
		public boolean isUpdate() {
			return update;
		}

		/**
		 * @param payload the object
		 * @param update  true if the object should be updated, false if the object
		 *                should be created
		 */
		public LinePreparation(E payload, boolean update) {

			this.payload = payload;
			this.update = update;
		}

	}

	/**
	 * @return true if the line is a preparator extra. Then, the loader column will
	 *         have to implement (overrides) the method generateLinePreparatorExtra
	 */
	public boolean isLinePreparatorExtra() {
		return false;
	}

	/**
	 * @param data data to process for the column
	 * @return a line preparation extra
	 */
	public LinePreparationExtra<E> generateLinePreparatorExtra(Object data) {
		throw new RuntimeException("Line preparator extra method is not implemented");
	}

	/**
	 * a line preparation extra processing. This is used to query background data
	 * necessary for the processing
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 * @param <E> the type of object
	 */
	public static interface LinePreparationExtra<E extends DataObject<E>> {
		/**
		 * generates a query condition to query relevant data to the object being
		 * processed
		 * 
		 * @param definition object definition
		 * @param alias      alias of the query
		 * @return
		 */
		public QueryCondition generateQueryCondition(DataObjectDefinition<E> definition, String alias);
	}

	/**
	 * performs the load
	 * 
	 * @param object                    the object on which to load data
	 * @param value                     an object, either Date, String or Double
	 * @param postupdateprocessingstore a store to perform post processing after the
	 *                                  single update
	 * @return true if something was changed on the object, else, false
	 */
	public abstract boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore);

	/**
	 * method to implement to treat as post processing (after object was persisted
	 * 
	 * @param object the object on which to load data
	 * @param value  an object, either Date, String or Double
	 */
	public void postprocessLine(E object, Object value) {

	}

	/**
	 * @return true if final post processing is required (typically after object
	 *         persistence, false else
	 */
	public boolean finalpostprocessing() {
		return false;
	}

	/**
	 * A flat file loader should be able to extract from an object the value, and
	 * put it in a cell.
	 * 
	 * @param currentobject the object to analyze
	 * @param cell          cell to put the data in
	 * @param context       a context in case of multiple lines per same object
	 * @return true if specific style was applied, false if specific style was not
	 *         applied
	 */
	protected abstract boolean putContentInCell(E currentobject, Cell cell, String context);

	/**
	 * if an extractor is complex, it means that:
	 * <ul>
	 * <li>there will be a call at beginning of line to get the list of duplicates
	 * lines for the object</li>
	 * <li>there will be a call at the end of line to clean the complex extractor
	 * (likely to have stored data)</li>
	 * </ul>
	 * By default, an extractor is not complex. There should be a single complex
	 * extractor per object
	 * 
	 * @return
	 */
	public boolean isComplexExtractor() {
		return false;
	}

	/**
	 * called at the beginning of processing of a line
	 * 
	 * @param currentobject object to analyze
	 * @return an array of several strings constituting the context for the line
	 */
	public String[] initComplexExtractorForObject(E currentobject) {
		throw new RuntimeException("Not implemented for non-complex extractor");
	}

	/**
	 * allows to define a restriction (list of potential values) for a cell when
	 * exporting data in a spreadsheet. The column should return null if no
	 * restriction applies to cell.<br>
	 * Should be overridden only by loaders that need to restrict values
	 * 
	 * @return the list of potential values, or null if no restriction.
	 */
	public String[] getValueRestriction() {
		return null;
	}

}
