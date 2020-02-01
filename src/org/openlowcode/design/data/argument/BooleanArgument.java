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
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A boolean argument will store a true / false value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class BooleanArgument
		extends
		ArgumentContent {
	private ArrayList<String> imports;

	/**
	 * creates a boolean argument of the given name
	 * 
	 * @param name name of the argument (should be a valid java name)
	 */
	public BooleanArgument(String name) {
		super(name, false); // boolean cannot be a security argument
		imports = new ArrayList<String>();
	}

	@Override
	public String getType() {
		return "boolean";
	}

	@Override
	public String getGenericDataEltName() {
		return "BooleanDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "BooleanDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {

		return false;
	}

	@Override
	public String getPreciseDataEltName() {

		return getGenericDataEltName();
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public ArrayList<String> getImports() {
		return imports;
	}

	@Override
	public String initblank() {
		return "false";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return null;
	}
}
