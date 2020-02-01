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
 * An argument holding a time period (year, quarter, month...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriodArgument
		extends
		ArgumentContent {

	/**
	 * creates a TimePeriod argument that is not a security argument
	 * 
	 * @param name      name of the argument, should be unique amongst input and
	 *                  output argument, should be a valid java field name
	 */
	public TimePeriodArgument(String name) {
		super(name, false);

	}

	@Override
	public String getType() {
		return "TimePeriod";
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String getGenericDataEltName() {
		return "TimePeriodDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {
		return "TimePeriodDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "TimePeriodDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.tools.data.TimePeriod;");
	}

	@Override
	public ArrayList<String> getImports() {
		ArrayList<String> importstatement = new ArrayList<String>();
		importstatement.add("import org.openlowcode.tools.data.TimePeriod;");
		return importstatement;
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
