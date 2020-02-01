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
 * an argument having the integer class as payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IntegerArgument
		extends
		ArgumentContent {
	private ArrayList<String> imports;

	/**
	 * creates an integer argument of the given name
	 * 
	 * @param name name of the argument, should be unique amongst input and output
	 *             argument, should be a valid java field name
	 */
	public IntegerArgument(String name) {
		super(name, false); // integer cannot be a security argument
		imports = new ArrayList<String>();
	}

	/**
	 * creates an integer argument of the given name
	 * 
	 * @param name  name of the argument, should be unique amongst input and output
	 *              argument, should be a valid java field name
	 * @param label plain language label of the field
	 */
	public IntegerArgument(String name, String label) {
		super(name, false); // integer cannot be a security argument
		imports = new ArrayList<String>();
		this.setDisplaylabel(label);
	}

	@Override
	public String getType() {
		return "Integer";
	}

	@Override
	public String getGenericDataEltName() {
		return "IntegerDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "IntegerDataEltType";
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
		return "new Integer(0)";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return null;
	}
}
