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
 * an argument content being made of 3 Objects. One example is for sending an
 * array of triplets of left object, link object, and right object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ThreeObjectsArgument
		extends
		ArgumentContent {
	private ObjectArgument objectone;
	private ObjectArgument objecttwo;
	private ObjectArgument objectthree;
	private ArrayList<String> imports;

	/**
	 * creates a three object argument
	 * 
	 * @param name        name of the argument, should be unique amongst input and
	 *                    output argument, should be a valid java field name
	 * @param objectone   first object of the triplet
	 * @param objecttwo   second object of the triplet
	 * @param objectthree third object of the triplet
	 */
	public ThreeObjectsArgument(
			String name,
			ObjectArgument objectone,
			ObjectArgument objecttwo,
			ObjectArgument objectthree) {
		super(name, false); // security argument not supported
		this.objectone = objectone;
		this.objecttwo = objecttwo;
		this.objectthree = objectthree;
		imports = new ArrayList<String>();
		imports.addAll(objectone.getImports());
		imports.addAll(objecttwo.getImports());
		imports.addAll(objectthree.getImports());
	}

	@Override
	public String getType() {
		return "ThreeDataObjects<" + objectone.getType() + "," + objecttwo.getType() + "," + objectthree.getType()
				+ ">";
	}

	@Override
	public String getGenericDataEltName() {
		return "#NOT SUPPORTED YET#";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "#NOT SUPPORTED YET#";
	}

	@Override
	public boolean needDefinitionForInit() {
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "#NOT SUPPORTED YET#";
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
		return " new ThreeDataObjects<" + objectone.getType() + "," + objecttwo.getType() + "," + objectthree.getType()
				+ ">(" + objectone.initblank() + "," + objecttwo.initblank() + "," + objectthree.initblank() + ")";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return null;
	}
}
