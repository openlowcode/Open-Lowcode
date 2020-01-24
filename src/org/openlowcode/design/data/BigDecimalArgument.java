/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * An argument containing as payload a BigDecimal
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class BigDecimalArgument
		extends
		ArgumentContent {
	private ArrayList<String> imports;

	/**
	 * creates a big decimal argument with the given name (note: precision and scale
	 * are not precised)
	 * 
	 * @param name name of the argument
	 */
	public BigDecimalArgument(String name) {
		super(name, false); // decimal cannot be a security argument
		imports = new ArrayList<String>();
	}

	/**
	 * creates a big decimal argument with the given name (note: precision and scale
	 * are not precised)
	 * 
	 * @param name  name of the argument
	 * @param label label of the argument
	 */
	public BigDecimalArgument(String name, String label) {
		super(name, false); // decimal cannot be a security argument
		imports = new ArrayList<String>();
		this.setDisplaylabel(label);
	}

	@Override
	public String getType() {
		return "BigDecimal";
	}

	@Override
	public ArgumentContent generateCopy(String newname)  {
		throw new RuntimeException( "Not yet implemented");
	}

	@Override
	public String getGenericDataEltName() {
		return "DecimalDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "DecimalDataEltType";
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
