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
 * an argument made of an array of arguments
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ArrayArgument
		extends
		ArgumentContent {
	private ArgumentContent payload;

	/**
	 * creates an array argument with each element of the array being of the given
	 * argument content
	 * 
	 * @param payload payload of one element of the array
	 */
	public ArrayArgument(ArgumentContent payload) {
		super(payload.getName(), payload.isSecurityrelevant());
		this.payload = payload;

	}

	@Override
	public String getType() {

		return payload.getType() + "[]";
	}

	@Override
	public String getGenericDataEltName() {

		return "ArrayDataElt<" + payload.getGenericDataEltName() + ">";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "ArrayDataEltType<" + payload.getPreciseDataEltTypeName() + ">";
	}

	@Override
	public String getPreciseDataEltTypeNameWithArgument() {
		return getPreciseDataEltTypeName() + "(new " + payload.getPreciseDataEltTypeNameWithArgument() + ")";
	}

	/**
	 * @return the argument content of the payload of the array (e.g. for an array
	 *         of objects, will send back an ObjectArgument
	 */
	public ArgumentContent getPayload() {
		return this.payload;
	}

	@Override
	public boolean needDefinitionForInit() {
		return payload.needDefinitionForInit();
	}

	@Override
	public String getPreciseDataEltName() {

		return "ArrayDataElt<" + payload.getPreciseDataEltName() + ">";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		payload.writeImports(sg, module);

	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public ArrayList<String> getImports() {
		return payload.getImports();
	}

	@Override
	public String initblank() {

		return "new " + payload.getType() + "[]";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return payload.getMasterObject();
	}
}
