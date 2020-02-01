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
 * an argument having a large binary (file) as a payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LargeBinaryArgument
		extends
		ArgumentContent {
	private ArrayList<String> imports;

	/**
	 * creates a large binary argument
	 * 
	 * @param name             name of the argument, should be unique amongst input
	 *                         and output argument, should be a valid java field
	 *                         name
	 * @param securityrelevant true if the argument is security relevant (probably
	 *                         not used, see Github issue #23)
	 */
	public LargeBinaryArgument(String name, boolean securityrelevant) {
		super(name, securityrelevant);
		imports = new ArrayList<String>();
		imports.add("import org.openlowcode.tools.messages.SFile;");
	}

	@Override
	public String getType() {

		return "SFile";
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String getGenericDataEltName() {
		return "LargeBinaryDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "LargeBinaryDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "LargeBinaryDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.tools.messages.SFile;");

	}

	@Override
	public ArrayList<String> getImports() {
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
