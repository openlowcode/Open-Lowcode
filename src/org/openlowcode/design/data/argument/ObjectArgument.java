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
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * an argument holding the payload of an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectArgument
		extends
		ArgumentContent {
	private DataObjectDefinition payload;
	private ArrayList<String> imports;

	public ObjectArgument(String name, DataObjectDefinition payload) {
		this(name, payload, false);
	}

	/**
	 * creates an argument content holding the payload of an object
	 * 
	 * @param name             name of the argument, should be unique amongst input
	 *                         and output argument, should be a valid java field
	 *                         name
	 * @param payload          the type of data object held as payload
	 * @param securityargument if true, this argument is the security argument. If
	 *                         input argument of an action, a check will be done
	 *                         before the action, if output argument of an action,
	 *                         data will be filtered before being sent to the client
	 */
	public ObjectArgument(String name, DataObjectDefinition payload, boolean securityargument) {
		super(name, securityargument);
		this.payload = payload;
		imports = new ArrayList<String>();
		imports.add("import " + payload.getOwnermodule().getPath() + ".data."
				+ StringFormatter.formatForJavaClass(payload.getName()) + ";");
	}

	@Override
	public String getType() {

		return StringFormatter.formatForJavaClass(payload.getName());
	}

	@Override
	public String getGenericDataEltName() {
		return "ObjectDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "TObjectDataEltType<" + StringFormatter.formatForJavaClass(payload.getName()) + ">";
	}

	@Override
	public String getPreciseDataEltTypeNameWithArgument() {
		return getPreciseDataEltTypeName() + "(" + StringFormatter.formatForJavaClass(payload.getName())
				+ ".getDefinition())";
	}

	public DataObjectDefinition getPayload() {
		return this.payload;
	}

	@Override
	public boolean needDefinitionForInit() {
		return true;
	}

	@Override
	public String getPreciseDataEltName() {

		return "TObjectDataElt<" + StringFormatter.formatForJavaClass(payload.getName()) + ">";
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
		return "new " + StringFormatter.formatForJavaClass(payload.getName()) + "()";
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return this.payload;

	}
}
