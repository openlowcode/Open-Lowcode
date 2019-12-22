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

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;

/**
 * the parent class to all auto-generated multi-field constraints
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SMultiFieldConstraint extends Named {
	private static Logger logger = Logger.getLogger(SMultiFieldConstraint.class.getName());
	private ArrayList<String> fieldsequence;
	private ArrayList<ArrayList<String>> fieldvalues;

	/**
	 * @return the list of fields managed in this multi-field constraint
	 */
	public ArrayList<String> getFieldSequence() {
		return fieldsequence;
	}

	/**
	 * @return the list of authorized combinations for the field values
	 */
	public ArrayList<ArrayList<String>> getFieldValues() {
		return fieldvalues;
	}

	/**
	 * @param fieldname adds a field as part of this constraint
	 */
	public void addField(String fieldname) {
		logger.finer("add field " + fieldname + " for sequence " + fieldsequence.size());
		this.fieldsequence.add(fieldname);
	}

	/**
	 * @param onelineconstraint adds one authorized combination on the defined
	 *                          fields
	 */
	public void addOneLineOfConstraint(ArrayList<String> onelineconstraint) {
		if (onelineconstraint.size() != fieldsequence.size())
			throw new RuntimeException(
					"field values should be size " + fieldsequence.size() + " and is now " + onelineconstraint.size());
		this.fieldvalues.add(onelineconstraint);
	}

	/**
	 * Creates a new SMultiFieldConstraint
	 * 
	 * @param name the name of the multi-field constraint. It needs to be unique for
	 *             the module
	 */
	public SMultiFieldConstraint(String name) {
		super(name);
		this.fieldsequence = new ArrayList<String>();
		this.fieldvalues = new ArrayList<ArrayList<String>>();
	}

	/**
	 * writes the payload to a message
	 * 
	 * @param writer writer for the message
	 * @throws IOException if any error happens during the transmission of the
	 *                     message
	 */
	public void writeToCML(MessageWriter writer) throws IOException {

		writer.startStructure("FLDS");
		for (int i = 0; i < fieldsequence.size(); i++) {

			writer.startStructure("FLD");
			logger.finer("writing to CML " + i + "fieldsequence = " + fieldsequence.get(i));
			writer.addStringField("NAM", fieldsequence.get(i));
			writer.endStructure("FLD");
		}
		writer.endStructure("FLDS");
		writer.startStructure("VALS");

		for (int i = 0; i < fieldvalues.size(); i++) {
			writer.startStructure("VAL");
			writer.startStructure("VALELTS");
			ArrayList<String> line = fieldvalues.get(i);
			for (int j = 0; j < line.size(); j++) {
				writer.startStructure("VALELT");
				writer.addStringField("ELT", line.get(j));
				writer.endStructure("VALELT");
			}
			writer.endStructure("VALELTS");
			writer.endStructure("VAL");
		}
		writer.endStructure("VALS");

	}

	/**
	 * checks if the combination is valid
	 * 
	 * @param thisobjectvalues a set of values for the different fields
	 */
	public void checkCombination(ArrayList<String> thisobjectvalues) {
		for (int i = 0; i < this.fieldvalues.size(); i++) {
			ArrayList<String> reference = this.fieldvalues.get(i);
			if (reference.size() != thisobjectvalues.size())
				throw new RuntimeException("Comparing value sets of different sizes (" + thisobjectvalues.size() + "!="
						+ thisobjectvalues.size());
			boolean lineok = true;
			for (int j = 0; j < reference.size(); j++) {
				if (thisobjectvalues.get(j) != null)
					if (thisobjectvalues.get(j).length() > 0) {
						if (!thisobjectvalues.get(j).equals(reference.get(j)))
							lineok = false;
					}
			}
			if (lineok)
				return;
		}
		// -- build and return error message
		StringBuffer dropvalues = new StringBuffer("(");
		for (int i = 0; i < thisobjectvalues.size(); i++) {
			if (i > 0)
				dropvalues.append(",");
			dropvalues.append(this.fieldsequence.get(i));
			dropvalues.append("=");
			dropvalues.append(thisobjectvalues.get(i));
		}
		dropvalues.append(")");
		throw new RuntimeException("Combination not allowed " + dropvalues);

	}
}
