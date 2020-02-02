/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

import org.openlowcode.tools.misc.NamedList;

/**
 * A simple choice category is typically used to define a
 * {@link org.openlowcode.design.data.ChoiceField} in a data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleChoiceCategory
		extends
		ChoiceCategory {
	private NamedList<ChoiceValue> values;

	/**
	 * @param name             has to be a unique name inside the package. It is
	 *                         recommended to only put upper case and numbers. The
	 *                         name should not start by a number.
	 * @param keystoragelength the storage length of the key in the database.
	 *                         Storage code of {link
	 *                         org.openlowcode.design.data.ChoiceValue} should be
	 *                         smaller than the storage length of the key.
	 */
	public SimpleChoiceCategory(String name, int keystoragelength) {
		super(name, keystoragelength);
		values = new NamedList<ChoiceValue>();

	}

	/**
	 * @param name             has to be a unique name inside the package. It is
	 *                         recommended to only put upper case and numbers. The
	 *                         name should not start by a number.
	 * @param keystoragelength the storage length of the key in the database.
	 *                         Storage code of {link
	 *                         org.openlowcode.design.data.ChoiceValue} should be
	 * @param pseudonumber     true if all the values can be translated to a number,
	 *                         either by specific setting or transforming the
	 *                         display into an integer smaller than the storage
	 *                         length of the key.
	 * 
	 */
	public SimpleChoiceCategory(String name, int keystoragelength, boolean pseudonumber) {
		super(name, keystoragelength, pseudonumber);
		values = new NamedList<ChoiceValue>();

	}

	/**
	 * adds a value to the choice category. THis is called during the definition of
	 * the choice value
	 * 
	 * @param value value to add
	 */
	public void addValue(ChoiceValue value) {
		if (value.getName().length() > this.getKeyStorageLength())
			throw new RuntimeException("key '" + value.getName() + "' is too long for key storage code length of "
					+ this.getKeyStorageLength() + " in chocice value " + this.getName());
		if (this.isPseudoNumber()) {
			if (value.hasPseudoNumber() == null) {
				try {
					Integer.parseInt(value.getDisplayName());
				} catch (NumberFormatException e) {
					throw new RuntimeException("ChoiceCategory is pseudo-number but choice value " + value.getName()
							+ " display '" + value.getDisplayName()
							+ "' is not a number, and there is no manual pseudoNumber");
				}
			}
		}
		values.add(value);
	}

	@Override
	public String getDefinitionClass() {
		return "SimpleFieldChoiceDefinition";
	}

	@Override
	public void generatetoFile(SourceGenerator sg, Module module) throws IOException {
		sg.wl("package " + module.getPath() + ".data.choice;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		if (this.isPseudoNumber())
			sg.wl("import java.util.HashMap;");
		sg.wl("");
		String choicevalueprefix = "";
		if (this.isPseudoNumber()) {
			choicevalueprefix = "PseudoInteger";
		}
		sg.wl("import org.openlowcode.server.data." + choicevalueprefix + "ChoiceValue;");

		sg.wl("import org.openlowcode.server.data.FieldChoiceDefinition;");
		sg.wl("import org.openlowcode.server.data.SimpleFieldChoiceDefinition;");

		sg.wl("");
		String classname = StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition";
		sg.wl("public class " + classname + " extends " + this.getDefinitionClass() + "<"+classname+"> {");
		sg.wl("	private static " + classname + " singleton;");
		sg.wl("");
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("	public final " + choicevalueprefix + "ChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition> " + currentvalue.getName()
					+ "; ");
		}
		sg.wl("");
		sg.wl("	private ArrayList<" + choicevalueprefix + "ChoiceValue<"
				+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition>> allvalueslist;");
		if (this.isPseudoNumber()) {
			sg.wl("	private HashMap<Integer,PseudoIntegerChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition>> valuesbypseudonumber;");
		}
		sg.wl("");
		sg.wl("	private " + classname + "()  {");
		sg.wl("		super(" + this.getKeyStorageLength() + ");");
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			String pseudovalue = "";
			if (this.isPseudoNumber()) {
				if (currentvalue.hasPseudoNumber() != null) {
					pseudovalue = "," + currentvalue.hasPseudoNumber().toString();
				} else {
					try {
						Integer value = Integer.parseInt(currentvalue.getDisplayName());
						pseudovalue = "," + value;
					} catch (NumberFormatException e) {
						throw new RuntimeException(
								"NumberFormatexception while generating pseudointegervalue " + e.getMessage());
					}
				}
			}
			sg.wl("		" + currentvalue.getName() + " = new " + choicevalueprefix + "ChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition>(\""
					+ currentvalue.getName() + "\",\"" + currentvalue.getDisplayName() + "\",\""
					+ currentvalue.getTooltip() + "\",true" + pseudovalue + ");");
			sg.wl("		this.addChoiceValue(" + currentvalue.getName() + ");");
		}

		sg.wl("		allvalueslist = new ArrayList<" + choicevalueprefix + "ChoiceValue<"
				+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition>>();");
		if (this.isPseudoNumber()) {
			sg.wl("		valuesbypseudonumber = new HashMap<Integer,PseudoIntegerChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition>>();");
		}
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("		allvalueslist.add(" + currentvalue.getName() + ");");
			if (this.isPseudoNumber()) {
				sg.wl("		valuesbypseudonumber.put(new Integer(" + currentvalue.getName()
						+ ".getPseudoIntegerValue())," + currentvalue.getName() + ");");
			}
		}

		sg.wl("	}");
		sg.wl("");

		sg.wl("	public int getValueNumber() {");
		sg.wl("		return allvalueslist.size();");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	public " + choicevalueprefix + "ChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
				+ "ChoiceDefinition> getChoiceAtIndex(int index) {");
		sg.wl("		return allvalueslist.get(index);");
		sg.wl("	}		");
		if (this.isPseudoNumber()) {
			sg.wl("	public PseudoIntegerChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
					+ "ChoiceDefinition> getChoiceByPseudoInteger(int pseudonumber)  {");
			sg.wl("		PseudoIntegerChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
					+ "ChoiceDefinition> lookupvalue = valuesbypseudonumber.get(new Integer(pseudonumber));");
			sg.wl("		if (lookupvalue==null) throw new RuntimeException(\"No value found for pseudointeger \"+pseudonumber);");
			sg.wl("		return lookupvalue;");
			sg.wl("	}	");
		}

		sg.wl("");
		sg.wl("	public static " + classname + " get()  {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + classname + " temp = new " + classname + "();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("}");
		sg.close();
	}

	@Override
	public int getDisplayLabelLength(int label) {
		int maxlength = label;
		for (int i = 0; i < values.getSize(); i++) {
			if (values.get(i).getDisplayName() != null)
				if (values.get(i).getDisplayName().length() > maxlength)
					maxlength = values.get(i).getDisplayName().length();
		}
		return maxlength;
	}

	@Override
	public boolean isKeyPresent(String key) {
		ChoiceValue value = this.values.lookupOnName(key);
		if (value == null)
			return false;
		return true;
	}

}
