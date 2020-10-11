/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.argument;

import java.io.IOException;

import java.util.ArrayList;

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A list of value argument will allow user to enter only a limited list of
 * valid values. It is mostly used with string values.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChoiceArgument
		extends
		ArgumentContent {

	private ChoiceCategory category;

	/**
	 * creates a choice argument
	 * 
	 * @param name     name of the argument, should be a valid java field name
	 * @param category choice category of the argument
	 */
	public ChoiceArgument(String name, ChoiceCategory category) {
		super(name, false);
		this.category = category;
	}

	@Override
	public String getType() {

		return "ChoiceValue<" + getChoiceCategoryClass() + ">";
	}

	/**
	 * @return the choice category class for code generation
	 */
	public String getChoiceCategoryClass() {
		return StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition";
	}

	@Override
	public String getGenericDataEltName() {

		return "ChoiceDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {

		return "ChoiceDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "ChoiceDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import " + category.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition;");

	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		return new ChoiceArgument(newname,category);
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add("import org.openlowcode.server.data.ChoiceValue;");
		imports.add("import " + category.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition;");
		return imports;
	}

	@Override
	public String initblank() {
		return "null";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return null;
	}
}
