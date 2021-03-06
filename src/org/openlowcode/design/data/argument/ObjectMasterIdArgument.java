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
 * An argument holding as payload the master id of a Versioned data object. This
 * is the id common to all versions of the data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectMasterIdArgument
		extends
		ArgumentContent {
	private DataObjectDefinition payload;
	private ArrayList<String> imports;

	/**
	 * creates an argument holding as payload the master id of an object
	 * 
	 * @param name    name of the argument, should be unique amongst input and
	 *                output argument, should be a valid java field name
	 * @param payload the type of data object held as payload
	 */
	public ObjectMasterIdArgument(String name, DataObjectDefinition payload) {
		super(name, false);
		this.payload = payload;
		imports = new ArrayList<String>();
		imports.add("import org.openlowcode.server.data.properties.DataObjectMasterId;");
		if (payload != null)
			imports.add("import " + payload.getOwnermodule().getPath() + ".data."
					+ StringFormatter.formatForJavaClass(payload.getName()) + ";");
	}

	/**
	 * @return the type of data object the master id is refering to
	 */
	public DataObjectDefinition getObject() {
		return payload;
	}

	/**
	 * creates an argument holding as payload the master id of an object
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
	public ObjectMasterIdArgument(String name, DataObjectDefinition payload, boolean securityargument) {
		super(name, securityargument);
		this.payload = payload;

	}

	@Override
	public String getType() {
		if (payload != null)
			return "DataObjectMasterId<" + StringFormatter.formatForJavaClass(payload.getName()) + ">";
		return "DataObjectMasterId<DataObject>";
	}

	/**
	 * @return the object type for code generation
	 */
	public String getObjectType() {
		if (payload != null)
			return StringFormatter.formatForJavaClass(this.payload.getName());
		return "DataObject";
	}

	@Override
	public String getGenericDataEltName() {
		return "ObjectMasterIdDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		if (payload != null)
			return "TObjectMasterIdDataEltType<" + StringFormatter.formatForJavaClass(payload.getName()) + ">";
		return "TObjectMasterIdDataEltType";

	}

	@Override
	public boolean needDefinitionForInit() {

		return false; // mostly behaves as a string
	}

	@Override
	public String getPreciseDataEltName() {
		return "ObjectMasterIdDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.properties.DataObjectMasterId;");
		if (payload != null)

			sg.wl("import " + payload.getOwnermodule().getPath() + ".data."
					+ StringFormatter.formatForJavaClass(payload.getName()) + ";");
		sg.wl("import org.openlowcode.server.data.DataObject;");
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		ObjectIdArgument copy = new ObjectIdArgument(newname, payload, this.isSecurityrelevant());
		copy.setOptional(this.isOptional());
		return copy;
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
		return this.payload;
	}
}
