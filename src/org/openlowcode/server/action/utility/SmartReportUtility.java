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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	 * 
	 * @param mapdata a combination of object pairs
	 * @return a function that provides the number of the second object, given the
	 *         id of the first object for a link
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
	 * provides a mapper that provides second object number from a first object id
	 * 
	 * @param mapdata a combination of object pairs
	 * @return a function that provides a string containing all the values of number
	 *         in the right object, with each value being ordered by alphabetic
	 *         number
	 */
	public static <
			E extends DataObject<E>,
			F extends DataObject<F> & LinkobjectInterface<F, E, G>,
			G extends DataObject<G> & NumberedInterface<G>> Function<DataObjectId<E>, String> getMultipleLinkNr(
					TwoDataObjects<F, G>[] mapdata) {

		HashMap<DataObjectId<E>, ArrayList<String>> filtermap = new HashMap<DataObjectId<E>, ArrayList<String>>();

		if (mapdata != null)
			for (int i = 0; i < mapdata.length; i++) {
				TwoDataObjects<F, G> thislink = mapdata[i];
				ArrayList<String> previous = filtermap.get(thislink.getObjectOne().getLfid());
				if (previous == null) {
					previous = new ArrayList<String>();
					filtermap.put(thislink.getObjectOne().getLfid(), previous);
				}
				previous.add(thislink.getObjectTwo().getNr());
			}

		HashMap<DataObjectId<E>, String> consolidatedfiltermap = new HashMap<DataObjectId<E>, String>();

		Iterator<DataObjectId<E>> keyiterator = filtermap.keySet().iterator();
		while (keyiterator.hasNext()) {
			DataObjectId<E> key = keyiterator.next();
			ArrayList<String> allnumbers = filtermap.get(key);
			allnumbers.sort(null);
			StringBuffer classif = new StringBuffer();
			for (int i = 0; i < allnumbers.size(); i++) {
				if (i > 0)
					classif.append(", ");
				classif.append(allnumbers.get(i));
			}
			consolidatedfiltermap.put(key, classif.toString());
		}

		return (id) -> (consolidatedfiltermap.get(id));
	}

	/**
	 * generates Column Values without order
	 * 
	 * @param objects             objects
	 * @param extractcolumnlabels how to extract the column label
	 * @param suffix              suffix for the column
	 * @return the list of column headers
	 */
	public static <E extends DataObject<E>, F extends Object> void fillColumnValues(
			ColumnGrouping<F> existingcolumngrouping,
			E[] objects,
			Function<E, F> extractcolumnpayloads,
			Function<E, String> extractcolumnlabels,
			String suffix) {
		HashMap<String, ColumnIndex<F>> results = new HashMap<String, ColumnIndex<F>>();
		if (objects != null)
			for (int i = 0; i < objects.length; i++) {
				String value = extractcolumnlabels.apply(objects[i]) + (suffix != null ? suffix : "");
				results.put(value, new ColumnIndex<F>(extractcolumnpayloads.apply(objects[i]), value));
			}
		Iterator<ColumnIndex<F>> valuesiterator = results.values().iterator();
		while (valuesiterator.hasNext())
			existingcolumngrouping.addColumn(valuesiterator.next());

	}

	/**
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class ColumnList {

		private HashMap<Integer, ColumnGrouping<?>> columnsregister;

		public ColumnList() {
			this.columnsregister = new HashMap<Integer, ColumnGrouping<?>>();
		}

		public ColumnGrouping<?> getGroupingForIndex(int index) {
			return columnsregister.get(new Integer(index));
		}

		public void setGroupingForIndex(int index, ColumnGrouping<?> grouping) {
			columnsregister.put(new Integer(index), grouping);
		}

		/**
		 * orders the columns by name
		 */
		public void order() {
			Iterator<Integer> groupsiterator = columnsregister.keySet().iterator();
			while (groupsiterator.hasNext())
				columnsregister.get(groupsiterator.next()).order();

		}

		public List<String> getAllColumnslabel() {
			ArrayList<String> labels = new ArrayList<String>();
			List<Integer> orderedcolumnsindex = new ArrayList<Integer>(columnsregister.keySet());
			Collections.sort(orderedcolumnsindex);
			for (int i = 0; i < orderedcolumnsindex.size(); i++) {
				ColumnGrouping<?> grouping = columnsregister.get(orderedcolumnsindex.get(i));
				for (int j = 0; j < grouping.columns.size(); j++)
					labels.add(grouping.columns.get(j).label);
			}
			return labels;
		}

	}

	/**
	 * An index for a column to allow for smart ordering
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */

	public static class ColumnIndex<E extends Object> {
		private E payload;
		private String label;

		/**
		 * @param payload payload of the column, to use for ordering if orderable
		 * @param label   strign label, used else to order a column
		 */
		public ColumnIndex(E payload, String label) {
			super();
			this.payload = payload;
			this.label = label;
		}

		/**
		 * @return payload
		 */
		public E getPayload() {
			return payload;
		}

		/**
		 * @return label
		 */
		public String getLabel() {
			return label;
		}

	}

	/**
	 * the repository for columns of a grouping index
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */

	public static class ColumnGrouping<E extends Object> {
		int groupingindex;
		private ArrayList<ColumnIndex<E>> columns;

		/**
		 * creates a new column grouping for the given index
		 * 
		 * @param groupingindex an integer (positive)
		 * @param columnstoadd  the list of columns to add
		 */
		public ColumnGrouping(int groupingindex, List<ColumnIndex<E>> columnstoadd) {
			this.columns = new ArrayList<ColumnIndex<E>>();
			if (columnstoadd != null)
				for (int i = 0; i < columnstoadd.size(); i++)
					this.addColumn(columnstoadd.get(i));
		}

		/**
		 * @param column adds one column
		 */
		public void addColumn(ColumnIndex<E> column) {
			this.columns.add(column);
		}

		/**
		 * order by payload if possible, else by label of the column index
		 */
		public void order() {
			if (columns.size() == 0)
				return;
			E firstobject = columns.get(0).payload;
			if (firstobject instanceof Comparable<?>) {
				Collections.sort(columns, new Comparator<ColumnIndex<E>>() {

					@Override
					public int compare(ColumnIndex<E> o1, ColumnIndex<E> o2) {
						@SuppressWarnings("unchecked")
						Comparable<E> o1comp = (Comparable<E>) o1.payload;
						if (o1comp == null)
							return -1;
						int payloadcomparator = o1comp.compareTo(o2.payload);
						if (payloadcomparator != 0)
							return payloadcomparator;
						if (o1.label == null)
							return -1;
						return o1.label.compareTo(o2.label);

					}
				});
			} else {
				Collections.sort(columns, new Comparator<ColumnIndex<E>>() {
					@Override
					public int compare(ColumnIndex<E> o1, ColumnIndex<E> o2) {
						if (o1.label == null)
							return -1;
						return o1.label.compareTo(o2.label);
					}
				});
			}
		}

	}
}
