/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.design.utility;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalField;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * A multi-field constraint allows to set only certain combinations of values as
 * valid across several fields
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MultiFieldConstraint
		extends
		Named {
	private DataObjectDefinition object;
	private ArrayList<Field> fieldsinorder;

	/**
	 * Allows to set a constraint between fields. Typically, this would allow to
	 * choose a country, then only regions or states inside this precise country
	 * (you would get the choice "Illinois" for region/state only if you choose
	 * "USA" as a country.
	 * 
	 * @param name   name of the constraint, should be unique for the module
	 * @param object the data object on which the constraint is set
	 */
	public MultiFieldConstraint(String name, DataObjectDefinition object) {
		super(name);
		this.object = object;
		object.addMultiFieldConstraint(this);
		this.fieldsinorder = new ArrayList<Field>();

	}

	/**
	 * allows to set a field in the sequence of constraint (add 1st field like
	 * country first, then 2nd field like region, then 3rd field like city...).
	 * Today, the following values are allowed: ChoiceField and DecimalField.
	 * 
	 * @param field the field to enter
	 */
	public void addField(Field field) {
		if (field == null)
			throw new RuntimeException("Cannot add null field for multifieldconstraint " + this.getName()
					+ " for object " + object.getName());
		boolean iscorrectclass = false;

		if (field instanceof ChoiceField)
			iscorrectclass = true;
		if (field instanceof DecimalField)
			iscorrectclass = true;
		if (!iscorrectclass)
			throw new RuntimeException("only ChoiceField and DecimalField allowed for field " + field.getName()
					+ " for multifieldconstraint " + this.getName() + " for object " + object.getName());

		for (int i = 0; i < this.fieldsinorder.size(); i++) {
			if (this.fieldsinorder.get(i).getName().compareTo(field.getName()) == 0)
				throw new RuntimeException("Duplicate Field " + field.getName() + " for multifieldconstraint "
						+ this.getName() + " for object " + object.getName());
		}
		this.fieldsinorder.add(field);
	}

	/**
	 * generates the multi-field constraint to file. This is an automatically
	 * generated utility class
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateFile(SourceGenerator sg, Module module) throws IOException {
		String constraintclass = StringFormatter.formatForJavaClass(this.getName());

		sg.wl("package " + module.getPath() + ".utility.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("");
		for (int i = 0; i < this.fieldsinorder.size(); i++) {
			Field thisfield = this.fieldsinorder.get(i);
			thisfield.writeDependentClass(sg, module);
		}
		sg.wl("import gallium.server.data.ChoiceValue;");
		sg.wl("import gallium.server.utility.SMultiFieldConstraint;");
		sg.wl("import gallium.tools.trace.GalliumException;");
		sg.wl("import java.math.MathContext;");
		sg.wl("");
		sg.wl("public abstract class Abs" + constraintclass + "MultiFieldConstraint {");
		sg.wl("	");
		sg.wl("	private SMultiFieldConstraint storage;");
		sg.wl("	public SMultiFieldConstraint getStorage() {");
		sg.wl("		return storage;");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	public void addOneConstraint(");
		for (int i = 0; i < this.fieldsinorder.size(); i++) {
			Field thisfield = this.fieldsinorder.get(i);
			String comma = "";
			if (i > 0)
				comma = ",";
			sg.wl("			" + comma + thisfield.getJavaType() + " " + thisfield.getName().toLowerCase());
		}
		sg.wl(" 		)	throws GalliumException {");
		sg.wl("		ArrayList<String> rawconstraintline = new ArrayList<String>();");
		for (int i = 0; i < this.fieldsinorder.size(); i++) {
			Field thisfield = this.fieldsinorder.get(i);
			boolean done = false;
			if (thisfield instanceof ChoiceField) {
				sg.wl("		rawconstraintline.add((" + thisfield.getName().toLowerCase() + "!=null?"
						+ thisfield.getName().toLowerCase() + ".getStorageCode():null));");
				done = true;
			}
			if (thisfield instanceof DecimalField) {
				DecimalField thisdecimalfield = (DecimalField) thisfield;
				sg.wl("		rawconstraintline.add((" + thisfield.getName().toLowerCase() + "!=null?"
						+ thisfield.getName().toLowerCase() + ".round(new MathContext("
						+ thisdecimalfield.getPrecision() + ")).toPlainString():null));");
				done = true;
			}
			if (!done)
				throw new RuntimeException(
						"type not supported for field " + thisfield.getName() + " in constraint " + this.getName());
		}

		sg.wl("		storage.addOneLineOfConstraint(rawconstraintline);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	public abstract void addData() throws GalliumException;");
		sg.wl("	");
		sg.wl("	public Abs" + constraintclass + "MultiFieldConstraint() throws GalliumException {");
		sg.wl("		this.storage=new SMultiFieldConstraint(\"" + this.getName().toUpperCase() + "\");");
		for (int i = 0; i < this.fieldsinorder.size(); i++) {
			Field thisfield = this.fieldsinorder.get(i);
			sg.wl("		storage.addField(\"" + thisfield.getName().toUpperCase() + "\");");

		}
		sg.wl("		addData();");
		sg.wl("	}");
		sg.wl("}");

		sg.close();
	}

}
