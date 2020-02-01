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
 * A multiple choice argument is able to transport several choice values in a
 * single choice. As an example, it could be a list of amenities in an hotel,
 * where a single hotel could have a swimming pool, and air-conditioning, but
 * not a tennis court.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MultipleChoiceArgument
		extends
		ArgumentContent {

	private ChoiceCategory category;

	/**
	 * creates a multiple choice argument
	 * 
	 * @param name     name of the argument, should be unique amongst input and
	 *                 output argument, should be a valid java field name
	 * @param category choice category the choice values are chosen amongst
	 */
	public MultipleChoiceArgument(String name, ChoiceCategory category) {
		super(name, false);
		this.category = category;
	}

	@Override
	public String getType() {
		return "ChoiceValue<" + getChoiceCategoryClass() + ">[]";
	}

	public String getChoiceCategoryClass() {
		return StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition";
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		return null;
	}

	@Override
	public String getGenericDataEltName() {
		return "MultipleChoiceDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "MultipleChoiceDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "MultipleChoiceDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import " + category.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition;");

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
