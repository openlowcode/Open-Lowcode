/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.CDateField;
import org.openlowcode.client.graphic.widget.CDecimalField;
import org.openlowcode.client.graphic.widget.CGrid;
import org.openlowcode.client.graphic.widget.CDecimalField.LockableBigDecimal;
import org.openlowcode.client.graphic.widget.CTextField.OrderableString;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;

/**
 * An object grid line
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectGridLine<E extends Comparable<E>>
		extends
		Named
		implements
		Comparable<CObjectGridLine<E>> {
	private static Logger logger = Logger.getLogger(CObjectGridLine.class.getName());
	private NamedList<ObjectInGrid> linedata;
	private E codetoorder = null;
	private String originallinelabel = null;
	private E linelabelobject;
	private CGrid parentarraynode;

	/**
	 * @return the label object
	 */
	public E getLabelObject() {
		return this.linelabelobject;
	}

	/**
	 * get the object for the selected column
	 * 
	 * @param dataname column unique name
	 * @return objet in the grid
	 */
	public ObjectInGrid getObjectInGrid(String dataname) {
		return linedata.lookupOnName(Named.cleanName(dataname));
	}

	/**
	 * get the object for the selected combination of primary data name and
	 * secondary data name
	 * 
	 * @param primarydataname   primary column name
	 * @param secondarydataname secondary column name
	 * @return the object at the selected column
	 */
	public ObjectInGrid getObjectInGrid(String primarydataname, String secondarydataname) {
		return linedata.lookupOnName(Named.cleanName(buildtwofieldscolumnindex(primarydataname, secondarydataname)));
	}

	/**
	 * @return the number of objects
	 */
	public int getObjectinlineNumber() {
		return linedata.getSize();
	}

	/**
	 * get the object in line according to the given index
	 * 
	 * @param index a number between 0 (included) and getObjectinlineNumber
	 *              (excluded)
	 * @return the object
	 */
	public ObjectInGrid getObjectinline(int index) {
		return linedata.get(index);
	}

	/**
	 * @return the line label
	 */
	public String getLineLabel() {
		return originallinelabel;
	}

	/**
	 * get the value for the given data
	 * 
	 * @param dataname   name of the data
	 * @param valueindex index of the value to show
	 * @return string payload to show in grid
	 */
	public String getValueForData(String dataname, int valueindex) {
		ObjectInGrid objectingrid = linedata.lookupOnName(Named.cleanName(dataname));

		if (objectingrid != null)
			return objectingrid.valuetoshow[valueindex];

		logger.warning("No value for dataname = " + dataname + ", availabledata = " + linedata.dropNameList());
		return "no value";
	}

	/**
	 * creates a line in an object grid with a code for ordering the lins
	 * 
	 * @param parentarraynode parent grid widget
	 * @param linelabel       label of the line
	 * @param codetoorder     code used to order the line
	 */
	public CObjectGridLine(CGrid parentarraynode, E linelabel, Comparable<E> codetoorder) {
		super(linelabel.toString());
		this.originallinelabel = linelabel.toString();
		this.linelabelobject = linelabel;
		linedata = new NamedList<ObjectInGrid>();
		this.parentarraynode = parentarraynode;
	}

	@SuppressWarnings("unchecked")
	public E getCodeToOrder() {
		if (this.codetoorder != null)
			return this.codetoorder;
		return (E) this.getName();
	}

	/**
	 * creates a line in an object grid without any special ordering
	 * 
	 * @param parentarraynode parent grid widget
	 * @param linelabel       label of the line
	 */
	public CObjectGridLine(CGrid parentarraynode, E linelabel) {
		this(parentarraynode, linelabel, null);
	}

	/**
	 * @param columnvalue
	 * @param valuetoshow
	 * @param thisline
	 */
	public void addObject(String columnvalue, String[] valuetoshow, String[] valuelabel, ObjectDataElt thisline) {
		linedata.addIfNew(new ObjectInGrid(parentarraynode, thisline, columnvalue, valuetoshow, valuelabel));

	}

	/**
	 * adds an object inside this grid line
	 * 
	 * @param columnvalue          value of the primary column marker
	 * @param secondarycolumnvalue value of the secondary column marker (if it
	 *                             exists)
	 * @param valuetoshow          the list of values to show
	 * @param valuelabel           the list of labels for values to show
	 * @param thisline             object to add
	 */
	public void addObject(
			String columnvalue,
			String secondarycolumnvalue,
			String[] valuetoshow,
			String[] valuelabel,
			ObjectDataElt thisline) {
		linedata.addIfNew(new ObjectInGrid(parentarraynode, thisline,
				buildtwofieldscolumnindex(columnvalue, secondarycolumnvalue), valuetoshow, valuelabel));

	}

	/**
	 * builds a column index with two fields, using ':' as separator and escaping
	 * also the potential ':' inside each field
	 * 
	 * @param columnvalue          primary column value
	 * @param secondarycolumnvalue secondary column value
	 * @return a string index
	 */
	public static String buildtwofieldscolumnindex(String columnvalue, String secondarycolumnvalue) {
		return "" + (columnvalue != null ? columnvalue.replaceAll(":", "::") : "") + ":"
				+ (secondarycolumnvalue != null ? secondarycolumnvalue.replaceAll(":", "::") : "");
	}

	/**
	 * build a column index with one field
	 * 
	 * @param columnvalue primary column value
	 * @return a string index
	 */
	public ObjectDataElt getObjectForColumn(String columnvalue) {
		ObjectInGrid columnobject = linedata.lookupOnName(Named.cleanName(columnvalue));

		if (columnobject != null)
			return columnobject.object;
		throw new RuntimeException("Did not find object for column value = '" + columnvalue + "' for line = "
				+ this.originallinelabel + ", available keys = " + linedata.dropNameList());
	}

	/**
	 * get the object for the provided column indicator
	 * 
	 * @param columnvalue          primary column value
	 * @param secondarycolumnvalue secondary column value
	 * @return the object for the corresponding columns
	 */
	public ObjectDataElt getObjectForColumn(String columnvalue, String secondarycolumnvalue) {
		String searchkey = Named.cleanName(buildtwofieldscolumnindex(columnvalue, secondarycolumnvalue));
		ObjectInGrid columnobject = linedata.lookupOnName(searchkey);

		if (columnobject != null)
			return columnobject.object;
		throw new RuntimeException("Did not find object for search key = '" + searchkey + "' for line = "
				+ this.originallinelabel + ", available keys = " + linedata.dropNameList());
	}

	/**
	 * @return true if the row was updated
	 */
	public boolean isRowUpdate() {
		List<ObjectInGrid> array = linedata.getFullList();
		if (array == null)
			return false;
		for (int i = 0; i < array.size(); i++) {
			if (array.get(i).isRowUpdated())
				return true;
		}
		return false;
	}

	/**
	 * ObjectInGrid stores the object behind a cell or group cell of the grid
	 * object.
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class ObjectInGrid
			extends
			Named {
		private ObjectDataElt object;
		private ObjectDataElt originalobject;
		private String valuetoshow[];
		private boolean rowupdated;
		private HashMap<String, Boolean> cellupdated;
		private CGrid parentarraynode;

		/**
		 * @return the object
		 */
		public ObjectDataElt getObject() {
			return object;
		}

		/**
		 * set the object as baseline. Object is considered not updated
		 * 
		 * @param object object
		 */
		public void forceUpdatedObject(ObjectDataElt object) {
			this.object = object;
			this.originalobject = object;
			this.rowupdated = false;
		}

		/**
		 * creates an object in grid
		 * 
		 * @param parentarraynode parent grid
		 * @param object          object payload
		 * @param columnlabel     column label (for grid with a single column)
		 * @param valuetoshow     values to show
		 * @param valuelabel      labels of values to show
		 */
		public ObjectInGrid(
				CGrid parentarraynode,
				ObjectDataElt object,
				String columnlabel,
				String[] valuetoshow,
				String[] valuelabel) {
			super(columnlabel);
			this.object = object;
			this.originalobject = object.deepcopy();
			logger.finest("   --> object  in grid " + object.lookupEltByName("ID") + " - "
					+ object.lookupEltByName("YEARALLOCATED"));
			logger.finest("   --> original object  in grid " + originalobject.lookupEltByName("ID") + " - "
					+ originalobject.lookupEltByName("YEARALLOCATED"));

			this.valuetoshow = valuetoshow;
			this.rowupdated = false;
			cellupdated = new HashMap<String, Boolean>();
			for (int i = 0; i < object.fieldnumber(); i++) {
				SimpleDataElt field = object.getField(i);
				cellupdated.put(field.getName(), false);
			}
			this.parentarraynode = parentarraynode;
		}

		/**
		 * gets a clone of the given field of hte object
		 * 
		 * @param fieldname name of the field (unique java field name)
		 * @return clone of the element
		 */
		public SimpleDataElt getFieldDataEltClone(String fieldname) {
			SimpleDataElt field = object.lookupEltByName(fieldname);
			if (field == null)
				return null;
			return field.cloneElt();
		}

		/**
		 * @return true if the row is frozen
		 */
		public boolean isRowFrozen() {
			return object.isFrozen();
		}

		/**
		 * @return true if the row is updated
		 */
		public boolean isRowUpdated() {
			return this.rowupdated;
		}

		/**
		 * updates the object
		 * 
		 * @param fieldname    name of the field
		 * @param fieldpayload payload of the field
		 */
		public void updateField(String fieldname, Object fieldpayload) {
			logger.finest("Update field " + fieldname + " - payload " + fieldpayload);
			SimpleDataElt field = object.lookupEltByName(fieldname);
			SimpleDataElt faultyfieldtemp = object.lookupEltByName("YEARALLOCATED");
			if (faultyfieldtemp != null)
				logger.finest("YEARALLOCATED = " + faultyfieldtemp.toString());
			SimpleDataElt faultyfieldtempid = object.lookupEltByName("ID");
			if (faultyfieldtemp != null)
				logger.finest("ID = " + faultyfieldtempid.toString());
			boolean treated = false;
			if (field == null)
				throw new RuntimeException("field name '" + fieldname + "' not found for object " + object.getName()
						+ ", existing field list = " + object.dropFieldNames());
			if (field instanceof TextDataElt) {
				TextDataElt textfield = (TextDataElt) field;
				if (!(fieldpayload instanceof OrderableString))
					throw new RuntimeException("expects text payload for field '" + fieldname + ", got "
							+ fieldpayload.getClass().toString());
				textfield.changePayload(((OrderableString) fieldpayload).getValue());
				treated = true;
			}
			if (field instanceof ChoiceDataElt) {
				@SuppressWarnings("unchecked")
				ChoiceDataElt<CChoiceFieldValue> choicefield = (ChoiceDataElt<CChoiceFieldValue>) field;
				if (fieldpayload == null) {
					choicefield.forceContent(null);
				}
				if (fieldpayload != null)
					if (!(fieldpayload instanceof CChoiceFieldValue))
						throw new RuntimeException(
								"expects choice payload (CChoiceFieldValue) for field '" + fieldname + ", got "
										+ (fieldpayload != null ? fieldpayload.getClass().toString() : "Null Content"));
				choicefield.changePayload((CChoiceFieldValue) fieldpayload);

				treated = true;
			}
			if (field instanceof DateDataElt) {
				DateDataElt datefield = (DateDataElt) field;
				if (!(fieldpayload instanceof CDateField.LockableDate))
					throw new RuntimeException("expects date payload  for field '" + fieldname + ", got "
							+ fieldpayload.getClass().toString());
				datefield.updatePayload(((CDateField.LockableDate) fieldpayload).getValue());
				treated = true;
			}
			if (field instanceof DecimalDataElt) {
				DecimalDataElt decimalfield = (DecimalDataElt) field;
				if (!decimalfield.islocked()) {
					if (fieldpayload == null)
						decimalfield.updatePayload(null);
					if (fieldpayload != null)
						if (!(fieldpayload instanceof CDecimalField.LockableBigDecimal))
							throw new RuntimeException("expects bigdecimal payload  for field '" + fieldname + ", got "
									+ fieldpayload.getClass().toString());
					LockableBigDecimal bigdecimal = (LockableBigDecimal) fieldpayload;
					decimalfield.updatePayload(bigdecimal.getValue());
					decimalfield.setLocked(bigdecimal.isLocked());

					treated = true;
				} else {
					logger.fine("Decimal Data Elt is locked " + decimalfield.getName()
							+ ", update not performed for object " + fieldpayload);
					treated = true;
				}
			}
			if (!treated)
				throw new RuntimeException("Payload not supported for field '" + fieldname + ", type "
						+ fieldpayload.getClass().toString());
			cellupdated.put(field.getName(), true);
			if (object.equals(originalobject)) {
				logger.fine("           - original payload is still equal to payload");
				this.rowupdated = false;
			} else {
				logger.fine("           - original payload is different from payload");

				this.rowupdated = true;
			}
			// check on null is because he method is sometimes triggered after mothball.
			// This is probably
			// when a click is made somewhere else while editing and the focus is actually
			// lost during the
			// page mothballing
			if (parentarraynode != null)
				parentarraynode.reviewDataWarningForGrid();
		}

		/**
		 * reset the update flag
		 */
		public void resetUpdateFlag() {
			this.rowupdated = false;
			cellupdated = new HashMap<String, Boolean>();
			for (int i = 0; i < object.fieldnumber(); i++) {
				SimpleDataElt field = object.getField(i);
				cellupdated.put(field.getName(), false);
			}
			originalobject = object.deepcopy();
		}

	}

	@Override
	public int compareTo(CObjectGridLine<E> otherline) {
		return this.getCodeToOrder().compareTo(otherline.getCodeToOrder());
	}

	/**
	 * mothball - does not do anyting on this widget
	 */
	public void mothball() {

	}

	/**
	 * reset the update flag
	 */
	public void resetUpdateFlag() {
		for (int i = 0; i < this.linedata.getSize(); i++) {
			ObjectInGrid thisobject = this.linedata.get(i);
			thisobject.resetUpdateFlag();
		}

	}

}
