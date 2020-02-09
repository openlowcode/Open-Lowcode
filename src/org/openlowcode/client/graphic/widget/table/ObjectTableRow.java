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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.CBusinessField;
import org.openlowcode.client.graphic.widget.CChoiceField;
import org.openlowcode.client.graphic.widget.CDateField;
import org.openlowcode.client.graphic.widget.CDecimalField;
import org.openlowcode.client.graphic.widget.CMultiFieldConstraint;
import org.openlowcode.client.graphic.widget.CMultiFieldObjectAccess;
import org.openlowcode.client.graphic.widget.CObjectArray;
import org.openlowcode.client.graphic.widget.CDecimalField.LockableBigDecimal;
import org.openlowcode.client.graphic.widget.CTextField.OrderableString;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import javafx.scene.control.TableView;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;

/**
 * A wrapper around a data object to be used as row of a CObjectArray
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectTableRow
		implements
		CMultiFieldObjectAccess {
	private ObjectDataElt payload;
	private ObjectDataElt originalpayload;
	private boolean rowupdated;
	private HashMap<String, Boolean> cellupdated;
	private ArrayList<CMultiFieldConstraint> constraints;
	private HashMap<String, ArrayList<String>> activeconstraintsforrow;
	private TableView<ObjectTableRow> tableview;
	private CObjectArray parentarraynode;
	@SuppressWarnings("unused")
	private PageActionManager actionmanager;
	private static Logger logger = Logger.getLogger(ObjectTableRow.class.getName());

	/**
	 * return the restrictions active for the field
	 * 
	 * @param fieldname name of the field
	 * @return possible value considering constraints and other field values
	 */
	public ArrayList<String> hasFieldRestriction(String fieldname) {
		return activeconstraintsforrow.get(fieldname);
	}

	/**
	 * @return true if the row is frozen
	 */
	public boolean isRowFrozen() {
		return this.payload.isFrozen();
	}

	/**
	 * creates an object table row
	 * 
	 * @param payload         object payload
	 * @param constraints     constraints
	 * @param tableview       parent table view
	 * @param parentarraynode parent node
	 * @param actionmanager   action manager of the page
	 */
	public ObjectTableRow(
			ObjectDataElt payload,
			ArrayList<CMultiFieldConstraint> constraints,
			TableView<ObjectTableRow> tableview,
			CObjectArray parentarraynode,
			PageActionManager actionmanager) {
		this.tableview = tableview;
		if (parentarraynode == null)
			throw new RuntimeException("Cannot create a table row with a null parent array node");
		this.parentarraynode = parentarraynode;
		this.payload = payload;
		this.originalpayload = payload.deepcopy();
		this.rowupdated = false;
		cellupdated = new HashMap<String, Boolean>();
		for (int i = 0; i < payload.fieldnumber(); i++) {
			SimpleDataElt field = payload.getField(i);
			cellupdated.put(field.getName(), false);
		}
		this.constraints = constraints;
		this.actionmanager = actionmanager;
		activeconstraintsforrow = new HashMap<String, ArrayList<String>>();
		if (constraints != null)
			for (int i = 0; i < constraints.size(); i++) {
				CMultiFieldConstraint thisconstraint = constraints.get(i);
				for (int j = 0; j < thisconstraint.getConstrainedFieldSize(); j++) {
					CBusinessField<?> thisfield = thisconstraint.getConstrainedField(j);
					if (thisfield instanceof CChoiceField) {
						CChoiceField thischoicefield = (CChoiceField) thisfield;
						SimpleDataElt fieldvalue = payload.lookupEltByName(thischoicefield.getFieldname());
						if (!(fieldvalue instanceof ChoiceDataElt))
							throw new RuntimeException("Expected ChoiceDataElt for field "
									+ thischoicefield.getFieldname() + ", got " + fieldvalue.getClass().toString());
						ChoiceDataElt<?> thischoicevalue = (ChoiceDataElt<?>) fieldvalue;

						if (thischoicevalue.getStoredValue() != null)
							if (thischoicevalue.getStoredValue().length() > 0) {
							}
					}
				}
			}
	}

	/**
	 * get the field type of the given field
	 * 
	 * @param fieldname unique name of the field
	 * @return the data element type
	 */
	public DataEltType lookupFieldType(String fieldname) {
		SimpleDataElt field = payload.lookupEltByName(fieldname);
		if (field == null)
			return null;
		return field.getType();
	}

	/**
	 * @return the object used as payload of the object table ro
	 */
	public ObjectDataElt getObject() {
		return payload;
	}

	/**
	 * @return true if some data was updated
	 */
	public boolean isRowUpdate() {
		return this.rowupdated;
	}

	/**
	 * gets the representation of the payload of a field
	 * 
	 * @param fieldname field name
	 * @return the string representation of the payload
	 */
	public String getFieldRepresentation(String fieldname) {
		SimpleDataElt field = payload.lookupEltByName(fieldname);
		if (field == null)
			return "#ERROR# " + fieldname;
		return field.defaultTextRepresentation();
	}

	/**
	 * gets a clone of one of the fields payload
	 * 
	 * @param fieldname field name
	 * @return payload as simple data element
	 */
	public SimpleDataElt getFieldDataEltClone(String fieldname) {
		SimpleDataElt field = payload.lookupEltByName(fieldname);
		if (field == null)
			return null;
		return field.cloneElt();
	}

	/**
	 * reset all changes and go back to original data
	 */
	public void resetChange() {
		this.payload = originalpayload;
		this.rowupdated = false;
		for (int i = 0; i < payload.fieldnumber(); i++) {
			SimpleDataElt field = payload.getField(i);
			cellupdated.put(field.getName(), false);
		}
	}

	/**
	 * this method manages the update of objects on the table, and stored them. It
	 * now detects if a real change was done.
	 * 
	 * @param fieldname    the field to change
	 * @param fieldpayload the payload changed
	 */
	public void updateField(String fieldname, Object fieldpayload) {

		SimpleDataElt field = payload.lookupEltByName(fieldname);
		boolean treated = false;
		if (field == null)
			throw new RuntimeException("field name '" + fieldname + "' not found for object " + payload.getName()
					+ ", existing field list = " + payload.dropFieldNames());
		// ---------------------------------- treating text field ---------------
		if (field instanceof TextDataElt) {
			TextDataElt textfield = (TextDataElt) field;
			if (!(fieldpayload instanceof OrderableString))
				throw new RuntimeException(
						"expects text payload for field '" + fieldname + ", got " + fieldpayload.getClass().toString());
			String newvalue = ((OrderableString) fieldpayload).getValue();
			textfield.changePayload(newvalue);
			treated = true;
		}
		// ---------------------------------- treating choice field ---------------
		if (field instanceof ChoiceDataElt) {
			@SuppressWarnings("unchecked")
			ChoiceDataElt<CChoiceFieldValue> choicefield = (ChoiceDataElt<CChoiceFieldValue>) field;
			if (fieldpayload == null) {

				choicefield.forceContent(null);
			}
			if (fieldpayload != null)
				if (!(fieldpayload instanceof CChoiceFieldValue))
					throw new RuntimeException("expects choice payload (CChoiceFieldValue) for field '" + fieldname
							+ ", got " + (fieldpayload != null ? fieldpayload.getClass().toString() : "Null Content"));
			choicefield.changePayload((CChoiceFieldValue) fieldpayload);
			// calling back the relevant constraints after a choice
			if (fieldpayload != null)
				for (int i = 0; i < constraints.size(); i++) {
					CMultiFieldConstraint thisconstraint = constraints.get(i);
					if (thisconstraint.isConstraintRelevantForField(fieldname)) {
						StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
						// checking if change as part of multifield constraint. If so, do not trigger
						// the multifield constraints again
						boolean changedaspartoffieldconstraint = false;
						for (int sti = 0; sti < stacktrace.length; sti++)
							if (stacktrace[sti].getClassName().equals(CMultiFieldConstraint.class.getName()))
								changedaspartoffieldconstraint = true;

						if (!changedaspartoffieldconstraint)
							thisconstraint.checkFieldEntry(fieldname,
									((CChoiceFieldValue) fieldpayload).getStorageCode(), this);
						if (changedaspartoffieldconstraint)
							logger.info("discarded change in table row");
					}
				}
			treated = true;
		}
		// ---------------------------------- treating date field ---------------
		if (field instanceof DateDataElt) {
			DateDataElt datefield = (DateDataElt) field;
			if (!(fieldpayload instanceof CDateField.LockableDate))
				throw new RuntimeException("expects date payload  for field '" + fieldname + ", got "
						+ fieldpayload.getClass().toString());
			datefield.updatePayload(((CDateField.LockableDate) fieldpayload).getValue());
			treated = true;
		}
		// ---------------------------------- treating decimal field ---------------
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
				logger.info("Decimal Data Elt is locked " + decimalfield.getName()
						+ ", update not performed for object " + fieldpayload);
				treated = true;
			}
		}
		if (!treated)
			throw new RuntimeException(
					"Payload not supported for field '" + fieldname + ", type " + fieldpayload.getClass().toString());
		cellupdated.put(field.getName(), true);
		if (payload.equals(originalpayload)) {
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
			parentarraynode.reviewDataWarningForTable();
	}

	/**
	 * set a constraint
	 * 
	 * @param fieldname  field name
	 * @param constraint list of potential values for this field
	 */
	public void setConstraint(String fieldname, ArrayList<String> constraint) {
		logger.finer(" --------- setting constraint on field " + fieldname + " with constraint "
				+ (constraint != null ? "length = " + constraint.size() : "null"));
		this.activeconstraintsforrow.put(fieldname, constraint);
		SimpleDataElt element = payload.lookupEltByName(fieldname);
		if (constraint.size() == 1)
			if (constraint.get(0) != null) {
				logger.finer("trying to set value on previous field " + element.getName() + " to value "
						+ constraint.get(0));
				element.forceContent(constraint.get(0));
				this.refreshTable();
			}
		if (constraint.size() > 0) {
			boolean currentvaluevalid = false;
			for (int i = 0; i < constraint.size(); i++) {
				if (constraint.get(i).equals(element.defaultTextRepresentation()))
					currentvaluevalid = true;
			}
			if (!currentvaluevalid)
				element.forceContent(null);
		}
		if (element instanceof ChoiceDataElt) {
			ChoiceDataElt<?> choiceelement = (ChoiceDataElt<?>) element;
			choiceelement.defineRestriction(constraint);
		}

	}

	@Override
	public boolean setConstraint(String fieldname, ArrayList<String> restrainedvalues, String selected) {
		logger.finer("   -**- data model update - set value for " + fieldname + ", selected = " + selected + ", "
				+ (restrainedvalues == null ? "null" : restrainedvalues.size() + " elements"));
		boolean forcedecimal = false;
		this.activeconstraintsforrow.put(fieldname, restrainedvalues);
		SimpleDataElt element = payload.lookupEltByName(fieldname);
		boolean currenterased = false;
		if (element instanceof ChoiceDataElt) {
			ChoiceDataElt<?> choiceelement = (ChoiceDataElt<?>) element;
			currenterased = choiceelement.defineRestriction(restrainedvalues);
			if (restrainedvalues.size() == 1) {
				boolean hasvalue = true;
				if (restrainedvalues.get(0) == null)
					hasvalue = false;
				if (restrainedvalues.get(0) != null)
					if (restrainedvalues.get(0).length() == 0)
						hasvalue = false;
				if (hasvalue) {
					choiceelement.forceContent(restrainedvalues.get(0));
					this.refreshTable();
				}
			}
		}

		if (element instanceof DecimalDataElt) {
			DecimalDataElt decimalelement = (DecimalDataElt) element;
			if (restrainedvalues == null) {
				decimalelement.unlockValue();
			} else {
				if (restrainedvalues.size() == 0)
					decimalelement.unlockValue();
				if (restrainedvalues.size() == 1) {
					boolean hasvalue = true;
					if (restrainedvalues.get(0) == null)
						hasvalue = false;
					if (restrainedvalues.get(0) != null)
						if (restrainedvalues.get(0).length() == 0)
							hasvalue = false;
					if (hasvalue) {
						forcedecimal = true;
						decimalelement.lockToValue(restrainedvalues.get(0));
						logger.finer("     **---->>> lock to Value " + restrainedvalues.get(0) + " for decimalelement "
								+ decimalelement.getName() + " lock after = " + decimalelement.islocked()
								+ " instance name = " + Integer.toHexString(decimalelement.hashCode()));

						this.refreshTable();
					} else {
						decimalelement.unlockValue();
					}
				}
				if (restrainedvalues.size() > 1) {
					decimalelement.forceContent("");
					decimalelement.unlockValue();
				}
			}
		}

		if (selected != null)
			if (selected.length() > 0) {
				element.forceContent(selected);
				this.refreshTable();
				return false;

			}
		if (selected != null)
			if (selected.length() == 0) {
				// hack to force decimal to be filled if there is only one value proposed.
				if (!forcedecimal)
					element.forceContent(null);
				return true;
			}
		// only code for selected = null
		if (currenterased) {
			this.refreshTable();
			return true;
		}
		return false;
	}

	@Override
	public String getFieldValueForConstraint(String fieldname) {

		SimpleDataElt data = this.payload.lookupEltByName(fieldname);
		if (data == null)
			throw new RuntimeException("data element does not exist, name = " + fieldname);
		logger.finer(
				"   -**- data model update - get value for " + fieldname + " : " + data.defaultTextRepresentation());
		return data.defaultTextRepresentation();
	}

	@Override
	public void liftConstraint(String fieldname) {
		logger.finer("   -**- data model update - lift constraint on " + fieldname);
		SimpleDataElt data = this.payload.lookupEltByName(fieldname);
		if (data == null)
			throw new RuntimeException("data element does not exist, name = " + fieldname);
		boolean treated = false;
		if (data instanceof ChoiceDataElt) {
			ChoiceDataElt<?> choiceelt = (ChoiceDataElt<?>) data;
			choiceelt.defineRestriction(null);
			this.refreshTable();
			treated = true;
		}
		if (data instanceof DecimalDataElt) {
			DecimalDataElt decimalelt = (DecimalDataElt) data;

			decimalelt.unlockValue();
			this.refreshTable();
			treated = true;
		}
		if (!treated)
			throw new RuntimeException("field of supported type not found, field name = " + fieldname);
	}

	/**
	 * This is a hack for versions before 1.8u60.
	 */
	public void refreshTable() {
		this.tableview.getProperties().put(TableViewSkinBase.RECREATE, Boolean.TRUE);
	}

	/**
	 * removes all references inside this object table row to facilitate garbage
	 * collecting. Was added after identified memory leak and contributed to solve
	 * the memory leak
	 */
	public void mothball() {
		this.tableview = null;
		this.parentarraynode = null;
		this.actionmanager = null;

	}
}
