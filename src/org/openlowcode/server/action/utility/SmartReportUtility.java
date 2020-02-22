/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LinkobjectInterface;
import org.openlowcode.server.data.properties.NumberedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.tools.misc.CompositeObjectKey;

/**
 * This class regroups utility methods to be used inside smart reports. The
 * methods can of course be used in another context
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SmartReportUtility {
	private static Logger logger = Logger.getLogger(SmartReportUtility.class.getName());

	/**
	 * This method will filter according to the following business rules:
	 * <ul>
	 * <li>if the right objects in selection is empty (null or zero element array),
	 * all left objects are returned</li>
	 * <li>if there are right objects in selection, left objects with at least one
	 * link to the right object in selection are returned</li>
	 * </ul>
	 * 
	 * @param leftobjects             the objects to filter
	 * @param rightobjectsinselection the list of right objects that are part of
	 *                                selection
	 * @param existinglinks           links between left and right objects
	 * @param definition              definition of the left object
	 * @return the filtered list of left objects
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & LinkobjectInterface<F, E, G>,
			G extends DataObject<G> & UniqueidentifiedInterface<G> & NumberedInterface<G>> E[] filterByLinkRightObject(
					E[] leftobjects,
					G[] rightobjectsinselection,
					TwoDataObjects<F, G>[] existinglinks,
					DataObjectDefinition<E> definition) {
		logger.fine(" * starting filter by link right object with left objects nr = " + leftobjects
				+ ", right objects in selection nr = " + rightobjectsinselection + ", links nr = " + existinglinks);
		HashMap<CompositeObjectKey<E>, G> filtermap = new HashMap<CompositeObjectKey<E>, G>();
		if (existinglinks != null)
			for (int i = 0; i < existinglinks.length; i++) {
				TwoDataObjects<F, G> link = existinglinks[i];
				CompositeObjectKey<E> key = new CompositeObjectKey<E>(link.getObjectOne().getLfid(),
						new String[] { link.getObjectTwo().getNr() });
				filtermap.put(key, link.getObjectTwo());
				logger.fine("        - adding composite key " + key);
			}
		logger.fine("------------------------------------------------------------------------------");
		boolean hasfilterdata = false;
		if (rightobjectsinselection != null)
			if (rightobjectsinselection.length > 0)
				hasfilterdata = true;
		ArrayList<E> filteredresult = new ArrayList<E>();
		for (int i = 0; i < leftobjects.length; i++) {
			E thisobject = leftobjects[i];
			if (hasfilterdata) {
				boolean hastag = false;
				for (int j = 0; j < rightobjectsinselection.length; j++) {
					CompositeObjectKey<E> key = new CompositeObjectKey<E>(thisobject.getId(),
							new String[] { rightobjectsinselection[j].getNr() });
					if (filtermap.containsKey(key)) {
						logger.fine("   - found a match for object " + thisobject.dropIdToString() + " number "
								+ rightobjectsinselection[j].getNr());

						hastag = true;
						break;
					} else {
						logger.fine("    - no match for key " + key);
					}

				}
				if (hastag) {
					logger.fine("   - added object " + thisobject.dropIdToString() + " to selection ");
					filteredresult.add(thisobject);
				}
			} else {
				filteredresult.add(thisobject);
			}
		}
		logger.fine("     --> returning an array with  element nr = " + filteredresult.size());
		logger.fine("------------------------------------------------------------------------------");

		return filteredresult.toArray(definition.generateArrayTemplate());
	}

	/**
	 * provides a mapper that provides second object number from a first object id
	 * @param mapdata a combination of object pairs
	 * @return a function that provides the number of the second object, given the id of the first object for a link
	 */
	public static <
			E extends DataObject<E>,
			F extends DataObject<F> & LinkobjectInterface<F, E, G>,
			G extends DataObject<G> & NumberedInterface<G>> Function<DataObjectId<E>, String> getUniqueLinkNr(
					TwoDataObjects<F, G>[] mapdata) {
		HashMap<DataObjectId<E>, String> filtermap = new HashMap<DataObjectId<E>, String>();
		if (mapdata != null)
			for (int i = 0; i < mapdata.length; i++) {
				TwoDataObjects<F, G> thislink = mapdata[i];
				filtermap.put(thislink.getObjectOne().getLfid(), thislink.getObjectTwo().getNr());
			}
		return (id) -> (filtermap.get(id));
	}

	/**
	 * 
	 * 
	 * @param objects
	 * @param extractcolumnlabels
	 * @param suffix
	 * @return
	 */
	public static <E extends DataObject<E>> String[] getColumnValues(
			E[] objects,
			Function<E, String> extractcolumnlabels,
			String suffix) {
		HashMap<String, String> results = new HashMap<String, String>();
		if (objects != null)
			for (int i = 0; i < objects.length; i++) {
				String value = extractcolumnlabels.apply(objects[i]) + (suffix != null ? suffix : "");
				results.put(value, value);
			}
		return results.keySet().toArray(new String[0]);
	}

	/**
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class ColumnList {
		private ArrayList<String> totalcolumns;
		private HashMap<String, Integer> columnindex;

		/**
		 * 
		 */
		public ColumnList() {
			this.totalcolumns = new ArrayList<String>();
		}

		/**
		 * adds the columns to the column list
		 * 
		 * @param columns to add
		 */
		public void addColumns(String[] columns) {
			if (columns != null)
				this.totalcolumns.addAll(Arrays.asList(columns));
		}

		/**
		 * orders the columns by name
		 */
		public void Order() {
			Collections.sort(totalcolumns);
			columnindex = new HashMap<String, Integer>();
			for (int i = 0; i < totalcolumns.size(); i++)
				columnindex.put(totalcolumns.get(i), new Integer(i));
		}

		/**
		 * @param column name of the column
		 * @return the index of the column, or an exception, if the column name does not
		 *         exist
		 */
		public int getColumnIndex(String column) {
			Integer index = columnindex.get(column);
			if (index == null)
				throw new RuntimeException("Column " + column + " is not in the column list");
			return index.intValue();
		}

		/**
		 * @return the number of columns
		 */
		public int getSize() {
			return totalcolumns.size();
		}

		/**
		 * @param index index of the column between 0 (included) and getSize()
		 *              (excluded)
		 * @return the name of the column at the given index
		 */
		public String getColumn(int index) {
			return totalcolumns.get(index);
		}
	}
}
