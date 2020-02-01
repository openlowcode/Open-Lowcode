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
 * A tree where each node or leaf is made of the given argument content type.
 * Typically used or tree of data objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NodeTreeArgument
		extends
		ArgumentContent {
	private ArgumentContent payload;

	/**
	 * creates an argument having as payload a tree of nodes holding the given
	 * argument content
	 * 
	 * @param payload payload of each node of the tree
	 */
	public NodeTreeArgument(ArgumentContent payload) {
		super(payload.getName(), payload.isSecurityrelevant());
		this.payload = payload;
	}

	@Override
	public String getType() {

		return "NodeTree<" + payload.getType() + ">";
	}

	/**
	 * @return the payload of each node of the tree
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

		return "ObjectTreeDataElt<" + payload.getPreciseDataEltName() + ">";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		payload.writeImports(sg, module);
		sg.wl("import org.openlowcode.server.data.NodeTree;");

	}

	@Override
	public String getGenericDataEltName() {
		return "ObjectTreeDataElt<" + payload.getGenericDataEltName() + ">";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "ObjectTreeDataEltType<" + payload.getPreciseDataEltTypeName() + ">";
	}

	@Override
	public String getPreciseDataEltTypeNameWithArgument() {
		return getPreciseDataEltTypeName() + "(new " + payload.getPreciseDataEltTypeNameWithArgument() + ")";
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public ArrayList<String> getImports() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String initblank() {
		return null;
	}

	@Override
	public DataObjectDefinition getMasterObject() {
		return payload.getMasterObject();
	}
}
