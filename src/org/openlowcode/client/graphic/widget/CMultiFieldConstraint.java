/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * A multi-field constraint used in the client. It provides a list of valid
 * combinations across the fields that are part of the constraint
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CMultiFieldConstraint
		implements
		CMultiFieldObjectAccess {
	private static Logger logger = Logger.getLogger(CMultiFieldConstraint.class.getName());
	private ArrayList<CBusinessField<?>> constrainedfields;
	private ArrayList<ArrayList<String>> allowedcombinations;

	private CBusinessField<?> lookupfieldbyname(ArrayList<CBusinessField<?>> fieldlist, String name) {
		for (int i = 0; i < fieldlist.size(); i++) {
			if (fieldlist.get(i).getFieldname().compareTo(name) == 0) {
				return fieldlist.get(i);

			}
		}
		return null;
	}

	/**
	 * tells if the constraint is relevant for the given field
	 * 
	 * @param fieldname the field java name
	 * @return true if the constraint is relevant for the field
	 */
	public boolean isConstraintRelevantForField(String fieldname) {
		for (int i = 0; i < constrainedfields.size(); i++) {
			if (constrainedfields.get(i).getFieldname().equals(fieldname))
				return true;

		}
		return false;
	}

	/**
	 * @return the number of fields constrained
	 */
	public int getConstrainedFieldSize() {
		return constrainedfields.size();
	}

	/**
	 * gets a constrained field
	 * 
	 * @param index a number between 0 (included) and getConstrainedFieldSize()
	 *              (excluded)
	 * @return the business field
	 */
	public CBusinessField<?> getConstrainedField(int index) {
		return constrainedfields.get(index);
	}

	/**
	 * creaes a multiple field constraint from the server message
	 * 
	 * @param reader      message reader from the server
	 * @param payloadlist list of fields in the object
	 * @throws OLcRemoteException if anything bad happens on the server during the
	 *                            message transmission
	 * @throws IOException        if any transmission error happens for the message
	 */
	public CMultiFieldConstraint(MessageReader reader, ArrayList<CBusinessField<?>> payloadlist)
			throws OLcRemoteException, IOException {
		constrainedfields = new ArrayList<CBusinessField<?>>();
		allowedcombinations = new ArrayList<ArrayList<String>>();
		// ------------------- lookup constrained field ---------------------

		reader.startStructureArray("FLD");
		while (reader.structureArrayHasNextElement("FLD")) {
			String fieldname = reader.returnNextStringField("NAM");
			reader.returnNextEndStructure("FLD");
			CBusinessField<?> field = lookupfieldbyname(payloadlist, fieldname);
			if (field == null)
				throw new RuntimeException(
						"In multifield constraint, field with unknown name " + fieldname + " is referenced");
			field.addConstraintCallBack(this);
			constrainedfields.add(field);

		}

		// ------------------- load constraint lines
		// ------------------------------------
		reader.startStructureArray("VAL");
		while (reader.structureArrayHasNextElement("VAL")) {
			ArrayList<String> combination = new ArrayList<String>();
			reader.startStructureArray("VALELT");
			while (reader.structureArrayHasNextElement("VALELT")) {
				int index = combination.size();
				String value = reader.returnNextStringField("ELT");
				if (index >= constrainedfields.size())
					throw new RuntimeException("value element is index " + index + " but there are only "
							+ constrainedfields.size() + " constrained values.");
				if (!constrainedfields.get(index).isRestrictionValid(value))
					throw new RuntimeException("restriction is not valid " + value);
				combination.add(value);
				reader.returnNextEndStructure("VALELT");
			}
			reader.returnNextEndStructure("VAL");
			allowedcombinations.add(combination);
		}
		reader.returnNextEndStructure("CTR");
	}

	/**
	 * checks that a field entry is valid
	 * 
	 * @param fieldname   java name of the field
	 * @param storedvalue stored value for the field
	 */
	public void checkFieldEntry(String fieldname, String storedvalue) {
		this.checkFieldEntry(fieldname, storedvalue, this);
	}

	/**
	 * checks field entry
	 * 
	 * @param fieldname     name of the field
	 * @param storedvalue   stored value
	 * @param accessgateway a multifield object access (can be this constraint, or
	 *                      an object table row)
	 */
	public void checkFieldEntry(String fieldname, String storedvalue, CMultiFieldObjectAccess accessgateway) {
		CBusinessField<?> choicefield = this.lookupfieldbyname(this.constrainedfields, fieldname);
		int currententryindex = checkfieldindex(choicefield);
		logger.finer(" --------------- check field " + (choicefield != null ? choicefield.getLabel() : "null")
				+ " with index = " + currententryindex + " total fields in constraint =  " + constrainedfields.size());
		for (int i = 0; i < constrainedfields.size(); i++) {
			logger.finer(" drop field list index = " + i + " field name = " + constrainedfields.get(i).getFieldname()
					+ " - " + constrainedfields.get(i).getLabel());
		}

		// manage before: check if OK
		ArrayList<String> combinationtocheck = new ArrayList<String>();
		String combinationlog = "[";
		for (int i = 0; i < currententryindex; i++) {
			String currententryvalue = accessgateway
					.getFieldValueForConstraint(constrainedfields.get(i).getFieldname());
			combinationtocheck.add(currententryvalue);
			combinationlog += (i == 0 ? "" : ",") + currententryvalue;
		}
		combinationtocheck.add(storedvalue);
		combinationlog += "," + storedvalue;
		logger.finer("checking combination " + combinationlog + "], size = " + combinationtocheck.size());
		if (combinationtocheck.size() > 1)
			checkifValid(combinationtocheck);
		logger.finer("combination " + combinationlog + "] is valid, now checking if previous fields can be preset");
		setupPreviousFields(combinationtocheck, accessgateway);

		logger.finer("now managing further fields - setting constraints");
		// manage after : restrain fields
		boolean forcetoblank = false;
		for (int i = currententryindex + 1; i < constrainedfields.size(); i++) {
			CBusinessField<?> thisfield = constrainedfields.get(i);
			logger.finer("Analyzing constraints on field " + thisfield + " with index = " + i);
			ArrayList<String> restrainedvalues = findrestrainedvalue(i, combinationtocheck);
			logger.finer("restrainedvalue size = " + restrainedvalues.size());
			String restrainedvaluedrop = "[";
			for (int j = 0; j < restrainedvalues.size(); j++)
				restrainedvaluedrop += "," + restrainedvalues.get(j);
			logger.finer("putting constraint on field " + thisfield.getLabel() + " " + restrainedvaluedrop + "]");
			if (restrainedvalues.size() > 0) {
				boolean blank = accessgateway.setConstraint(thisfield.getFieldname(), restrainedvalues,
						(forcetoblank ? "" : null));
				if (blank)
					forcetoblank = true;
			}
			if (restrainedvalues.size() == 0)
				accessgateway.liftConstraint(thisfield.getFieldname());
		}
	}

	/**
	 * @param combinationtocheck the combination of set fields from the first field
	 *                           of combination to the field entered, null if
	 *                           nothing if specified
	 */
	private void setupPreviousFields(ArrayList<String> combinationtocheck, CMultiFieldObjectAccess accessgateway) {
		// perform the algorithm for all fields before the field added. The potential
		// values
		// and selected values are reset

		for (int i = 0; i < combinationtocheck.size() - 1; i++) {

			// determining the list of combinations allowables for fields before the current
			// field.
			// This will determine the possible combinations for this field.
			ArrayList<String> previousconstraints = new ArrayList<String>();
			for (int j = 0; j < i; j++)
				previousconstraints.add(combinationtocheck.get(j));

			// possible combinations for the field
			ArrayList<String> allowedcombination = this.findrestrainedvalue(i, previousconstraints);
			String forcevalue = null;
			boolean hasvalue = true;
			if (combinationtocheck.get(i) == null)
				hasvalue = false;
			if (combinationtocheck.get(i) != null)
				if (combinationtocheck.get(i).length() == 0)
					hasvalue = false;

			if (!hasvalue) {
				// find number of valid combinations that specify this field with all set fields
				HashMap<String, String> potentialvalues = new HashMap<String, String>();
				for (int j = 0; j < allowedcombinations.size(); j++) {
					ArrayList<String> thiscombination = allowedcombinations.get(j);
					boolean correct = true;
					for (int k = 0; k < combinationtocheck.size(); k++) {
						String thisvalueincombination = thiscombination.get(k);
						String thiscombinationtocheck = combinationtocheck.get(k);
						if (thiscombinationtocheck != null)
							if (thiscombinationtocheck.length() > 0)
								if (thisvalueincombination != null)
									if (thisvalueincombination.length() > 0) {

										if (!thisvalueincombination.equals(thiscombinationtocheck))
											correct = false;
									}
					}

					if (correct)
						potentialvalues.put(thiscombination.get(i), thiscombination.get(i));
				} // end of loop on allowed combinations
				logger.fine("		*-* potential values for index = " + i + " size = " + potentialvalues.size());

				if (potentialvalues.size() == 1)
					if (potentialvalues.get(potentialvalues.keySet().iterator().next()) != null) {
						// found one unique combination for previous field
						ArrayList<String> constraint = new ArrayList<String>();
						constraint.add(potentialvalues.get(potentialvalues.keySet().iterator().next()));
						// BUG this is not correct, the constraint should be only determined by parent
						// the filled value however should come from children
						forcevalue = potentialvalues.get(potentialvalues.keySet().iterator().next());
						combinationtocheck.set(i, potentialvalues.get(potentialvalues.keySet().iterator().next()));
					}
				String potentialvaluesdump = "[";
				Iterator<String> iterator = potentialvalues.keySet().iterator();
				while (iterator.hasNext()) {
					potentialvaluesdump += iterator.next() + "|";
				}
				logger.fine("       *-* nb of potential values = " + potentialvalues.size() + ", dump = "
						+ potentialvaluesdump);
			} else {
				logger.fine("       *-* not checking potential values for field combination index = " + i
						+ " as value already exists [" + combinationtocheck.get(i) + "]");
				// end of field not filled
			}
			CBusinessField<?> thisfield = this.constrainedfields.get(i);
			logger.finer(" * preset empty previous field " + thisfield.getFieldname() + " to " + forcevalue
					+ " with potential choices length = " + allowedcombination.size());

			accessgateway.setConstraint(thisfield.getFieldname(), allowedcombination, forcevalue);
		}

	}

	private ArrayList<String> findrestrainedvalue(int columnindex, ArrayList<String> combinationtocheck) {
		HashMap<String, String> restrictions = new HashMap<String, String>();
		for (int i = 0; i < allowedcombinations.size(); i++) {
			ArrayList<String> thiscombinationline = allowedcombinations.get(i);
			boolean relevantline = true;
			// --- check if combination valid
			for (int j = 0; j < combinationtocheck.size(); j++) {
				if (thiscombinationline.get(j) != null) {
					if (!thiscombinationline.get(j).equals(combinationtocheck.get(j)))
						relevantline = false;
				}

			}
			if (relevantline) {
				String relevantcolumnvalue = thiscombinationline.get(columnindex);
				if (relevantcolumnvalue == null)
					return new ArrayList<String>(); // valid null value, everything is allowed;
				restrictions.put(relevantcolumnvalue, relevantcolumnvalue);
			}
		}
		ArrayList<String> answer = new ArrayList<String>();
		Iterator<String> restrictionsiterator = restrictions.values().iterator();
		while (restrictionsiterator.hasNext())
			answer.add(restrictionsiterator.next());
		return answer;
	}

	private void checkifValid(ArrayList<String> combinationtocheck) {
		for (int i = 0; i < allowedcombinations.size(); i++) {
			ArrayList<String> thiscombination = allowedcombinations.get(i);
			boolean correct = true;
			for (int j = 0; j < combinationtocheck.size(); j++) {
				String thisvalueincombination = thiscombination.get(j);
				String thiscombinationtocheck = combinationtocheck.get(j);
				if (thiscombinationtocheck != null)
					if (thiscombinationtocheck.length() > 0)
						if (thisvalueincombination != null)
							if (thisvalueincombination.length() > 0) {

								if (!thisvalueincombination.equals(thiscombinationtocheck))
									correct = false;
							}
			}
			if (correct)
				return;
		}
		throw new RuntimeException("no combination found after trying " + allowedcombinations.size());
	}

	private int checkfieldindex(CBusinessField<?> businessfield) {
		for (int i = 0; i < constrainedfields.size(); i++) {
			if (businessfield.equals(constrainedfields.get(i)))
				return i;
		}
		throw new RuntimeException("field not found in constraint " + businessfield);
	}

	@Override
	public boolean setConstraint(String fieldname, ArrayList<String> restrainedvalues, String selected)
			throws RuntimeException {
		logger.finer("   -**- data model update - set value for " + fieldname + ", selected = " + selected + ", "
				+ (restrainedvalues == null ? "null" : restrainedvalues.size() + " elements"));

		CBusinessField<?> field = this.lookupfieldbyname(constrainedfields, fieldname);
		return field.setConstraint(restrainedvalues, selected);
	}

	@Override
	public String getFieldValueForConstraint(String fieldname) {
		return this.lookupfieldbyname(constrainedfields, fieldname).getValueForConstraint();
	}

	@Override
	public void liftConstraint(String fieldname) {
		this.lookupfieldbyname(constrainedfields, fieldname).liftConstraint();

	}
}
