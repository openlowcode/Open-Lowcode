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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringArgument
		extends
		ArgumentContent {
	private ArrayList<String> imports;
	private int maxlength;

	/**
	 * @return the maximum length of the string
	 */
	public int getMaxLength() {
		return this.maxlength;
	}

	/**
	 * creates a string argument of the given length
	 * 
	 * @param name      name of the argument, should be unique amongst input and
	 *                  output argument, should be a valid java field name
	 * @param maxlength maximum length of the string
	 */
	public StringArgument(String name, int maxlength) {
		super(name, false); // string cannot be a security argument
		imports = new ArrayList<String>();
		this.maxlength = maxlength;
	}

	/**
	 * creates a string argument of the given length
	 * 
	 * @param name      name of the argument, should be unique amongst input and
	 *                  output argument, should be a valid java field name
	 * @param maxlength maximum length of the string
	 * @param label     plain language label for the argument
	 */
	public StringArgument(String name, int maxlength, String label) {
		super(name, false); // string cannot be a security argument
		imports = new ArrayList<String>();
		this.maxlength = maxlength;
		this.setDisplaylabel(label);
	}

	@Override
	public String getType() {
		return "String";
	}

	@Override
	public String getGenericDataEltName() {
		return "TextDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "TextDataEltType";
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
		return "\"\"";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return null;
	}
}
